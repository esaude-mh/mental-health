package org.openmrs.module.mentalhealth.reporting.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;

/**
 * Convenience cohort definition for date obs where we want
 * to filter by date values in a given range. This class differs from DateObsCohortDefinition in
 * that the ${onOrAfter} and ${onOrBefore} apply to the obs value rather than the obs date
 */
public class DateObsValueBetweenCohortDefinition extends BaseObsCohortDefinition {

    /**
     * Default constructor
     */
    public DateObsValueBetweenCohortDefinition() {
    }
}

