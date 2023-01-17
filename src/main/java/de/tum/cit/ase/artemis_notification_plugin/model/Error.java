package de.tum.cit.ase.artemis_notification_plugin.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "error")
@XmlType(name = "error", namespace = "http://www.w3.org/2001/XMLSchema-instance")
public class Error {

    @XmlAttribute
    private String message;

    @XmlValue
    private String messageWithStackTrace;

    @XmlAttribute
    private String type;

    public String getMessage() {
        return message;
    }

    public String getMessageWithStackTrace() {
        return messageWithStackTrace;
    }

    public String getType() {
        return type;
    }
}
