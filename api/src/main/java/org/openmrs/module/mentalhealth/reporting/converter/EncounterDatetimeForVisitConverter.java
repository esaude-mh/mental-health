package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class EncounterDatetimeForVisitConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }


        return obj;
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
