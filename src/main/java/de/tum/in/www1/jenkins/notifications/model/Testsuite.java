package de.tum.in.www1.jenkins.notifications.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@ExportedBean(defaultVisibility = 2)
@XmlRootElement(name = "testsuite")
public class Testsuite {
    private String name;
    @XmlAttribute
    private double time;
    @XmlAttribute
    private int errors;
    @XmlAttribute
    private int skipped;
    @XmlAttribute
    private int failures;
    @XmlAttribute
    private int tests;
    private List<TestCase> testCases;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Exported
    public double getTime() {
        return time;
    }

    @Exported
    public int getErrors() {
        return errors;
    }

    @Exported
    public int getSkipped() {
        return skipped;
    }

    @Exported
    public int getFailures() {
        return failures;
    }

    @Exported
    public int getTests() {
        return tests;
    }

    @Exported
    public List<TestCase> getTestCases() {
        return testCases;
    }

    @XmlElement(name = "testcase")
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}
