package org.openmrs.module.mentalhealth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.obs.definition.ObsDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.ObsDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.BaseReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.util.ReportUtil;

public abstract class MhReportManager extends BaseReportManager {

    protected final Log log = LogFactory.getLog(getClass());

    protected void addColumn(EncounterDataSetDefinition dsd, String columnName, EncounterDataDefinition edd) {
        dsd.addColumn(columnName, edd, ObjectUtil.toString(Mapped.straightThroughMappings(edd), "=", ","));
    }

    protected void addColumn(ObsDataSetDefinition dsd, String columnName, ObsDataDefinition odd) {
        dsd.addColumn(columnName, odd, ObjectUtil.toString(Mapped.straightThroughMappings(odd), "=", ","));
    }

    protected void addColumn(PatientDataSetDefinition dsd, String columnName, PatientDataDefinition pdd) {
        dsd.addColumn(columnName, pdd, Mapped.straightThroughMappings(pdd));
    }

    protected ReportDesign createExcelTemplateDesign(String reportDesignUuid, ReportDefinition reportDefinition,
                                                     String templatePath) {
        String resourcePath = ReportUtil.getPackageAsPath(getClass()) + "/" + templatePath;
        return ReportManagerUtil.createExcelTemplateDesign(reportDesignUuid, reportDefinition, resourcePath);
    }

    protected ReportDesign createExcelDesign(String reportDesignUuid, ReportDefinition reportDefinition) {
        return MhReportUtils.createExcelDesign(reportDesignUuid, reportDefinition);
    }
}
