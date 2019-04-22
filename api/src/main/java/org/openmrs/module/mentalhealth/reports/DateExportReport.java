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
import org.openmrs.module.mentalhealth.reporting.converter.ObsDateDataConverter;
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
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
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
        //Initial form details
        dsd.addColumn("Homeless", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.HOMELESS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Civil Status", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.CIVIL_STATUS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Number of Children", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.NUMBER_OF_CHILDREN, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Do you agree with Community Follow-up", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.COMMUNITY_FOLLO_UP, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Accompanying person name", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ACCOMPANYING_PERSON_NAME, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Accompanying person telephone", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ACCOMPANYING_PERSON_TELEPHONE, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Accompanying person relationship", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ACCOMPANYING_PERSON_RELATIONSHIP, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Main Complaint", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.MAIN_COMPLAINT, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Reason for Consultation", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.REASON_FOR_CONSULTATION, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("History of Current Illness", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.HISTORY_OF_CURRENT_ILLINESS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Previous Personal History", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.PREVIOUS_PERSONAL_HISTORY, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Epilepsy", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.EPILEPSY, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Mental Illness", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.MENTAL_ILLINESS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Mental Illness specified", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.MENTAL_ILLINESS_YES, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Suicidal", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.SUICIDAL, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Alcoholism", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ALCOHOLISM, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Other Substances", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_SUBSTANCE, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Other Substances specified", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_SUBSTANCE_YES, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Other chronic diseases (comorbidity)", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_CHRONIC_DISEASES, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Other chronic diseases (Specify)", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_CHRONIC_DISEASE_SPECIFY, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("TB Test +", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.TB_TEST, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("TB Test Diagnosed", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.TB_TEST_DIAGNOSED, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("HIV Test", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.HIV_TEST, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("HIV Date Diagnosed", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.HIV_DIAGNOSED_DATE, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("HIV start date of ART", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.HIV_START_DATE, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Alcohol use", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ALCOHOL_USE, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Alcohol use Description", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ALCOHOL_USE_DESC, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Use of other drugs", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.USE_OF_OTHER_DRUGS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Use of other drugs Description", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.USE_OF_OTHER_DRUGS_DESC, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Use of traditional medicine", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.USE_TRADITIOANL_MEDICINE, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Use of traditional medicine description", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.USE_TRADITIONAL_MEDICINE_DESC, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("PHQ-9 Score (DEPRESSION)", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.PHQ9, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Score GAD 7 (ANXIETY)", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.GAD7, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("AUDIT Score (ALCOHOL)", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.AUDIT, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("WHODAS Score (DISABILITY)", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.WHODAS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("A. Appearance and Posture", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.APPEARENCE_POSTURE, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("B1. Synthesis Functions", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.SYNTHESIS_FUNCTIONS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("B2. Sensory Perception", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.SENSIORY_PERCEPTION, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("B3. Thought / Judgment Critical", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.THOUGHTS_JUDGMENT_CRITICAL, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("C. Humor", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.HUMOR, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("D. Conduct", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.CONDUCT, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("F. Relationship Functions", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.RELATIONSHIP_FUNCTIONS, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Depressive Disorder", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.DEPRESSIVE_DISORDER, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Depressive Disorder Date", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.DEPRESSIVE_DISORDER, TimeQualifier.LAST), "", new ObsDateDataConverter());
        dsd.addColumn("Anxiety Disorder", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ANXIETY_DISORDER, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Anxiety Disorder Date", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ANXIETY_DISORDER, TimeQualifier.LAST), "", new ObsDateDataConverter());
        dsd.addColumn("Adjustment Disorder", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ADJUSTMENT_DISORDER, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Adjustment Disorder Date", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.ADJUSTMENT_DISORDER, TimeQualifier.LAST), "", new ObsDateDataConverter());
        dsd.addColumn("Bipolar Affective Disorder", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.BIPOLAR_AFFECTIVE_DISORDER, TimeQualifier.LAST), "", new ObsDataConverter());
        dsd.addColumn("Bipolar Affective Disorder Date", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.BIPOLAR_AFFECTIVE_DISORDER, TimeQualifier.LAST), "", new ObsDateDataConverter());






        dsd.addColumn("Diagnosticos", getObs(MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid(), MhConstants.DIAGNOSIS_CONCEPT, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("OTHER INFORMATION FROM PROCESSO", other1(), "", new ObsDataConverter());
        dsd.addColumn("Visita", followUp(), "", new EncounterDatetimeForVisitConverter());
        dsd.addColumn("Diagnostico", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.DIAGNOSIS_CONCEPT,  TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("Data da consulta", followUp(), "", new EncounterDatetimeForVisitConverter());
        dsd.addColumn("WHODAS", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.WHODAS, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("PHQ9", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.PHQ9, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("GAD7", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.GAD7, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("AUDIT", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.AUDIT, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("Gravidade", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.GRAVITY, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("Suicidas", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.SUICIDAL_F2, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("Other Concerns", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_CONCERNS, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("Other Concerns Yes", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.OTHER_CONCERNS_YES, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("Alcohol and Drugs", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.CONSERNS_WITH_ALCOHOL_DRUGS, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("Pregnant", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.PATIENT_PREGNANT, TimeQualifier.ANY), "", new ObsDataConverter());
        dsd.addColumn("OnFP", getObs(MentalHealthEncounterTypes.FOLLOW_UP_ENCOUNTER_TYPE.uuid(), MhConstants.PATIENT_ON_FP, TimeQualifier.ANY), "", new ObsDataConverter());


        return dsd;
    }

    private DataDefinition firstDateOfConsultations(){
        EncountersForPatientDataDefinition dsd = new EncountersForPatientDataDefinition();
        dsd.setWhich(TimeQualifier.LAST);
        dsd.setTypes(Arrays.asList(MetadataUtils.existing(EncounterType.class, MentalHealthEncounterTypes.INITIAL_ENCOUNTER_TYPE.uuid())));
        return dsd;
    }

    private DataDefinition getObs(String whichForm, String conceptUuid, TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition obs = new ObsForPersonDataDefinition();
        obs.setName("getObs");
        obs.setEncounterTypeList(Arrays.asList(MetadataUtils.existing(EncounterType.class, whichForm)));
        obs.setQuestion(MhReportUtils.getConcept(conceptUuid));
        obs.setWhich(timeQualifier);
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