package de.tum.in.ase.notification;

import de.tum.in.ase.notification.exception.TestParsingException;
import de.tum.in.ase.notification.model.Commit;
import de.tum.in.ase.notification.model.TestResults;
import de.tum.in.ase.notification.model.Testsuite;
import org.apache.http.HttpException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.tum.in.ase.notification.EnvReader.*;

public class NotificationPlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    public NotificationPlugin() {
        Configurator.setRootLevel(Level.ALL);
    }

    public static void main(String[] args) throws IOException {
        new NotificationPlugin().run();
    }

    public void run() throws IOException {
        final Path currentPath = Paths.get(".").toAbsolutePath().normalize();
        final Path testResultsDir = currentPath.resolve(Paths.get(getTestResultsDir()));
        final Path customFeedbackDir = currentPath.resolve(Paths.get(getCustomFeedbackDir()));
        final Path buildLogsFile = currentPath.resolve(Paths.get(getBuildLogsFile()));

        final List<Testsuite> testReports = extractTestResults(testResultsDir);
        final Optional<Testsuite> customFeedback = CustomFeedbackParser.extractCustomFeedbacks(customFeedbackDir);
        customFeedback.ifPresent(testReports::add);

        final TestResults results = combineTestResults(testReports);

        results.setIsBuildSuccessful(getBuildStatus().equalsIgnoreCase("success"));

        results.setLogs(extractLogs(buildLogsFile));

        try {
            HttpHelper.postTestResults(results, getNotificationUrl(), getNotificationSecret());
        } catch (HttpException e) {
            // TODO: taskListener.error(e.getMessage(), e);
            LOGGER.error(e.getMessage(), e);
        }
    }

    private List<Testsuite> extractTestResults(Path resultsDir) throws IOException {
        LOGGER.debug("Extracting test results from " + resultsDir);
        return Files.walk(resultsDir, 1)
                // TODO: .filter(path -> path.endsWith(".xml"))
                .filter(Files::isRegularFile)
                .map(report -> {
                    try {
                        final JAXBContext context = JAXBContext.newInstance(Testsuite.class);
                        final Unmarshaller unmarshaller = context.createUnmarshaller();
                        Testsuite testsuite = (Testsuite) unmarshaller.unmarshal(Files.newInputStream(report));
                        System.out.println(testsuite.flatten());
                        return testsuite.flatten();
                    } catch (JAXBException | IOException e) {
                        // TODO: taskListener.error(e.getMessage(), e);
                        LOGGER.error(e.getMessage(), e);
                        throw new TestParsingException(e);
                    }
                }).collect(Collectors.toList());
    }

    private TestResults combineTestResults(List<Testsuite> testReports) {
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
        results.setCommits(getCommits());
        results.setFullName(getBuildPlanIdKey());
        results.setErrors(errors);
        results.setSkipped(skipped);
        results.setSuccessful(successful);
        results.setFailures(failed);
        return results;
    }

    private List<Commit> getCommits() {
        final Commit testCommit = new Commit();
        testCommit.setHash(getTestGitHashKey());
        testCommit.setBranchName(getTestGitBranch());
        testCommit.setRepositorySlug(getTestGitRepositorySlug());

        final Commit submissionCommit = new Commit();
        submissionCommit.setHash(getSubmissionGitHashKey());
        submissionCommit.setBranchName(getSubmissionGitBranch());
        submissionCommit.setRepositorySlug(getSubmissionGitRepositorySlug());

        final List<Commit> commits = new ArrayList<>();
        commits.add(testCommit);
        commits.add(submissionCommit);
        return commits;
    }

    private List<String> extractLogs(Path buildLogsFile) throws IOException {
        return Files.readAllLines(buildLogsFile);
    }
}
