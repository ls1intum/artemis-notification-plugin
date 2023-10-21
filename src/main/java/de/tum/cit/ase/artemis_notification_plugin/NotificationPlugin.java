package de.tum.cit.ase.artemis_notification_plugin;

import com.google.gson.Gson;
import de.tum.cit.ase.artemis_notification_plugin.configuration.Context;
import de.tum.cit.ase.artemis_notification_plugin.configuration.ContextFactory;
import de.tum.cit.ase.artemis_notification_plugin.exception.TestParsingException;
import de.tum.cit.ase.artemis_notification_plugin.model.Commit;
import de.tum.cit.ase.artemis_notification_plugin.model.TestResults;
import de.tum.cit.ase.artemis_notification_plugin.model.Testsuite;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract class for the notification plugin. All other subtypes of the plugin, e.g. the CLI plugin, should extend this as this class provides the basic functionality.
 */
public abstract class NotificationPlugin {

    private static final Logger LOGGER = LogManager.getLogger(NotificationPlugin.class);

    protected final ContextFactory contextFactory;

    protected NotificationPlugin(ContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    /**
     * Runs the plugin. This includes reading the test results, parsing them and sending them to the Artemis server.
     *
     * @param context the context containing the configuration.
     * @throws IOException if one of the result directories is not readable.
     */
    public void run(Context context) throws IOException {
        final Path currentPath = Paths.get(".").toAbsolutePath().normalize();
        final Path testResultsDir = currentPath.resolve(Paths.get(context.getTestResultsDir()));
        final Path customFeedbackDir = currentPath.resolve(Paths.get(context.getCustomFeedbackDir()));
        final Path buildLogsFile = currentPath.resolve(Paths.get(context.getBuildLogsFile()));

        final List<Testsuite> testReports = extractTestResults(testResultsDir);
        final Optional<Testsuite> customFeedback = CustomFeedbackParser.extractCustomFeedbacks(customFeedbackDir);
        customFeedback.ifPresent(testReports::add);

        final TestResults results = combineTestResults(testReports, context);

        results.setIsBuildSuccessful(context.isBuildSuccessful());

        results.setLogs(extractLogs(buildLogsFile));

        postResult(results, context);
    }

    /**
     * Sends the test results to the Artemis server.
     *
     * @param results the test results to send.
     * @param context the context containing the configuration.
     */
    public void postResult(TestResults results, Context context) {
        try {
            final String body = new Gson().toJson(results);

            LOGGER.debug("Posting test results to {}: {}", context.getNotificationUrl(), body);

            final HttpResponse response = Request.Post(context.getNotificationUrl())
                    .addHeader("Authorization", context.getNotificationSecret())
                    .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
                    .bodyString(body, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new HttpException(String.format("Sending test results failed (%d) with response: %s",
                        response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset())));
            }
        }
        catch (HttpException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Provides the context, which contains the configuration for the plugin.
     *
     * @return the context object.
     */
    public abstract Context provideContext();

    private List<Testsuite> extractTestResults(Path resultsDir) throws IOException {
        LOGGER.debug("Extracting test results from {}", resultsDir);

        if (Files.notExists(resultsDir)) {
            LOGGER.warn("The custom feedback directory does not exist: {}", resultsDir);
            return new ArrayList<>();
        }

        return Files.walk(resultsDir, 1)
                .filter(path -> path.toString().endsWith(".xml"))
                .filter(Files::isRegularFile)
                .map(this::extractSingleReportFromFile).collect(Collectors.toList());
    }

    private Testsuite extractSingleReportFromFile(Path report) {
        try {
            final JAXBContext context = JAXBContext.newInstance(Testsuite.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            Testsuite testsuite = (Testsuite) unmarshaller.unmarshal(Files.newInputStream(report));
            return testsuite.flatten();
        }
        catch (JAXBException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new TestParsingException(e);
        }
    }

    private TestResults combineTestResults(List<Testsuite> testReports, Context context) {
        LOGGER.debug("Combining test results");

        int skipped = 0;
        int failed = 0;
        int successful = 0;
        int errors = 0;
        for (final Testsuite suite : testReports) {
            successful += suite.getTests() - (suite.getErrors() + suite.getFailures() + suite.getSkipped());
            failed += suite.getFailures();
            errors += suite.getErrors();
            skipped += suite.getSkipped();
        }

        final TestResults results = new TestResults();
        results.setResults(testReports);
        // results.setStaticCodeAnalysisReports(staticCodeAnalysisReports);
        results.setCommits(getCommits(context));
        results.setFullName(context.getBuildPlanId());
        results.setErrors(errors);
        results.setSkipped(skipped);
        results.setSuccessful(successful);
        results.setFailures(failed);
        return results;
    }

    private List<Commit> getCommits(Context context) {
        final Commit testCommit = new Commit();
        testCommit.setHash(context.getTestGitHash());
        testCommit.setBranchName(context.getTestGitBranch());
        testCommit.setRepositorySlug(context.getTestGitRepositorySlug());

        final Commit submissionCommit = new Commit();
        submissionCommit.setHash(context.getSubmissionGitHash());
        submissionCommit.setBranchName(context.getSubmissionGitBranch());
        submissionCommit.setRepositorySlug(context.getSubmissionGitRepositorySlug());

        final List<Commit> commits = new ArrayList<>();
        commits.add(testCommit);
        commits.add(submissionCommit);
        return commits;
    }

    private List<String> extractLogs(Path buildLogsFile) throws IOException {
        return Files.readAllLines(buildLogsFile);
    }
}
