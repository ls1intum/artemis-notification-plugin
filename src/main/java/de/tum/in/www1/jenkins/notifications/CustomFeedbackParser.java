package de.tum.in.www1.jenkins.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.gson.Gson;

import de.tum.in.www1.jenkins.notifications.model.*;

import hudson.FilePath;
import hudson.model.TaskListener;

public class CustomFeedbackParser {

    private CustomFeedbackParser() {
    }

    /**
     * Reads all {@link CustomFeedback} JSONs from the directory and converts them into a {@link Testsuite}
     *
     * @param taskListener needed to provide useful error messages in the Jenkins log.
     * @param resultsDir the directory to read the feedback files from.
     * @return a {@link Testsuite} if at least one valid {@link CustomFeedback} has been found, empty otherwise.
     * @throws IOException if the resultsDir is not readable.
     * @throws InterruptedException if the resultsDir is not readable.
     */
    public static Optional<Testsuite> extractCustomFeedbacks(@Nonnull TaskListener taskListener, FilePath resultsDir) throws IOException, InterruptedException {
        final Gson gson = new Gson();
        final List<CustomFeedback> feedbacks = resultsDir.list()
                .stream()
                .filter(path -> path.getName().endsWith(".json"))
                .map((Function<FilePath, Optional<CustomFeedback>>) feedbackFile -> {
                    try {
                        final CustomFeedback feedback = gson.fromJson(feedbackFile.readToString(), CustomFeedback.class);
                        if (feedback.getMessage() != null && feedback.getMessage().trim().isEmpty()) {
                            feedback.setMessage(null);
                        }
                        validateCustomFeedback(feedbackFile.getName(), feedback);
                        return Optional.of(feedback);
                    }
                    catch (IOException | InterruptedException e) {
                        taskListener.error(e.getMessage(), e);
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (feedbacks.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(customFeedbacksToTestSuite(feedbacks));
        }
    }

    /**
     * Checks that the custom feedback has a valid format
     * <p>
     * A custom feedback has to have a non-empty, non only-whitespace name to be able to identify it in Artemis.
     * If it is not successful, there has to be a message explaining a reason why this is the case.
     *
     * @param fileName where the custom feedback was read from.
     * @param feedback the custom feedback to validate.
     * @throws InvalidPropertiesFormatException if one of the invariants described above does not hold.
     */
    private static void validateCustomFeedback(final String fileName, final CustomFeedback feedback) throws InvalidPropertiesFormatException {
        if (feedback.getName() == null || feedback.getName().trim().isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("Custom feedback from file %s needs to have a name attribute.", fileName));
        }
        if (!feedback.isSuccessful() && feedback.getMessage() == null) {
            throw new InvalidPropertiesFormatException(String.format("Custom non-success feedback from file %s needs to have a message", fileName));
        }
    }

    /**
     * Convert the feedbacks into {@link TestCase}s and wrap them in a {@link Testsuite}
     *
     * @param feedbacks the list of parsed custom feedbacks to wrap
     * @return a Testsuite in the same format as used by JUnit reports
     */
    private static Testsuite customFeedbacksToTestSuite(final List<CustomFeedback> feedbacks) {
        final Testsuite suite = new Testsuite();
        suite.setName("customFeedbackReports");

        final List<TestCase> testCases = feedbacks.stream().map(feedback -> {
            final TestCase testCase = new TestCase();
            testCase.setName(feedback.getName());

            if (feedback.isSuccessful()) {
                final SuccessInfo successInfo = new SuccessInfo();
                successInfo.setMessage(feedback.getMessage());
                final List<SuccessInfo> infos = new ArrayList<>();
                infos.add(successInfo);

                testCase.setSuccessInfos(infos);
            }
            else {
                final Failure failure = new Failure();
                failure.setMessage(feedback.getMessage());
                final List<Failure> failures = new ArrayList<>();
                failures.add(failure);

                testCase.setFailures(failures);
            }

            return testCase;
        }).collect(Collectors.toList());

        suite.setTestCases(testCases);

        return suite;
    }
}
