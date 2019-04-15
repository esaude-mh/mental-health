package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Encounter;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EncounterDatetimeForVisitConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        List<Object> encounterDatesObjs = new ArrayList<>(Collections.singletonList(obj));
        List<Encounter> encountersForPatient = new ArrayList<>();
        List<Date> wantedDated = new ArrayList<>();
        for(int i =0; i< encounterDatesObjs.size(); i++){
            encountersForPatient.addAll((Collection<? extends Encounter>) encounterDatesObjs.get(i));
        }
        if(encountersForPatient.size() > 0){
            for(Encounter enc: encountersForPatient){
                wantedDated.add(enc.getEncounterDatetime());
            }
        }
        if(wantedDated.size() > 0){
            Collections.sort(wantedDated);
        }

        return wantedDated;
    }

    @Override
    public Class<?> getInputDataType() {
        return String.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }

}
