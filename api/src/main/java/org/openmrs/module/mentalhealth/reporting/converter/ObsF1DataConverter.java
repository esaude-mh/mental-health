package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.openmrs.module.mentalhealth.utils.MhReportUtils.formatDate;
import static org.openmrs.module.mentalhealth.utils.ValueCodedAnswers.getValueCodedValues;

public class ObsF1DataConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        List<Object> obsList = new ArrayList<>(Collections.singletonList(obj));
        List<Obs> obsForPatient = new ArrayList<>();
        List<String> wantedValues = new ArrayList<>();

        if(obj instanceof Obs) {
            Obs obs = (Obs) obj;
            if (obs.getValueCoded() != null) {
                wantedValues.add(getValueCodedValues(obs.getValueCoded()));
            } else if (obs.getValueDatetime() != null) {
                wantedValues.add(formatDate(obs.getValueDatetime()));
            } else if (obs.getValueNumeric() != null) {
                wantedValues.add(obs.getValueNumeric().toString());
            } else if (obs.getValueText() != null) {
                wantedValues.add(obs.getValueText());
            }
        }
        else {

            for(int i =0; i< obsList.size(); i++){
                obsForPatient.addAll((Collection<? extends Obs>) obsList.get(i));
            }
            if(obsForPatient.size() > 0){
                for(Obs obs: obsForPatient){
                    if(obs.getValueText() != null){
                        wantedValues.add(formatDate(obs.getObsDatetime())+":"+obs.getValueText());
                    }
                    else if(obs.getValueCoded() != null) {
                        wantedValues.add(formatDate(obs.getObsDatetime())+":"+ getValueCodedValues(obs.getValueCoded()));
                    }
                    else if(obs.getValueNumeric() != null){
                        wantedValues.add(formatDate(obs.getObsDatetime())+":"+obs.getValueNumeric().toString());
                    }
                    else if(obs.getValueDatetime() != null) {
                        wantedValues.add(formatDate(obs.getObsDatetime())+":"+ formatDate(obs.getValueDatetime()));
                    }
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