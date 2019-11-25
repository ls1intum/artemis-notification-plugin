package io.jenkins.plugins.sample.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "testcase")
public class Testcase {
    private String name;
    private String classname;
    private double time;
    private List<Failure> failures;
    private List<Error> errors;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public List<Failure> getFailures() {
        return failures;
    }

    @XmlElement(name = "failure")
    public void setFailures(List<Failure> failures) {
        this.failures = failures;
    }

    public List<Error> getErrors() {
        return errors;
    }

    @XmlElement(name = "error")
    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
