package org.openmrs.module.mentalhealth;

import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

import java.util.ArrayList;
import java.util.List;

public abstract class MhDataExportManager extends MhReportManager {

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    public abstract String getExcelDesignUuid();

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        ReportDesign excelDesign = createExcelDesign(getExcelDesignUuid(), reportDefinition);
        l.add(excelDesign);
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report
     * design by adding properties and other metadata to the report design
     *
     * @return The report design
     */
    public abstract ReportDesign buildReportDesign(ReportDefinition reportDefinition);
}
