# Artemis Notification Plugin

Based on the [Jenkins Server Notification Plugin](https://github.com/ls1intum/jenkins-server-notification-plugin), this plugin can send a notification with the test results to the [Artemis](https://github.com/ls1intum/Artemis) server.

In difference to the Jenkins and [Bamboo Server Notification Plugin](https://github.com/ls1intum/bamboo-server-notification-plugin), this tool does not depend on any particular Continuous Integration System (CIS), but can be used with any CIS.
Therefore, no information can be fetched from the API of the CIS and all required configurations must be set as an environment variable.

## Which results will get published?
The plugin will collect and merge **all** JUnit formatted test results in the _results_ directory in a build.\
So, you have to copy or generate all JUnit XML files under this directory, the plugin will take care of merging
multiple files and posting the results to the specified endpoint.

The JUnit format is limited to allow messages only for failing test cases.
To circumvent this limitation and allow custom tools to easily send additional feedbacks to Artemis another approach is possible.
To achieve this, a directory _customFeedbacks_ has to be created.
Then this plugin will read all JSON files written to this directory and integrate them into the report sent to Artemis.
The JSON files must have the correct file ending `.json` and have to be in the format
```json
{
  "name": "string",
  "successful": boolean,
  "message": "string"
}
```
where the attributes are:
* `name`: This is the name of the test case as it will be shown for example on the ‘Configure Grading’ page.
  It should therefore have a for this exercise uniquely identifiable name and **has to be non-null**.
* `successful`: Indicates if the test case execution for this submission should be marked as successful or failed.
* `message`: The message shown as additional information to the student.
  **Required for non-successful feedback**, optional otherwise.

## Usage
Before executing the application make sure that the following environment variables are set:
* `TEST_RESULTS_DIR`: The directory where the JUnit XML files are located.
* `CUSTOM_FEEDBACK_DIR`: The directory where the custom feedback JSON files are located.
* `TEST_GIT_HASH`: The git hash of the commit of the test repository.
* `TEST_GIT_REPOSITORY_SLUG`: The slug of the test repository.
* `TEST_GIT_BRANCH`: The branch of the test repository.
* `SUBMISSION_GIT_HASH`: The git hash of the commit of the submission repository (the student repositories, the template or the solution repository) that was tested.
* `SUBMISSION_GIT_REPOSITORY_SLUG`: The slug of the submission repository.
* `SUBMISSION_GIT_BRANCH`: The branch of the submission repository.
* `BUILD_PLAN_ID`: The id of the build plan. This is used to identify the participation.
* `BUILD_STATUS`: The status of the build. If the build was successful, this must be set to `success`.
* `BUILD_LOGS_FILE`: The path to the log file of the build.
* `NOTIFICATION_URL`: The URL of the server to which the notification should be sent.
* `NOTIFICATION_SECRET`: The secret used to authenticate the notification request.


To execute the application, run `gradle run`.
The Docker image can be used to run the application in a container (e.g. in a CIS).
**Information:** On startup of the container, the application is not automatically executed, since some CIS require a script to be executed. Thus, please use `gradle run` to execute it.
