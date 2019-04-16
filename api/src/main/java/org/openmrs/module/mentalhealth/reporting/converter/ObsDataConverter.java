package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.mentalhealth.calculation.MhConfigCalculations;
import org.openmrs.module.mentalhealth.utils.MhConstants;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObsDataConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        List<Object> obsList = new ArrayList<>(Collections.singletonList(obj));
        List<Obs> obsForPatient = new ArrayList<>();
        List<String> wantedValues = new ArrayList<>();
        String exactValue = "";
        for(int i =0; i< obsList.size(); i++){
            if(obsList.get(i) instanceof Obs) {
                obsForPatient.add((Obs) obsList.get(i));
            }
            else  {
                obsForPatient.addAll((Collection<? extends Obs>) obsList.get(i));
            }
        }
        if(obsForPatient.size() > 0){
            for(Obs obs: obsForPatient){
                if(obs.getValueText() != null){
                    wantedValues.add(formatDate(obs.getObsDatetime())+":"+obs.getValueText());
                }
                else if(obs.getValueCoded() != null) {
                    wantedValues.add(formatDate(obs.getObsDatetime())+":"+getValueCodedValues(obs.getValueCoded()));
                }
                else if(obs.getValueNumeric() != null){
                    wantedValues.add(formatDate(obs.getObsDatetime())+":"+obs.getValueNumeric().toString());
                }
                else if(obs.getValueDatetime() != null) {
                    wantedValues.add(formatDate(obs.getObsDatetime())+":"+formatDate(obs.getValueDatetime()));
                }
            }
        }

        return wantedValues;
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
