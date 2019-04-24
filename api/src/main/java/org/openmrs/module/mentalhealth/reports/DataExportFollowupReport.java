package org.openmrs.module.mentalhealth.reports;

import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.mentalhealth.MhDataExportManager;
import org.openmrs.module.mentalhealth.Queries.FollowupQueries;
import org.openmrs.module.mentalhealth.metadata.MentalHealthEncounterTypes;
import org.openmrs.module.mentalhealth.metadata.MentalHealthPatientIdentifierTypes;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class DataExportFollowupReport extends MhDataExportManager {

    public static final String REPORT_NAME = "Data Export Follow Up Form";

    @Override
    public String getExcelDesignUuid() {
        return "c8314ace-66be-11e9-9155-fb657abf2477";
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "export2.xls");
        rd.setName(REPORT_NAME);
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:3,dataset:export2");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "ba371b2e-66be-11e9-a7e5-afdd9f3a3c11";
    }

    @Override
    public String getName() {
        return  REPORT_NAME;
    }

    @Override
    public String getDescription() {
        return "Follow up form details";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        //rd.
        rd.addDataSetDefinition("export2", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.0.2";
    }

    private DataSetDefinition dataSetDefinition() {
        PatientIdentifierType identifierType = MetadataUtils.existing(PatientIdentifierType.class, MentalHealthPatientIdentifierTypes.MH_NID.uuid());
        EncounterType encounterType = MetadataUtils.existing(EncounterType.class, MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid());
        SqlDataSetDefinition sql = new SqlDataSetDefinition();
        sql.setSqlQuery(FollowupQueries.getQuery(identifierType.getPatientIdentifierTypeId(), encounterType.getEncounterTypeId()));
        return sql;
    }
}