package org.openmrs.module.mentalhealth.reports;

import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.module.mentalhealth.MhDataExportManager;
import org.openmrs.module.mentalhealth.metadata.MentalHealthEncounterTypes;
import org.openmrs.module.mentalhealth.metadata.MentalHealthPatientIdentifierTypes;
import org.openmrs.module.mentalhealth.reporting.converter.EncounterDataConverter;
import org.openmrs.module.mentalhealth.reporting.converter.EncounterDatetimeForVisitConverter;
import org.openmrs.module.mentalhealth.reporting.converter.ObsDataConverter;
import org.openmrs.module.mentalhealth.utils.DataFactory;
import org.openmrs.module.mentalhealth.utils.MhConstants;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
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
        props.put("repeatingSections", "sheet:1,row:3,dataset:export");
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
        return "0.1.5";
    }

    private DataSetDefinition dataSetDefinition() {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName("export");
        dsd.addSortCriteria("consultation-date", SortCriteria.SortDirection.ASC);
        dsd.addRowFilter(getEnrolledInMentalProgram(), "");

        //identifier
        PatientIdentifierType mhNumber = MetadataUtils.existing(PatientIdentifierType.class, MentalHealthPatientIdentifierTypes.MH_NID.uuid());
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(mhNumber.getName(), mhNumber), identifierFormatter);

        dsd.addColumn("NID Paciente", identifierDef, "");
        dsd.addColumn("Nome Paciente", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Provincia", factory.getPreferredAddress("stateProvince"), "");
        dsd.addColumn("Distrito", factory.getPreferredAddress("countyDistrict"), "");
        dsd.addColumn("Data da 1ere consulta", firstDateOfConsultations(), "", new EncounterDataConverter());
        dsd.addColumn("Sexo", builtInPatientData.getGender(), "");
        dsd.addColumn("Idade", builtInPatientData.getAgeAtEnd(), "");
        dsd.addColumn("Data de nascimento (DN)", builtInPatientData.getBirthdate(), "");
        dsd.addColumn("Diagnosticos", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.DIAGNOSIS_CONCEPT), "", new ObsDataConverter());
        dsd.addColumn("OTHER INFORMATION FROM PROCESSO", other1(), "", new ObsDataConverter());
        dsd.addColumn("Visita", followUp(), "", new EncounterDatetimeForVisitConverter());
        dsd.addColumn("Diagnostico", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.DIAGNOSIS_CONCEPT), "", new ObsDataConverter());
        dsd.addColumn("Data da consulta", followUp(), "", new EncounterDatetimeForVisitConverter());
        dsd.addColumn("WHODAS", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.WHODAS), "", new ObsDataConverter());
        dsd.addColumn("PHQ9", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.PHQ9), "", new ObsDataConverter());
        dsd.addColumn("GAD7", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.GAD7), "", new ObsDataConverter());
        dsd.addColumn("AUDIT", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.AUDIT), "", new ObsDataConverter());
        dsd.addColumn("Gravidade", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.GRAVITY), "", new ObsDataConverter());
        dsd.addColumn("Suicidas", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.SUICIDAL_F2), "", new ObsDataConverter());
        dsd.addColumn("other-concerns", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_CONCERNS), "", new ObsDataConverter());
        dsd.addColumn("other-concerns-yes", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_CONCERNS_YES), "", new ObsDataConverter());
        dsd.addColumn("alcohol-drugs", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.CONSERNS_WITH_ALCOHOL_DRUGS), "", new ObsDataConverter());
        dsd.addColumn("pregnant", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.PATIENT_PREGNANT), "", new ObsDataConverter());
        dsd.addColumn("onFP", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.PATIENT_ON_FP), "", new ObsDataConverter());


        return dsd;
    }

    private DataDefinition firstDateOfConsultations(){
        EncountersForPatientDataDefinition dsd = new EncountersForPatientDataDefinition();
        dsd.setWhich(TimeQualifier.LAST);
        dsd.setTypes(Arrays.asList(MetadataUtils.existing(EncounterType.class, MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid())));
        return dsd;
    }

    private DataDefinition getObs(String whichForm, String conceptUuid) {
        ObsForPersonDataDefinition obs = new ObsForPersonDataDefinition();
        obs.setName("getObs");
        obs.setEncounterTypeList(Arrays.asList(MetadataUtils.existing(EncounterType.class, whichForm)));
        obs.setQuestion(MhReportUtils.getConcept(conceptUuid));
        obs.setWhich(TimeQualifier.ANY);
        return obs;
    }

    private DataDefinition other1() {
        ObsForPersonDataDefinition obs = new ObsForPersonDataDefinition();
        obs.setName("other1");
        obs.setQuestion(MhReportUtils.getConcept(MhConstants.OTHER_F1));
        obs.setWhich(TimeQualifier.LAST);
        return obs;
    }
    private DataDefinition followUp() {
        EncountersForPatientDataDefinition dsd = new EncountersForPatientDataDefinition();
        dsd.setWhich(TimeQualifier.ANY);
        dsd.setTypes(Arrays.asList(MetadataUtils.existing(EncounterType.class, MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid())));
        return dsd;
    }
    private CohortDefinition getEnrolledInMentalProgram() {
        ProgramEnrollmentCohortDefinition cd = new ProgramEnrollmentCohortDefinition();
        cd.setPrograms(Arrays.asList(MetadataUtils.existing(Program.class, "cb1e24be-aa03-11e8-a5b5-f34e18407f07")));
        return cd;
    }
}