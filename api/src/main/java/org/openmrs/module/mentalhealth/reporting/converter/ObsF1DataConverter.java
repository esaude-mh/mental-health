package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.mentalhealth.utils.MhConstants;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ObsF1DataConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Obs obs = (Obs) obj;
        if(obs.getValueCoded() != null) {
            return getValueCodedValues(obs.getValueCoded());
        }
        else if(obs.getValueDatetime() != null) {
            return formatDate(obs.getValueDatetime());
        }
        else if(obs.getValueNumeric() != null) {
            return obs.getValueNumeric();
        }
        else if(obs.getValueText() != null){
            return obs.getValueText();
        }

        return null;
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
        if(c.equals(MhReportUtils.getConcept(MhConstants.YES))){
            value = "S";
        }
        else if(c.equals(MhReportUtils.getConcept(MhConstants.NO))){
            value = "N";
        }
        else if(c.equals(MhReportUtils.getConcept(MhConstants.NOT_PROVIDED))){
            value = "NP";
        }
        else {
            value = c.getName().getName();
        }
        return value;
    }

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null?"":dateFormatter.format(date);
    }
}