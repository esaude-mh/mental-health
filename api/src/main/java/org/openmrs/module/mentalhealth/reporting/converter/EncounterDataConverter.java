package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Encounter;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.mentalhealth.utils.MhReportUtils.formatDate;

public class EncounterDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        List<Object> encounters = new ArrayList<>(Collections.singletonList(obj));
        List<String> wantedValues = new ArrayList<>();
        List<Encounter> encountersForPatient = new ArrayList<>();
        for(int i =0; i< encounters.size(); i++){
            if(encounters.get(i) instanceof Encounter) {
                wantedValues.add(formatDate(((Encounter) encounters.get(i)).getEncounterDatetime()));
            }
            else {
                encountersForPatient.addAll((Collection<? extends Encounter>) encounters.get(i));
            }
        }

        if(encountersForPatient.size() > 0){
            for(Encounter enc:encountersForPatient){
                wantedValues.add(formatDate(enc.getEncounterDatetime()));
            }
        }
        return wantedValues;
    }

    @Override
    public Class<?> getInputDataType() {
        return Encounter.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }


}
