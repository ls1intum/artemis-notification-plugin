package de.tum.in.ase.notification.model;

import de.tum.in.ase.parser.domain.Report;

import javax.annotation.CheckForNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TestResults {

    private String fullName;

    private int successful;

    private int skipped;

    private int errors;

    private int failures;

    private List<Commit> commits;

    private List<Testsuite> results;

    private List<Report> staticCodeAnalysisReports;

    private final String runDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    private List<String> logs;

    private boolean isBuildSuccessful;

    public String getRunDate() {
        return runDate;
    }

    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public List<Testsuite> getResults() {
        return results;
    }

    public void setResults(List<Testsuite> results) {
        this.results = results;
    }

    public List<Report> getStaticCodeAnalysisReports() {
        return staticCodeAnalysisReports;
    }

    public void setStaticCodeAnalysisReports(List<Report> staticCodeAnalysisReports) {
        this.staticCodeAnalysisReports = staticCodeAnalysisReports;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public boolean isBuildSuccessful() {
        return this.isBuildSuccessful;
    }

    public void setIsBuildSuccessful(boolean buildSuccessful) {
        this.isBuildSuccessful = buildSuccessful;
    }
}
