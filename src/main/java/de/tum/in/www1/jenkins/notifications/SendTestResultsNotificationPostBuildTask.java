package de.tum.in.www1.jenkins.notifications;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.sun.xml.bind.v2.ContextFactory;

import de.tum.in.ase.parser.domain.Report;
import de.tum.in.www1.jenkins.notifications.exception.TestParsingException;
import de.tum.in.www1.jenkins.notifications.model.Commit;
import de.tum.in.www1.jenkins.notifications.model.ObjectFactory;
import de.tum.in.www1.jenkins.notifications.model.TestResults;
import de.tum.in.www1.jenkins.notifications.model.Testsuite;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitObject;
import hudson.plugins.git.util.BuildData;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class SendTestResultsNotificationPostBuildTask extends Recorder implements SimpleBuildStep {

    private static final String TEST_RESULTS_PATH = "results";

    private static final String CUSTOM_FEEDBACKS_PATH = "customFeedbacks";

    private static final String STATIC_CODE_ANALYSIS_REPORTS_PATH = "staticCodeAnalysisReports";

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
        final FilePath customFeedbacksDir = filePath.child(CUSTOM_FEEDBACKS_PATH);
        final FilePath staticCodeAnalysisResultsDir = filePath.child(STATIC_CODE_ANALYSIS_REPORTS_PATH);

        final List<Testsuite> testReports = extractTestResults(taskListener, testResultsDir);
        final Optional<Testsuite> customFeedbacks = CustomFeedbackParser.extractCustomFeedbacks(taskListener, customFeedbacksDir);
        customFeedbacks.ifPresent(testReports::add);
        final List<Report> staticCodeAnalysisReport = StaticCodeAnalysisParser.parseReports(taskListener, staticCodeAnalysisResultsDir);

        final TestResults results = combineTestResults(run, testReports, staticCodeAnalysisReport);

        // Set build status
        results.setIsBuildSuccessful(run.getResult() == Result.SUCCESS);

        // Add build logs
        results.setLogs(extractLogs(run, taskListener));

        final StringCredentials credentials = CredentialsProvider.findCredentialById(credentialsId, StringCredentials.class, run, Collections.emptyList());
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
        return resultsDir.list().stream().filter(path -> path.getName().endsWith(".xml")).map(report -> {
            try {
                final JAXBContext context = createJAXBContext();
                final Unmarshaller unmarshaller = context.createUnmarshaller();
                Testsuite testsuite = (Testsuite) unmarshaller.unmarshal(report.read());
                return testsuite.flatten();
            }
            catch (JAXBException | IOException | InterruptedException e) {
                taskListener.error(e.getMessage(), e);
                throw new TestParsingException(e);
            }
        }).collect(Collectors.toList());
    }

    private JAXBContext createJAXBContext() throws JAXBException {
        return ContextFactory.createContext(ObjectFactory.class.getPackage().getName(), ObjectFactory.class.getClassLoader(), null);
    }

    private List<String> extractLogs(@Nonnull Run<?, ?> run, TaskListener taskListener) {
        final List<String> logs = new ArrayList<>();

        try (StringWriter stringWriter = new StringWriter()) {
            run.getLogText().writeLogTo(0, stringWriter);

            final String logString = stringWriter.toString();
            Collections.addAll(logs, logString.split("\n"));
        }
        catch (IOException ex) {
            taskListener.error(ex.getMessage(), ex);
        }

        return logs;
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
        return run.getActions(BuildData.class).stream().map(buildData -> {
            final String[] urlString = buildData.getRemoteUrls().iterator().next().split("/");
            final String slug = urlString[urlString.length - 1].split("\\.")[0];
            final String hash = Objects.requireNonNull(buildData.getLastBuiltRevision()).getSha1().name();
            final Commit commit = new Commit();
            commit.setRepositorySlug(slug);
            commit.setHash(hash);
            commit.setBranchName(getBranchName(buildData));
            return commit;
        }).collect(Collectors.toList());
    }

    private @Nullable String getBranchName(BuildData buildData) {
        if (buildData.getLastBuiltRevision() == null) {
            return null;
        }

        String branchName = buildData.getLastBuiltRevision().getBranches().stream().map(GitObject::getName).findFirst().orElse(null);
        if (branchName == null) {
            return null;
        }

        // The branch name is in the format REPO_NAME/BRANCH_NAME -> We want to get rid of the REPO_NAME (the BRANCH_NAME might also contain /, so we can not simply use
        // branchNameParts[1])
        String[] branchNameParts = branchName.split("/");
        String[] branchNamePartsWithoutRepositoryName = Arrays.copyOfRange(branchNameParts, 1, branchNameParts.length);
        return String.join("/", branchNamePartsWithoutRepositoryName);
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
                if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result.includeMatchingAs(ACL.SYSTEM, item, StringCredentials.class, Collections.emptyList(), CredentialsMatchers.always())
                    .includeCurrentValue(credentialsId);
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            }
            else {
                if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
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
