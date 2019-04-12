package org.openmrs.module.mentalhealth.utils;

import org.openmrs.PersonAddress;
import org.openmrs.module.reporting.data.ConvertedDataDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PersonToPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class DataFactory {

    public PatientDataDefinition getPreferredAddress(String property) {
        PreferredAddressDataDefinition d = new PreferredAddressDataDefinition();
        PropertyConverter converter = new PropertyConverter(PersonAddress.class, property);
        return convert(d, converter);
    }

    public PatientDataDefinition convert(PersonDataDefinition pdd, DataConverter converter) {
        return convert(pdd, null, converter);
    }

    public PatientDataDefinition convert(PersonDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
        return convert(new PersonToPatientDataDefinition(pdd), renamedParameters, converter);
    }

    public PatientDataDefinition convert(PatientDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
        ConvertedPatientDataDefinition convertedDefinition = new ConvertedPatientDataDefinition();
        addAndConvertMappings(pdd, convertedDefinition, renamedParameters, converter);
        return convertedDefinition;
    }
    protected <T extends DataDefinition> void addAndConvertMappings(T copyFrom, ConvertedDataDefinition<T> copyTo, Map<String, String> renamedParameters, DataConverter converter) {
        copyTo.setDefinitionToConvert(ParameterizableUtil.copyAndMap(copyFrom, copyTo, renamedParameters));
        if (converter != null) {
            copyTo.setConverters(Arrays.asList(converter));
        }
    }

}
