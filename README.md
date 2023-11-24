# Artemis Notification Plugin

Based on the [Jenkins Server Notification Plugin](https://github.com/ls1intum/jenkins-server-notification-plugin), this plugin can send a notification with the test results to the [Artemis](https://github.com/ls1intum/Artemis) server.

As part of the thesis [_Automatic Correction of Programming Exercises with Artemis & GitLab CI_](https://ase.cit.tum.de/theses/automatic-correction-of-programming-exercises-with-artemis-and-gitlab-ci/) this plugin was conceptualized and implemented.
The main goal of this redevelopment of the server notification plugin is to make it more flexible by removing the dependency on a specific Continuous Integration System (CIS).
Instead, the plugin aims to be used with any CIS, providing different adapters for getting the required build information from the specific CIS.

## System Design

![Class Diagram](docs/class-diagram.drawio.png "Class Diagram")

This plugin contains the common feedback extraction and sending logic used by all CIS plugins.
The required configuration is passed as a `Context` object.

Depending on the CIS, the plugin creates the `Context` object differently.
Thus, we use the Strategy Pattern to dynamically bind a suitable `ContextFactory` for the CIS.
For GitLab CI, environment variables are used to create the `Context` object.

The implementation of the Jenkins and Bamboo part still needs to be done.

## Which results will get sent?
The plugin will collect and merge all JUnit formatted test results in the results directory during the execution.
Please make sure to generate all JUnit XML files under this directory before. Then, the plugin will merge the files and post the results to the Artemis endpoint.

The JUnit format is limited to allow messages only for failing test cases.
In order to circumvent this limitation, the plugin will additionally read all JSON files in the custom feedback directory and integrate them into the report sent to Artemis.
The JSON files must have the correct file ending `.json` and have to be in the format:
```json
{
  "name": "string",
  "successful": boolean,
  "message": "string"
}
```
with the attributes:
* `name`: The name of the test case as it will be shown in Artemis. It has to be unique for this exercise and non-null.
* `successful`: Indicates if the test case execution for this submission should be marked as successful or failed.
* `message`: The message to be shown as additional information to the student. The attribute is required for non-successful feedback, and it is optional otherwise.

## Usage
Before executing the application via the command line interface (`CLIPlugin`), make sure that the following environment variables are set:
* `ARTEMIS_TEST_RESULTS_DIR`: The directory where the JUnit XML files are located.
* `ARTEMIS_CUSTOM_FEEDBACK_DIR`: The directory where the custom feedback JSON files are located.
* `ARTEMIS_TEST_GIT_HASH`: The git hash of the commit of the test repository.
* `ARTEMIS_TEST_GIT_REPOSITORY_SLUG`: The slug of the test repository.
* `ARTEMIS_TEST_GIT_BRANCH`: The branch of the test repository.
* `ARTEMIS_SUBMISSION_GIT_HASH`: The git hash of the commit of the submission repository (the student repositories, the template, or the solution repository) that was tested.
* `ARTEMIS_SUBMISSION_GIT_REPOSITORY_SLUG`: The slug of the submission repository.
* `ARTEMIS_SUBMISSION_GIT_BRANCH`: The branch of the submission repository.
* `ARTEMIS_BUILD_PLAN_ID`: The id of the build plan. This is used to identify the participation.
* `ARTEMIS_BUILD_STATUS`: The status of the build. If the build was successful, this must be set to `success`.
* `ARTEMIS_BUILD_LOGS_FILE`: The path to the log file of the build.
* `ARTEMIS_NOTIFICATION_URL`: The URL of the server to which the notification should be sent.
* `ARTEMIS_NOTIFICATION_SECRET`: The secret used to authenticate the notification request.


To execute the application, run `gradle run`.
The Docker image can be used to run the application in a container (e.g. in a CIS).
**Information:** On startup of the container, the application is not automatically executed since some CIS require a script to be executed. Thus, please use `java -jar /notification-plugin/artemis-notification-plugin.jar` to execute it.
