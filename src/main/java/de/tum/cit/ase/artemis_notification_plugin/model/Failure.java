package de.tum.cit.ase.artemis_notification_plugin.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "failure")
public class Failure {

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

    public void setMessageWithStackTrace(String messageWithStackTrace) {
        this.messageWithStackTrace = messageWithStackTrace;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
