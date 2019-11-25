package io.jenkins.plugins.sample.model;

import hudson.model.Action;
import hudson.model.Api;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import java.util.List;

@ExportedBean
public class TestResults implements Action {
    private List<String> commitHashes;
    private List<Testsuite> results;

    public Api getApi() {
        return new Api(this);
    }

    @Exported
    public List<String> getCommitHashes() {
        return commitHashes;
    }

    public void setCommitHashes(List<String> commitHashes) {
        this.commitHashes = commitHashes;
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
