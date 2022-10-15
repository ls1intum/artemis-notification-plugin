package de.tum.in.ase.notification.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

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
