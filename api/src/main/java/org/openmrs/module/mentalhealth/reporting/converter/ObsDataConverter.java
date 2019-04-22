package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.openmrs.module.mentalhealth.utils.MhReportUtils.formatDate;
import static org.openmrs.module.mentalhealth.utils.ValueCodedAnswers.getValueCodedValues;

public class ObsDataConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        List<Object> obsList = new ArrayList<>(Collections.singletonList(obj));
        List<Obs> obsForPatient = new ArrayList<>();
        List<String> wantedValues = new ArrayList<>();
        for(int i =0; i< obsList.size(); i++){
            if(obsList.get(i) instanceof Obs) {
                Obs singleObs = (Obs) obsList.get(i);
                if(singleObs.getValueText() != null){
                    wantedValues.add(singleObs.getValueText());
                }
                else if(singleObs.getValueCoded() != null){
                    wantedValues.add(getValueCodedValues(singleObs.getValueCoded()));
                }
                else if(singleObs.getValueNumeric() != null){
                    wantedValues.add(singleObs.getValueNumeric().toString());
                }
                else if(singleObs.getValueDatetime() != null){
                    wantedValues.add(formatDate(singleObs.getValueDatetime()));
                }
                else {
                    wantedValues.add("Not sure how to interpret this");
                }
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



}
