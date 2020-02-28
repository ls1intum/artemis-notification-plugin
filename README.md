# Jenkins Test Result Notifications
This plugin sends a notification containing the test results of a Jenkins build to any server.

Notifications get securely posted to a given URL.

## Which results will get published?
The plugin will collect and merge **all** JUnit formatted test results in the _results_ directory in a build.\
So, you have to copy or generate all JUnit XML files under this directory, the plugin will take care of merging
multiple files and posting the results to the specified endpoint.

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
      "repositorySlug": "test-repository"
    },
    {
      "hash": "3843ea74cf837a9374de324ca374988aa8347ffe8",
      "repositorySlug": "other-test-repository"
    }
  ],
  "errors": 0,
  "failures": 4,
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
              "type": "java.lang.AssertionError"
            }
          ],
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
              "type": "java.lang.AssertionError"
            }
          ],
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
              "type": "java.lang.AssertionError"
            }
          ],
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
              "type": "java.lang.AssertionError"
            }
          ],
          "name": "testMethods[ThermoAdapter]",
          "time": 0.078
        }
      ],
      "tests": 1,
      "time": 0.078
    }
  ],
  "runDate": "2020-02-19T14:42:42.084Z[Etc/UTC]",
  "skipped": 0,
  "successful": 0
}
```
