package de.tum.in.ase.notification.configuration;

/**
 * The context object is used to provide the plugin with all the information it needs to perform its task.
 */
public class Context {
    private final String testResultsDir;
    private final String customFeedbackDir;
    private final String testGitHash;
    private final String testGitRepositorySlug;
    private final String testGitBranch;
    private final String submissionGitHash;
    private final String submissionGitRepositorySlug;
    private final String submissionGitBranch;
    private final String buildPlanId;
    private final String buildStatus;
    private final String buildLogsFile;
    private final String notificationUrl;
    private final String notificationSecret;

    public Context(String testResultsDir, String customFeedbackDir, String testGitHash, String testGitRepositorySlug, String testGitBranch, String submissionGitHash, String submissionGitRepositorySlug, String submissionGitBranch, String buildPlanId, String buildStatus, String buildLogsFile, String notificationUrl, String notificationSecret) {
        this.testResultsDir = testResultsDir;
        this.customFeedbackDir = customFeedbackDir;
        this.testGitHash = testGitHash;
        this.testGitRepositorySlug = testGitRepositorySlug;
        this.testGitBranch = testGitBranch;
        this.submissionGitHash = submissionGitHash;
        this.submissionGitRepositorySlug = submissionGitRepositorySlug;
        this.submissionGitBranch = submissionGitBranch;
        this.buildPlanId = buildPlanId;
        this.buildStatus = buildStatus;
        this.buildLogsFile = buildLogsFile;
        this.notificationUrl = notificationUrl;
        this.notificationSecret = notificationSecret;
    }

    public String getTestResultsDir() {
        return testResultsDir;
    }

    public String getCustomFeedbackDir() {
        return customFeedbackDir;
    }

    public String getTestGitHash() {
        return testGitHash;
    }

    public String getTestGitRepositorySlug() {
        return testGitRepositorySlug;
    }

    public String getTestGitBranch() {
        return testGitBranch;
    }

    public String getSubmissionGitHash() {
        return submissionGitHash;
    }

    public String getSubmissionGitRepositorySlug() {
        return submissionGitRepositorySlug;
    }

    public String getSubmissionGitBranch() {
        return submissionGitBranch;
    }

    public String getBuildPlanId() {
        return buildPlanId;
    }

    public String getBuildStatus() {
        return buildStatus;
    }

    public String getBuildLogsFile() {
        return buildLogsFile;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public String getNotificationSecret() {
        return notificationSecret;
    }
}
