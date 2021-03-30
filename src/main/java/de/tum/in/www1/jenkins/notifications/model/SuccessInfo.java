package de.tum.in.www1.jenkins.notifications.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 4)
@XmlRootElement(name = "successInfo")
public class SuccessInfo {

    private String message;

    private String messageWithStackTrace;

    private String type;

    @XmlAttribute
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlValue
    public String getMessageWithStackTrace() {
        return messageWithStackTrace;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }
}
