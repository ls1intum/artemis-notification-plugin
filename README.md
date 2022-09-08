# Jenkins Test Result Notifications
This plugin sends a notification containing the test results of a Jenkins build to any server.

Notifications get securely posted to a given URL.

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
  **Required for non-sucessful feedback**, optional otherwise.

Additionally, the plugin will parse and send reports created by static code analysis. The tools Spotbugs, Checkstyle and PMD are currently supported.
The plugin only considers XML reports in the directory _staticCodeAnalysisReports_.

## Activate for a build
1. Under _Add post-build-action_ choose "Send Test Results Notification"
2. Input any URL to post a notification to as the _Notification Target_
3. For the token you can generate a secret text as a Jenkins credential, which will get sent in the _Authorization_ header of the POST to the URL.

You can then find any test results for any build under https://url.to.your.jenkins.instance/path/to/your/build/testResults/api/json.

The same JSON will get posted to the URL you specified.

## Compiling from Source
* To compile the plugin run `mvn clean install`
* To test the plugin in a temporary development Jenkins instance run `mvn hpi:run`\
Jenkins will be accessible (including the automatically installed plugin) under http://localhost:8080/jenkins

## Feedback? Questions?
Email: krusche[at]in[dot]tum[dot]de

## Sample Notification Body

```json
{
  "_class": "de.tum.in.www1.jenkins.notifications.model.TestResults",
  "commits": [
    {
      "hash": "413aa48eed159aa9753fa559f28b48ac91734c2cf",
      "repositorySlug": "test-repository",
      "branchName":"main"
    },
    {
      "hash": "3843ea74cf837a9374de324ca374988aa8347ffe8",
      "repositorySlug": "other-test-repository",
      "branchName":"main"
    }
  ],
  "errors": 0,
  "failures": 5,
  "fullName": "SOME-FOLDER » SOME-JOB #42",
  "results": [
    {
      "errors": 0,
      "failures": 1,
      "name": "de.tum.in.ase.FunctionalTest",
      "skipped": 0,
      "testCases": [
        {
          "classname": "de.tum.in.ase.FunctionalTest",
          "errors": null,
          "failures": [
            {
              "message": "Problem: the class 'ThermoAdapter' was not found within the submission. Please implement it properly.",
              // Only reported with JUnit 4
              "messageWithStackTrace": "org.opentest4j.AssertionFailedError: The class \u0027MergeSort\u0027 does not implement the interface \u0027SortStrategy\u0027 as expected. Implement the interface and its methods.\n\tat de.test.ClassTest.testClass(ClassTest.java:128)\n\tat de.test.ClassTest.lambda$generateTestsForAllClasses$0(ClassTest.java:53)\n",
              "type": "java.lang.AssertionError"
            }
          ],
          "successInfos": null,
          "name": "testAdapterValue",
          "time": 0.011
        }
      ],
      "tests": 1,
      "time": 0.011
    },
    {
      "errors": 0,
      "failures": 1,
      "name": "de.tum.in.ase.AttributeTest",
      "skipped": 0,
      "testCases": [
        {
          "classname": "de.tum.in.ase.AttributeTest",
          "errors": null,
          "failures": [
            {
              "message": "The exercise expects a class with the name ThermoAdapter in the package de.tum.in.ase. You did not implement the class in the exercise.",
              // Only reported with JUnit 4
              "messageWithStackTrace": "org.opentest4j.AssertionFailedError: The class \u0027MergeSort\u0027 does not implement the interface \u0027SortStrategy\u0027 as expected. Implement the interface and its methods.\n\tat de.test.ClassTest.testClass(ClassTest.java:128)\n\tat de.test.ClassTest.lambda$generateTestsForAllClasses$0(ClassTest.java:53)\n",
              "type": "java.lang.AssertionError"
            }
          ],
          "successInfos": null,
          "name": "testAttributes[ThermoAdapter]",
          "time": 0.008
        }
      ],
      "tests": 1,
      "time": 0.008
    },
    {
      "errors": 0,
      "failures": 1,
      "name": "de.tum.in.ase.ClassTest",
      "skipped": 0,
      "testCases": [
        {
          "classname": "de.tum.in.ase.ClassTest",
          "errors": null,
          "failures": [
            {
              "message": "The exercise expects a class with the name ThermoAdapter in the package de.tum.in.ase You did not implement the class in the exercise.",
              // Only reported with JUnit 4
              "messageWithStackTrace": "org.opentest4j.AssertionFailedError: The class \u0027MergeSort\u0027 does not implement the interface \u0027SortStrategy\u0027 as expected. Implement the interface and its methods.\n\tat de.test.ClassTest.testClass(ClassTest.java:128)\n\tat de.test.ClassTest.lambda$generateTestsForAllClasses$0(ClassTest.java:53)\n",
              "type": "java.lang.AssertionError"
            }
          ],
          "successInfos": null,
          "name": "testClass[ThermoAdapter]",
          "time": 0.014
        }
      ],
      "tests": 1,
      "time": 0.014
    },
    {
      "errors": 0,
      "failures": 1,
      "name": "de.tum.in.ase.MethodTest",
      "skipped": 0,
      "testCases": [
        {
          "classname": "de.tum.in.ase.MethodTest",
          "errors": null,
          "failures": [
            {
              "message": "The exercise expects a class with the name ThermoAdapter in the package de.tum.in.ase You did not implement the class in the exercise.",
              // Only reported with JUnit 4
              "messageWithStackTrace": "org.opentest4j.AssertionFailedError: The class \u0027MergeSort\u0027 does not implement the interface \u0027SortStrategy\u0027 as expected. Implement the interface and its methods.\n\tat de.test.ClassTest.testClass(ClassTest.java:128)\n\tat de.test.ClassTest.lambda$generateTestsForAllClasses$0(ClassTest.java:53)\n",
              "type": "java.lang.AssertionError"
            }
          ],
          "successInfos": null,
          "name": "testMethods[ThermoAdapter]",
          "time": 0.078
        }
      ],
      "tests": 1,
      "time": 0.078
    },
    {
      "errors": 0,
      "failures": 1,
      "name": "customFeedbackReports",
      "skipped": 0,
      "testCases": [
        {
          "errors": null,
          "failures": null,
          "successInfos": [
            {
              "message": "A custom message for a successful test case as defined in the JSON."
            }
          ],
          "name": "customFeedbackName001"
        },
        {
          "errors": null,
          "failures": [
            {
              "message": "A custom message for a non-succesful test case as defined in the JSON."
            }
          ],
          "successInfos": null,
          "name": "customFeedbackName002"
        }
      ],
      "tests": 2
    }
  ],
  "staticCodeAnalysisReports": [
    {
      "tool": "SPOTBUGS",
      "issues": [
        {
          "filePath": "/buildDir/testExercise/assignment/src/com/ls1/staticCodeAnalysis/App.java",
          "startLine": 16,
          "endLine": 16,
          "rule": "ES_COMPARING_PARAMETER_STRING_WITH_EQ",
          "category": "BAD_PRACTICE",
          "message": "Comparison of String parameter using == or != in com.stefan.staticCodeAnalysis.App.equalString(String)",
          "priority": "1"
        }
      ]
    },
    {
      "tool": "CHECKSTYLE",
      "issues": [
        {
          "filePath": "/buildDir/testExercise/assignment/src/com/ls1/staticCodeAnalysis/App.java",
          "startLine": 7,
          "endLine": 7,
          "startColumn": 1,
          "endColumn": 1,
          "rule": "HideUtilityClassConstructorCheck",
          "category": "design",
          "message": "Utility classes should not have a public or default constructor.",
          "priority": "error"
        }
      ]
    },
    {
      "tool": "PMD",
      "issues": [
        {
          "filePath": "/buildDir/testExercise/assignment/src/com/ls1/staticCodeAnalysis/App.java",
          "startLine": 10,
          "endLine": 10,
          "startColumn": 16,
          "endColumn": 16,
          "rule": "UnusedLocalVariable",
          "category": "Best Practices",
          "message": "Avoid unused local variables such as 'b'.",
          "priority": "3"
        }
      ]
    }
  ],
  "runDate": "2020-02-19T14:42:42.084Z[Etc/UTC]",
  "skipped": 0,
  "successful": 0
}
```
