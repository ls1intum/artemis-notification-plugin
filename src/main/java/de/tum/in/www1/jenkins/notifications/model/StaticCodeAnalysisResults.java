package de.tum.in.www1.jenkins.notifications.model;

import de.tum.in.ase.parser.domain.Report;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

public class StaticCodeAnalysisResults {

    private List<Report> reports;

    public List<Report> getFailures() {
        return reports;
    }

    public void setFailures(List<Failure> failures) {
        this.reports = reports;
    }
}
