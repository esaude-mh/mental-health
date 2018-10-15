package org.openmrs.module.mentalhealth.reporting.library.dimension;

import org.openmrs.module.mentalhealth.reporting.library.cohort.MhCommonLibrary;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.mentalhealth.utils.MhReportUtils.map;

/**
 * Contains common dimesions that could be tied to a report definition
 */
@Component
public class MhCommonDimension {

    @Autowired
    private MhCommonLibrary commonLibrary;

    /**
     * Gender dimension
     *
     * @return the dimension
     */
    public CohortDefinitionDimension gender() {
        CohortDefinitionDimension dim = new CohortDefinitionDimension();
        dim.setName("gender");
        dim.addCohortDefinition("M", map(commonLibrary.males()));
        dim.addCohortDefinition("F", map(commonLibrary.females()));
        return dim;
    }
}
