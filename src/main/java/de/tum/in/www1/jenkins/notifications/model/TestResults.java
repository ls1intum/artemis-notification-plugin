package de.tum.in.www1.jenkins.notifications.model;

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
    private List<Commit> commits;
    private List<Testsuite> results;
    private String runDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE);

    public Api getApi() {
        return new Api(this);
    }

    @Exported
    public String getRunDate() {
        return runDate;
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
