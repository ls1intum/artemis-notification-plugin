package de.tum.cit.ase.artemis_notification_plugin;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import de.tum.cit.ase.artemis_notification_plugin.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomFeedbackParser {

    private static final Logger LOGGER = LogManager.getLogger(CustomFeedbackParser.class);

    private CustomFeedbackParser() {
    }

    /**
     * Reads all {@link CustomFeedback} JSONs from the directory and converts them into a {@link Testsuite}
     *
     * @param resultsDir the directory to read the feedback files from.
     * @return a {@link Testsuite} if at least one valid {@link CustomFeedback} has been found, empty otherwise.
     * @throws IOException          if the resultsDir is not readable.
     */
    public static Optional<Testsuite> extractCustomFeedbacks(Path resultsDir) throws IOException {
        LOGGER.debug("Extracting custom feedbacks from {}", resultsDir);

        if (Files.notExists(resultsDir)) {
            LOGGER.warn("The results directory for custom feedback does not exist: {}", resultsDir);
            return Optional.empty();
        }

         var gson = new Gson();
         List<CustomFeedback> feedbacks ;
try (Stream<Path> stream = Files.walk(resultsDir, 1)) {
feedbacks =stream
                .filter(path -> path.endsWith(".json"))
                .map((Function<Path, Optional<CustomFeedback>>) feedbackFile -> {
                    try {
                         CustomFeedback feedback = gson.fromJson(new String(Files.readAllBytes(feedbackFile), UTF_8), CustomFeedback.class);
                        if (feedback.getMessage() != null && feedback.getMessage().trim().isEmpty()) {
                            feedback.setMessage(null);
                        }
                        validateCustomFeedback(feedbackFile.getFileName().toString(), feedback);
                        return Optional.of(feedback);
                    }
                    catch (IOException e) {
                        // TODO: taskListener.error(e.getMessage(), e);
                        LOGGER.error(e.getMessage(), e);
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());}

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
    private static void validateCustomFeedback( String fileName,  CustomFeedback feedback) throws InvalidPropertiesFormatException {
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
    private static Testsuite customFeedbacksToTestSuite( List<CustomFeedback> feedbacks) {
         var suite = new Testsuite();
        suite.setName("customFeedbackReports");

         List<TestCase> testCases = feedbacks.stream().map(feedback -> {
             var testCase = new TestCase();
            testCase.setName(feedback.getName());

            if (feedback.isSuccessful()) {
                 var successInfo = new SuccessInfo();
                successInfo.setMessage(feedback.getMessage());
                 List<SuccessInfo> infos = new ArrayList<>();
                infos.add(successInfo);

                testCase.setSuccessInfos(infos);
            }
            else {
                 var failure = new Failure();
                failure.setMessage(feedback.getMessage());
                 List<Failure> failures = new ArrayList<>();
                failures.add(failure);

                testCase.setFailures(failures);
            }

            return testCase;
        }).collect(Collectors.toList());

        suite.setTestCases(testCases);

        return suite;
    }
}
