package de.tum.in.www1.jenkins.notifications;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SendTestResultsNotificationPostBuildTask extends Recorder implements SimpleBuildStep {
    private String credentialsId;
    private String notificationUrl;

    @DataBoundConstructor
    public SendTestResultsNotificationPostBuildTask(String credentialsId, String notificationUrl) {
        this.credentialsId = credentialsId;
        this.notificationUrl = notificationUrl;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        final FilePath resultsDir = filePath.child("results");
        final List<Testsuite> reports = extractTestResults(taskListener, resultsDir);
        final TestResults results = rememberTestResults(run, reports);
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

    private TestResults rememberTestResults(@Nonnull Run<?, ?> run, List<Testsuite> reports) {
        final TestResults results = new TestResults();
        results.setResults(reports);
        results.setCommitHashes(findCommitHashes(run));
        results.setFullName(run.getFullDisplayName());
        run.addAction(results);
        return results;
    }

    private List<String> findCommitHashes(Run<?, ?> run) {
        return run.getActions(BuildData.class).stream()
                .map(BuildData::getLastBuiltRevision)
                .filter(Objects::nonNull)
                .map(revision -> revision.getSha1().name())
                .collect(Collectors.toList());
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
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
