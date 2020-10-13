package de.tum.in.www1.jenkins.notifications.model;

import de.tum.in.ase.parser.domain.Report;
import hudson.model.Action;
import hudson.model.Api;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ExportedBean
public class TestResults implements Action {
    private String fullName;
    private int successful;
    private int skipped;
    private int errors;
    private int failures;
    private List<Commit> commits;
    private List<Testsuite> results;
    private List<Report> staticCodeAnalysisResults;
    private String runDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    public Api getApi() {
        return new Api(this);
    }

    @Exported
    public String getRunDate() {
        return runDate;
    }

    @Exported
    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }

    @Exported
    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    @Exported
    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    @Exported
    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }

    @Exported
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Exported
    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    @Exported
    public List<Testsuite> getResults() {
        return results;
    }

    public void setResults(List<Testsuite> results) {
        this.results = results;
    }

    @Exported
    public List<Report> getStaticCodeAnalysisResults() {
        return staticCodeAnalysisResults;
    }

    public void setStaticCodeAnalysisResults(List<Report> staticCodeAnalysisResults) {
        this.staticCodeAnalysisResults = staticCodeAnalysisResults;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "testResults";
    }
}
