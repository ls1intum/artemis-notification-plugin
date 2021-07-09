package de.tum.in.www1.jenkins.notifications;

import java.io.File;

import org.jenkinsci.remoting.RoleChecker;

import de.tum.in.ase.parser.ReportParser;
import de.tum.in.ase.parser.exception.ParserException;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

/**
 * Custom FilePath.FileCallable implementation which enables the parsing of static code analysis
 * reports on the main Jenkins instance and its remote agents.
 *
 * Jenkins recommends this approach instead of working with File objects. For more information see.
 * https://javadoc.jenkins-ci.org/hudson/FilePath.html
 *
 * Note that we use String instead of returning Report because the return type must be serializable.
 */
public final class StaticCodeAnalysisParserFileCallable implements FilePath.FileCallable<String> {

    private final TaskListener taskListener;

    public StaticCodeAnalysisParserFileCallable(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    public String invoke(File file, VirtualChannel virtualChannel) {
        try {
            ReportParser reportParser = new ReportParser();
            return reportParser.transformToJSONReport(file);
        }
        catch (ParserException e) {
            taskListener.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        // Not needed
    }
}
