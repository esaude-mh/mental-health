package org.openmrs.module.mentalhealth.reports;

import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.mentalhealth.MhDataExportManager;
import org.openmrs.module.mentalhealth.metadata.MentalHealthEncounterTypes;
import org.openmrs.module.mentalhealth.metadata.MentalHealthPatientIdentifierTypes;
import org.openmrs.module.mentalhealth.reporting.converter.EncounterDataConverter;
import org.openmrs.module.mentalhealth.reporting.converter.ObsDataConverter;
import org.openmrs.module.mentalhealth.utils.DataFactory;
import org.openmrs.module.mentalhealth.utils.MhConstants;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Properties;

@Component
public class DateExportReport extends MhDataExportManager {

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private DataFactory factory;

    public static final String REPORT_NAME = "Data Export Report";
    @Override
    public String getExcelDesignUuid() {
        return "9c417ffa-5cec-11e9-a14f-8fd3ba460a1b";
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "export.xls");
        rd.setName(REPORT_NAME);
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:2,dataset:export");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "81611bb4-5cec-11e9-bb70-8f0b57bcc5a3";
    }

    @Override
    public String getName() {
        return REPORT_NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        //rd.
        rd.addDataSetDefinition("export", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1.4";
    }

    private DataSetDefinition dataSetDefinition() {
        EncounterDataSetDefinition dsd = new EncounterDataSetDefinition();
        dsd.setName("export");
        dsd.addSortCriteria("visit", SortCriteria.SortDirection.ASC);
        //dsd.addRowFilter(getFilter(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid()), "");

        //identifier
        PatientIdentifierType mhNumber = MetadataUtils.existing(PatientIdentifierType.class, MentalHealthPatientIdentifierTypes.MH_NID.uuid());
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(mhNumber.getName(), mhNumber), identifierFormatter);

        dsd.addColumn("NID", identifierDef, "");
        dsd.addColumn("Nome", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("province", factory.getPreferredAddress("stateProvince"), "");
        dsd.addColumn("district", factory.getPreferredAddress("countyDistrict"), "");
        dsd.addColumn("consult1", firstDateOfConsultations(), "", new EncounterDataConverter());
        dsd.addColumn("Gender", builtInPatientData.getGender(), "");
        dsd.addColumn("age", builtInPatientData.getAgeAtEnd(), "");
        dsd.addColumn("dob", builtInPatientData.getBirthdate(), "");
        dsd.addColumn("diagnosisI", diagnosis(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.DIAGNOSIS_CONCEPT), "", new ObsDataConverter());
        dsd.addColumn("other1", other1(), "", new ObsDataConverter());
        dsd.addColumn("visit", followUp(), "", new EncounterDataConverter());
        dsd.addColumn("diagnosisF", diagnosis(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.DIAGNOSIS_CONCEPT), "", new ObsDataConverter());



        return dsd;
    }

    private DataDefinition firstDateOfConsultations(){
        EncountersForPatientDataDefinition dsd = new EncountersForPatientDataDefinition();
        dsd.setWhich(TimeQualifier.FIRST);
        dsd.setTypes(Arrays.asList(MetadataUtils.existing(EncounterType.class, MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid())));
        return dsd;
    }

    private EncounterQuery getFilter(String uuid1, String uuid2) {
        int initial = MetadataUtils.existing(EncounterType.class, uuid1).getEncounterTypeId();
        int follow = MetadataUtils.existing(EncounterType.class, uuid2).getEncounterTypeId();
        SqlEncounterQuery encounter = new SqlEncounterQuery();
        encounter.setName("Has encounter");
        encounter.setQuery("SELECT pp.patient_id FROM patient pp INNER JOIN encounter e ON pp.patient_id=e.patient_id WHERE pp.voided=0 AND e.voided=0 AND e.encounter_type IN("+initial+","+follow+")");
        return encounter;
    }

    private DataDefinition diagnosis(String whichForm, String conceptUuid) {
        ObsForPersonDataDefinition obs = new ObsForPersonDataDefinition();
        obs.setName("diagnosis");
        obs.setEncounterTypeList(Arrays.asList(MetadataUtils.existing(EncounterType.class, whichForm)));
        obs.setQuestion(MhReportUtils.getConcept(conceptUuid));
        obs.setWhich(TimeQualifier.LAST);
        return obs;
    }

    private DataDefinition other1() {
        ObsForPersonDataDefinition obs = new ObsForPersonDataDefinition();
        obs.setName("other1");
        obs.setQuestion(MhReportUtils.getConcept(MhConstants.OTHER_1));
        obs.setWhich(TimeQualifier.LAST);
        return obs;
    }
    private DataDefinition followUp() {
        EncountersForPatientDataDefinition dsd = new EncountersForPatientDataDefinition();
        dsd.setWhich(TimeQualifier.ANY);
        dsd.setTypes(Arrays.asList(MetadataUtils.existing(EncounterType.class, MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid())));
        return dsd;
    }
}