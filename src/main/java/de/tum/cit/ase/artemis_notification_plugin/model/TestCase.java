package de.tum.cit.ase.artemis_notification_plugin.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "testcase")
public class TestCase {

    private String name;

    @XmlAttribute
    private String classname;

    @XmlAttribute
    private double time;

    private List<Failure> failures;

    private List<Error> errors;

    private List<SuccessInfo> successInfos;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassname() {
        return classname;
    }

    public double getTime() {
        return time;
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

    public List<SuccessInfo> getSuccessInfos() {
        return successInfos;
    }

    @XmlElement(name = "successInfo")
    public void setSuccessInfos(List<SuccessInfo> successInfos) {
        this.successInfos = successInfos;
    }
}
