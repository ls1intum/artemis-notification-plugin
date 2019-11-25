package io.jenkins.plugins.sample.model;

import java.util.List;

public class TestResults {
    private List<String> commitHashes;
    private List<Testsuite> results;

    public List<String> getCommitHashes() {
        return commitHashes;
    }

    public void setCommitHashes(List<String> commitHashes) {
        this.commitHashes = commitHashes;
    }

    public List<Testsuite> getResults() {
        return results;
    }

    public void setResults(List<Testsuite> results) {
        this.results = results;
    }
}
