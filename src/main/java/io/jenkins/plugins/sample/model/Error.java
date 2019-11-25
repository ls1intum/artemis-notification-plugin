package io.jenkins.plugins.sample.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@ExportedBean(defaultVisibility = 4)
@XmlRootElement(name = "error")
public class Error {
    @XmlAttribute
    private String message;
    @XmlAttribute
    private String type;

    @Exported
    public String getMessage() {
        return message;
    }

    @Exported
    public String getType() {
        return type;
    }
}
