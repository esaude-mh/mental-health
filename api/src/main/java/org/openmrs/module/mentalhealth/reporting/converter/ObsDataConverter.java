package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.mentalhealth.calculation.MhConfigCalculations;
import org.openmrs.module.mentalhealth.utils.MhConstants;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.reporting.data.converter.DataConverter;

public class ObsDataConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        /*Obs value = (Obs) obj;
        if(value.getValueNumeric() != null){
            return value.getValueNumeric();
        }
        else if(value.getValueCoded() != null){
            return getValueCodedValues(value.getValueCoded());
        }
        else if(value.getValueText() != null){
            return value.getValueText();
        }*/

        return obj;
    }

    @Override
    public Class<?> getInputDataType() {
        return Obs.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }

    private String getValueCodedValues(Concept c){
        String value = "";
        if(c.equals(c.equals(MhReportUtils.getConcept(MhConstants.YES)))){
            value = "Y";
        }
        else if(c.equals(c.equals(MhReportUtils.getConcept(MhConstants.NO)))){
            value = "N";
        }
        else if(c.equals(c.equals(MhReportUtils.getConcept(MhConstants.NOT_PROVIDED)))){
            value = "NP";
        }
            return value;
    }
}
