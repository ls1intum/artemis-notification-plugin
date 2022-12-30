package de.tum.in.ase.notification.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnvReader extends ContextFactory {

    private static final Logger LOGGER = LogManager.getLogger(EnvReader.class);

    private static final String PREFIX = "ARTEMIS_";

    private static final String TEST_RESULTS_DIR_KEY = PREFIX + "TEST_RESULTS_DIR";

    private static final String CUSTOM_FEEDBACK_DIR_KEY = PREFIX + "CUSTOM_FEEDBACK_DIR";

    private static final String TEST_GIT_HASH_KEY = PREFIX + "TEST_GIT_HASH";

    private static final String TEST_GIT_REPOSITORY_SLUG_KEY = PREFIX + "TEST_GIT_REPOSITORY_SLUG";

    private static final String TEST_GIT_BRANCH_KEY = PREFIX + "TEST_GIT_BRANCH";

    private static final String SUBMISSION_GIT_HASH_KEY = PREFIX + "SUBMISSION_GIT_HASH";

    private static final String SUBMISSION_GIT_REPOSITORY_SLUG_KEY = PREFIX + "SUBMISSION_GIT_REPOSITORY_SLUG";

    private static final String SUBMISSION_GIT_BRANCH_KEY = PREFIX + "SUBMISSION_GIT_BRANCH";

    private static final String BUILD_PLAN_ID_KEY = PREFIX + "BUILD_PLAN_ID";

    private static final String BUILD_STATUS_KEY = PREFIX + "BUILD_STATUS";

    private static final String BUILD_LOGS_FILE_KEY = PREFIX + "BUILD_LOGS_FILE";

    private static final String NOTIFICATION_URL_KEY = PREFIX + "NOTIFICATION_URL";

    private static final String NOTIFICATION_SECRET_KEY = PREFIX + "NOTIFICATION_SECRET";

    @Override
    public Context buildContext() {
        return new Context(
                getEnvVariable(TEST_RESULTS_DIR_KEY),
                getEnvVariable(CUSTOM_FEEDBACK_DIR_KEY),
                getEnvVariable(TEST_GIT_HASH_KEY),
                getEnvVariable(TEST_GIT_REPOSITORY_SLUG_KEY),
                getEnvVariable(TEST_GIT_BRANCH_KEY),
                getEnvVariable(SUBMISSION_GIT_HASH_KEY),
                getEnvVariable(SUBMISSION_GIT_REPOSITORY_SLUG_KEY),
                getEnvVariable(SUBMISSION_GIT_BRANCH_KEY),
                getEnvVariable(BUILD_PLAN_ID_KEY),
                getEnvVariable(BUILD_STATUS_KEY),
                getEnvVariable(BUILD_LOGS_FILE_KEY),
                getEnvVariable(NOTIFICATION_URL_KEY),
                getEnvVariable(NOTIFICATION_SECRET_KEY));
    }

    /**
     * Get the value of an environment variable.
     *
     * @param key The key of the environment variable.
     * @return The value of the environment variable.
     */
    public String getEnvVariable(String key) {
        LOGGER.debug("Getting environment variable: {}", key);
        final String value = System.getenv(key);
        if (value == null) {
            LOGGER.fatal("Environment variable {} is unset", key);
            throw new IllegalStateException("Environment variable " + key + " is unset");
        }
        return value;
    }
}
