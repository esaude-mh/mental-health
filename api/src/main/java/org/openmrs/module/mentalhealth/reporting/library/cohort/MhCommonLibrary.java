package org.openmrs.module.mentalhealth.reporting.library.cohort;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * Library of common cohort definitions
 */
@Component
public class MhCommonLibrary {

    /**
     * Patients who are female
     *
     * @return the cohort definition
     */
    public CohortDefinition females() {
        GenderCohortDefinition cd = new GenderCohortDefinition();
        cd.setName("females");
        cd.setFemaleIncluded(true);
        return cd;
    }

    /**
     * Patients who are male
     *
     * @return the cohort definition
     */
    public CohortDefinition males() {
        GenderCohortDefinition cd = new GenderCohortDefinition();
        cd.setName("males");
        cd.setMaleIncluded(true);
        return cd;
    }

    /**
     * Patients who have an encounter between ${onOrAfter} and ${onOrBefore}
     *
     * @param types the encounter types
     * @return the cohort definition
     */
    public CohortDefinition hasEncounter(EncounterType... types) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setName("has encounter between dates");
        cd.setTimeQualifier(TimeQualifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));
        if (types.length > 0) {
            cd.setEncounterTypeList(Arrays.asList(types));
        }
        return cd;
    }

    /**
     * Patients who were enrolled on the given programs between ${enrolledOnOrAfter} and
     * ${enrolledOnOrBefore}
     *
     * @param programs the programs
     * @return the cohort definition
     */
    public CohortDefinition enrolled(Program... programs) {
        ProgramEnrollmentCohortDefinition cd = new ProgramEnrollmentCohortDefinition();
        cd.setName("enrolled in program between dates");
        cd.addParameter(new Parameter("enrolledOnOrAfter", "After Date", Date.class));
        cd.addParameter(new Parameter("enrolledOnOrBefore", "Before Date", Date.class));
        if (programs.length > 0) {
            cd.setPrograms(Arrays.asList(programs));
        }
        return cd;
    }

    /**
     *
     */
    public CohortDefinition allPatients(){
        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
        sqlCohortDefinition.setName("All Patients");
        sqlCohortDefinition.addParameter(new Parameter("startDate", "Data Inicial Inclusão", Date.class));
        sqlCohortDefinition.addParameter(new Parameter("endDate", "Data Final Inclusão", Date.class));
        sqlCohortDefinition.addParameter(new Parameter("location", "Unidade Sanitária", Date.class));
        sqlCohortDefinition.addParameter(new Parameter("concept", "Concept", Concept.class));
        sqlCohortDefinition.addParameter(new Parameter("months", "Months", Integer.class));
        sqlCohortDefinition.addParameter(new Parameter("alive", "Alive", Boolean.class));
        sqlCohortDefinition.addParameter(new Parameter("encounter", "Encounter Type", EncounterType.class));
        sqlCohortDefinition.setQuery("SELECT person_id FROM obs where concept_id=:concept");
        return sqlCohortDefinition;
    }
}
