package de.tum.in.www1.jenkins.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;

import de.tum.in.ase.parser.domain.Report;

import hudson.FilePath;
import hudson.model.TaskListener;

public class StaticCodeAnalysisParser {

    /**
     * Parses all reports supported by the static code analysis parser.
     *
     * @param taskListener for logging errors in case of parsing errors.
     * @param staticCodeAnalysisResultDir the FilePath to the directory that contains static code analysis results.
     * @return a list of parsed reports.
     */
    public static List<Report> parseReports(TaskListener taskListener, FilePath staticCodeAnalysisResultDir) {
        // Static code analysis parsing must not crash the sending of notifications under any circumstances
        try {
            List<Report> reports = new ArrayList<>();
            for (FilePath filePath : staticCodeAnalysisResultDir.list()) {
                // Only parse files that are supported by the static code analysis parser.
                if (!StaticCodeAnalysisTool.isStaticCodeAnalysisReportsFile(filePath.getName())) {
                    continue;
                }

                // Try to parse each report separately. Failure parsing one report should not effect the parsing of others
                Optional<Report> report = parseReport(taskListener, filePath);
                report.ifPresent(reports::add);
            }
            return reports;
        }
        catch (Exception e) {
            taskListener.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Parses a static code analysis report from the specified file path.
     *
     * @param taskListener for logging errors in case of parsing errors.
     * @param reportFilePath the FilePath of the report file.
     * @return The parsed report or empty.
     * @throws IOException if something went wrong file parsing the report.
     * @throws InterruptedException if something went wrong file parsing the report.
     */
    private static Optional<Report> parseReport(TaskListener taskListener, FilePath reportFilePath) throws IOException, InterruptedException {
        // File operations in Jenkins should be done through a FileCallable class which allows operations on main instance and worker agents.
        StaticCodeAnalysisParserFileCallable staticCodeAnalysisParserFileCallable = new StaticCodeAnalysisParserFileCallable(taskListener);

        // Parse the report using act().
        String reportJson = reportFilePath.act(staticCodeAnalysisParserFileCallable);
        if (reportJson == null || reportJson.isEmpty()) {
            return Optional.empty();
        }

        Gson gson = new Gson();
        Report report = gson.fromJson(reportJson, Report.class);
        return Optional.of(report);
    }
}
