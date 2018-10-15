package org.openmrs.module.mentalhealth.reporting.library.indicator;

import org.openmrs.module.mentalhealth.reporting.library.cohort.MhCommonLibrary;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.mentalhealth.utils.MhEmrReportingUtils.cohortIndicator;
import static org.openmrs.module.mentalhealth.utils.MhReportUtils.map;

/**
 * All indicators require parameters ${startDate}, ${endDate} and ${location}
 */
@Component
public class MhSampleIndicators {

    @Autowired
    private MhCommonLibrary mhCommonLibrary;

    //populate your indicators here either as a whole number or a fraction

    /**
     *
     * @return
     */
    public CohortIndicator allPatients() {
        return cohortIndicator("Sample",  map(mhCommonLibrary.allPatients(), "startDate=${startDate},endDate=${endDate},location=${location}"));
    }

}
