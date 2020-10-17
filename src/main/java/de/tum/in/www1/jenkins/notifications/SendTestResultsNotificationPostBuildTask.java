package de.tum.in.www1.jenkins.notifications;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import de.tum.in.ase.parser.ReportParser;
import de.tum.in.ase.parser.domain.Report;
import de.tum.in.ase.parser.exception.ParserException;
import de.tum.in.www1.jenkins.notifications.model.Commit;
import de.tum.in.www1.jenkins.notifications.model.TestResults;
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
import hudson.util.Secret;
import de.tum.in.www1.jenkins.notifications.model.Testsuite;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SendTestResultsNotificationPostBuildTask extends Recorder implements SimpleBuildStep {
    private static final String TEST_RESULTS_PATH = "results";
    private static final String STATIC_CODE_ANALYSIS_REPORTS_PATH = "staticCodeAnalysisReports";

    private String credentialsId;
    private String notificationUrl;

    @DataBoundConstructor
    public SendTestResultsNotificationPostBuildTask(String credentialsId, String notificationUrl) {
        this.credentialsId = credentialsId;
        this.notificationUrl = notificationUrl;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        final FilePath testResultsDir = filePath.child(TEST_RESULTS_PATH);
        final FilePath staticCodeAnalysisResultsDir = filePath.child(STATIC_CODE_ANALYSIS_REPORTS_PATH);
        final List<Testsuite> testReports = extractTestResults(taskListener, testResultsDir);
        final List<Report> staticCodeAnalysisReport = parseStaticCodeAnalysisReports(taskListener, staticCodeAnalysisResultsDir);
        final TestResults results = rememberTestResults(run, testReports, staticCodeAnalysisReport);
        final Secret secret = Objects.requireNonNull(CredentialsProvider
                .findCredentialById(credentialsId, StringCredentials.class, run, Collections.emptyList()))
                .getSecret();

        // Post results to notification URL
        try {
            HttpHelper.postTestResults(results, notificationUrl, secret);
        } catch (HttpException e) {
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
                        } catch (JAXBException | IOException | InterruptedException e) {
                            taskListener.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
    }

    private TestResults rememberTestResults(@Nonnull Run<?, ?> run, List<Testsuite> testReports, List<Report> staticCodeAnalysisReports) {
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
            } else {
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
            } else {
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
