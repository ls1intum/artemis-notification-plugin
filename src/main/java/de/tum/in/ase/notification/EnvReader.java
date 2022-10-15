package de.tum.in.ase.notification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnvReader {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TEST_RESULTS_DIR_KEY = "TEST_RESULTS_DIR";

    private static final String CUSTOM_FEEDBACK_DIR_KEY = "CUSTOM_FEEDBACK_DIR";

    private static final String TEST_GIT_HASH_KEY = "TEST_GIT_HASH";

    private static final String TEST_GIT_REPOSITORY_SLUG_KEY = "TEST_GIT_REPOSITORY_SLUG";

    private static final String TEST_GIT_BRANCH_KEY = "TEST_GIT_BRANCH";

    private static final String SUBMISSION_GIT_HASH_KEY = "SUBMISSION_GIT_HASH";

    private static final String SUBMISSION_GIT_REPOSITORY_SLUG_KEY = "SUBMISSION_GIT_REPOSITORY_SLUG";

    private static final String SUBMISSION_GIT_BRANCH_KEY = "SUBMISSION_GIT_BRANCH";

    private static final String BUILD_PLAN_ID_KEY = "BUILD_PLAN_ID";

    private static final String BUILD_STATUS_KEY = "BUILD_STATUS";

    private static final String BUILD_LOGS_FILE_KEY = "BUILD_LOGS_FILE";

    private static final String NOTIFICATION_URL_KEY = "NOTIFICATION_URL";

    private static final String NOTIFICATION_SECRET_KEY = "NOTIFICATION_SECRET";

    private EnvReader() {
    }

    private static String getEnv(String key) {
        LOGGER.debug("Getting environment variable: " + key);
        final String value = System.getenv(key);
        if (value == null) {
            LOGGER.fatal("Environment variable {} is unset", key);
            throw new IllegalStateException("Environment variable " + key + " is unset");
        }
        return System.getenv(key);
    }

    public static String getTestResultsDir() {
        return getEnv(TEST_RESULTS_DIR_KEY);
    }

    public static String getCustomFeedbackDir() {
        return getEnv(CUSTOM_FEEDBACK_DIR_KEY);
    }

    public static String getTestGitHashKey() {
        return getEnv(TEST_GIT_HASH_KEY);
    }

    public static String getTestGitRepositorySlug() {
        return getEnv(TEST_GIT_REPOSITORY_SLUG_KEY);
    }

    public static String getTestGitBranch() {
        return getEnv(TEST_GIT_BRANCH_KEY);
    }

    public static String getSubmissionGitHashKey() {
        return getEnv(SUBMISSION_GIT_HASH_KEY);
    }

    public static String getSubmissionGitRepositorySlug() {
        return getEnv(SUBMISSION_GIT_REPOSITORY_SLUG_KEY);
    }

    public static String getSubmissionGitBranch() {
        return getEnv(SUBMISSION_GIT_BRANCH_KEY);
    }

    public static String getBuildPlanIdKey() {
        return getEnv(BUILD_PLAN_ID_KEY);
    }

    public static String getBuildStatus() {
        return getEnv(BUILD_STATUS_KEY);
    }

    public static String getBuildLogsFile() {
        return getEnv(BUILD_LOGS_FILE_KEY);
    }

    public static String getNotificationUrl() {
        return getEnv(NOTIFICATION_URL_KEY);
    }

    public static String getNotificationSecret() {
        return getEnv(NOTIFICATION_SECRET_KEY);
    }
}
