package io.jenkins.plugins.sample.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@ExportedBean(defaultVisibility = 3)
@XmlRootElement(name = "testcase")
public class TestCase {
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String classname;
    @XmlAttribute
    private double time;
    private List<Failure> failures;
    private List<Error> errors;

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public String getClassname() {
        return classname;
    }

    @Exported
    public double getTime() {
        return time;
    }

    @Exported
    public List<Failure> getFailures() {
        return failures;
    }

    @XmlElement(name = "failure")
    public void setFailures(List<Failure> failures) {
        this.failures = failures;
    }

    @Exported
    public List<Error> getErrors() {
        return errors;
    }

    @XmlElement(name = "error")
    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
