package org.openmrs.module.mentalhealth.reporting.converter;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

import static org.openmrs.module.mentalhealth.utils.MhReportUtils.formatDate;

public class ObsDateDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Obs obs = (Obs) obj;
        if(obs.getObsDatetime() != null) {
            return formatDate(obs.getObsDatetime());
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
}
