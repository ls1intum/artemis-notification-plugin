package de.tum.in.ase.notification.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<Testsuite> testSuites;

    private List<TestCase> testCases;

    private static String buildTestCaseName(final Testsuite suite, final TestCase testCase) {
        if (suite.name == null) {
            return testCase.getName();
        }
        else {
            return suite.name + '.' + testCase.getName();
        }
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTime() {
        return time;
    }

    public int getErrors() {
        return errors;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getFailures() {
        return failures;
    }

    public int getTests() {
        return tests;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    @XmlElement(name = "testcase")
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public List<Testsuite> getTestSuites() {
        return testSuites;
    }

    @XmlElement(name = "testsuite")
    public void setTestSuites(List<Testsuite> testSuites) {
        this.testSuites = testSuites;
    }

    /**
     * Combines the test suite and all child test suites recursively into a single test suite.
     *
     * @return This test suite with all children suites merged into it.
     */
    public Testsuite flatten() {
        initTestCaseCounts();

        if (testSuites != null) {
            testSuites.stream().map(Testsuite::flatten).forEach(this::addOther);

            // make sure the testSuites are null, as they should not be exported in the JSON back to Artemis
            testSuites = null;
        }

        return this;
    }

    /**
     * Initializes the number of test cases, errors, and failures from the list of test cases.
     * <p>
     * Not all test runners add the number of errors and failures to the suite information.
     * Therefore, consistency has to be ensured manually here.
     */
    private void initTestCaseCounts() {
        errors = 0;
        failures = 0;
        tests = 0;

        if (testCases != null) {
            updateTestCaseCounts(testCases);
        }
    }

    /**
     * Merges the other test suite into this one.
     *
     * @param other Some other test suite.
     */
    private void addOther(final Testsuite other) {
        if (testCases == null) {
            testCases = new ArrayList<>();
        }

        if (other.testCases != null) {
            List<TestCase> otherTestCases = other.testCases.stream().peek(testCase -> testCase.setName(buildTestCaseName(other, testCase))).collect(Collectors.toList());

            skipped += other.skipped;
            updateTestCaseCounts(otherTestCases);
            testCases.addAll(otherTestCases);
        }
    }

    /**
     * Updates the test case count, number of errors, and number of failures in this test suite.
     * <p>
     * Does <em>not</em> add the test cases themselves to the list of {@link #getTestCases()}.
     *
     * @param additionalTestCases The test case that will be separately added to the test cases of this suite.
     */
    private void updateTestCaseCounts(final List<TestCase> additionalTestCases) {
        tests += additionalTestCases.size();

        for (final TestCase testCase : additionalTestCases) {
            if (testCase.getFailures() != null) {
                failures += 1;
            }
            if (testCase.getErrors() != null) {
                errors += 1;
            }
        }
    }
}
