package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

public class ObsDataConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Obs value = (Obs) obj;

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
}
