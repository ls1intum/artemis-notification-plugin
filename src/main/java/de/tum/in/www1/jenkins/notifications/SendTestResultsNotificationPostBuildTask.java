package de.tum.in.www1.jenkins.notifications;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.google.gson.Gson;
import de.tum.in.ase.parser.ReportParser;
import de.tum.in.ase.parser.domain.Report;
import de.tum.in.ase.parser.exception.ParserException;
import de.tum.in.www1.jenkins.notifications.exception.TestParsingException;
import de.tum.in.www1.jenkins.notifications.model.*;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.util.BuildData;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SendTestResultsNotificationPostBuildTask extends Recorder implements SimpleBuildStep {

    private static final String TEST_RESULTS_PATH = "results";

    private static final String STATIC_CODE_ANALYSIS_REPORTS_PATH = "staticCodeAnalysisReports";

    private static final String CUSTOM_FEEDBACKS_RESULTS_PATH = "customFeedbacks";

    private String credentialsId;

    private String notificationUrl;

    @DataBoundConstructor
    public SendTestResultsNotificationPostBuildTask(String credentialsId, String notificationUrl) {
        this.credentialsId = credentialsId;
        this.notificationUrl = notificationUrl;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener)
            throws InterruptedException, IOException {
        final FilePath testResultsDir = filePath.child(TEST_RESULTS_PATH);
        final FilePath staticCodeAnalysisResultsDir = filePath.child(STATIC_CODE_ANALYSIS_REPORTS_PATH);

        final List<Testsuite> testReports = extractTestResults(taskListener, testResultsDir);
        extractCustomFeedbacks(taskListener, filePath.child(CUSTOM_FEEDBACKS_RESULTS_PATH)).ifPresent(testReports::add);
        final List<Report> staticCodeAnalysisReport = parseStaticCodeAnalysisReports(taskListener, staticCodeAnalysisResultsDir);

        final TestResults results = combineTestResults(run, testReports, staticCodeAnalysisReport);
        final StringCredentials credentials = CredentialsProvider
                .findCredentialById(credentialsId, StringCredentials.class, run, Collections.emptyList());
        final String secret = credentials != null ? credentials.getSecret().getPlainText() : "Credentials containing the Notification Plugin Secret not found";

        // Post results to notification URL
        try {
            HttpHelper.postTestResults(results, notificationUrl, secret);
        }
        catch (HttpException e) {
            taskListener.error(e.getMessage(), e);
        }
    }

    private List<Testsuite> extractTestResults(@Nonnull TaskListener taskListener, FilePath resultsDir) throws IOException, InterruptedException {
        return resultsDir.list().stream()
                .filter(path -> path.getName().endsWith(".xml"))
                .map(report -> {
                    try {
                        final JAXBContext context = JAXBContext.newInstance(Testsuite.class);
                        final Unmarshaller unmarshaller = context.createUnmarshaller();
                        return (Testsuite) unmarshaller.unmarshal(report.read());
                    }
                    catch (JAXBException | IOException | InterruptedException e) {
                        taskListener.error(e.getMessage(), e);
                        throw new TestParsingException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private TestResults combineTestResults(@Nonnull Run<?, ?> run, List<Testsuite> testReports, List<Report> staticCodeAnalysisReports) {
        int skipped = 0;
        int failed = 0;
        int successful = 0;
        int errors = 0;
        for (final Testsuite suite : testReports) {
            successful += suite.getTests() - (suite.getErrors() + suite.getFailures() + suite.getSkipped());
            failed += suite.getFailures();
            errors += suite.getErrors();
            skipped += suite.getSkipped();
        }

        final TestResults results = new TestResults();
        results.setResults(testReports);
        results.setStaticCodeAnalysisReports(staticCodeAnalysisReports);
        results.setCommits(findCommits(run));
        results.setFullName(run.getFullDisplayName());
        results.setErrors(errors);
        results.setSkipped(skipped);
        results.setSuccessful(successful);
        results.setFailures(failed);
        run.addAction(results);
        return results;
    }

    private List<Commit> findCommits(Run<?, ?> run) {
        return run.getActions(BuildData.class).stream()
                .map(buildData -> {
                    final String[] urlString = buildData.getRemoteUrls().iterator().next().split("/");
                    final String slug = urlString[urlString.length - 1].split("\\.")[0];
                    final String hash = Objects.requireNonNull(buildData.getLastBuiltRevision()).getSha1().name();
                    final Commit commit = new Commit();
                    commit.setRepositorySlug(slug);
                    commit.setHash(hash);

                    return commit;
                })
                .collect(Collectors.toList());
    }

    private List<Report> parseStaticCodeAnalysisReports(TaskListener taskListener, FilePath staticCodeAnalysisResultDir) {
        // Static code analysis parsing must not crash the sending of notifications under any circumstances
        try {
            List<Report> reports = new ArrayList<>();
            ReportParser reportParser = new ReportParser();
            for (FilePath filePath : staticCodeAnalysisResultDir.list()) {
                if (!filePath.getName().endsWith(".xml")) {
                    continue;
                }

                // Try to parse each report separately. Failure parsing one report should not effect the parsing of others
                try {
                    Report report = reportParser.transformToReport(filePath.read());
                    reports.add(report);
                }
                catch (ParserException | IOException | InterruptedException e) {
                    taskListener.error(e.getMessage(), e);
                }
            }
            return reports;
        }
        catch (Exception e) {
            taskListener.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private Optional<Testsuite> extractCustomFeedbacks(@Nonnull TaskListener taskListener, FilePath resultsDir) throws IOException, InterruptedException {
        final Gson gson = new Gson();
        final List<CustomFeedback> feedbacks = resultsDir.list()
                .stream()
                .filter(path -> path.getName().endsWith(".json"))
                .map(feedbackFile -> {
                    try {
                        final CustomFeedback feedback = gson.fromJson(feedbackFile.readToString(), CustomFeedback.class);
                        if (feedback.getName() == null) {
                            throw new IOException("Custom feedbacks need to have a name attribute.");
                        }
                        return feedback;
                    } catch (IOException | InterruptedException e) {
                        taskListener.error(e.getMessage(), e);
                        throw new TestParsingException(e);
                    }
                }).collect(Collectors.toList());

        if (feedbacks.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(customFeedbacksToTestSuite(feedbacks));
        }
    }

    /**
     * Convert the feedbacks into {@link TestCase}s and wrap them in a {@link Testsuite}
     *
     * @param feedbacks the list of parsed custom feedbacks to wrap
     * @return a Testsuite in the same format as used by JUnit reports
     */
    private Testsuite customFeedbacksToTestSuite(final List<CustomFeedback> feedbacks) {
        final Testsuite suite = new Testsuite();
        suite.setName("customFeedbackReports");

        final List<TestCase> testCases = feedbacks.stream().map(feedback -> {
            final TestCase testCase = new TestCase();
            testCase.setName(feedback.getName());

            if (feedback.isSuccessful()) {
                final SuccessInfo successInfo = new SuccessInfo();
                successInfo.setMessage(feedback.getMessage());
                final List<SuccessInfo> infos = new ArrayList<>();
                infos.add(successInfo);

                testCase.setSuccessInfos(infos);
            } else {
                final Failure failure = new Failure();
                failure.setMessage(feedback.getMessage());
                final List<Failure> failures = new ArrayList<>();
                failures.add(failure);

                testCase.setFailures(failures);
            }

            return testCase;
        }).collect(Collectors.toList());

        suite.setTestCases(testCases);

        return suite;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    @Extension
    @Symbol("sendTestResults")
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.SendTestResultsNotificationPostBuildTask_DescriptorImpl_DisyplayName();
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            final StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result
                    .includeMatchingAs(ACL.SYSTEM, item, StringCredentials.class, Collections.emptyList(), CredentialsMatchers.always())
                    .includeCurrentValue(credentialsId);
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            }
            else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            if (value.startsWith("${") && value.endsWith("}")) {
                return FormValidation.warning("Cannot validate expression based credentials");
            }
            return FormValidation.ok();
        }
    }
}
