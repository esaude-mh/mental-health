package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Encounter;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EncounterDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Encounter value = (Encounter) obj;
        if(value.getEncounterDatetime() != null) {
            return formatDate(value.getEncounterDatetime());
        }
        return null;
    }

    @Override
    public Class<?> getInputDataType() {
        return Encounter.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null?"":dateFormatter.format(date);
    }
}
