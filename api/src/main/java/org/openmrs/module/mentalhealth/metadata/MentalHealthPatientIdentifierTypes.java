package org.openmrs.module.mentalhealth.metadata;

import org.openmrs.module.idgen.validator.LuhnMod30IdentifierValidator;
import org.openmrs.module.metadatadeploy.descriptor.PatientIdentifierTypeDescriptor;
import org.openmrs.patient.IdentifierValidator;

public class MentalHealthPatientIdentifierTypes {

    public static PatientIdentifierTypeDescriptor MH_NID= new PatientIdentifierTypeDescriptor(){

        @Override
        public String uuid() {
            return "689a4f84-aa03-11e8-adff-6fec648a8d89";
        }

        @Override
        public String name() {
			return "NID (SAÚDE MENTAL)";
        }

        @Override
        public String description() {
            return "Patient Identifier to represent a patient in mental health program";
        }
    };

    public static PatientIdentifierTypeDescriptor TARV_NID= new PatientIdentifierTypeDescriptor(){

        @Override
        public String uuid() {
            return "e2b966d0-1d5f-11e0-b929-000c29ad1d07";
        }

        @Override
        public String name() {
			return "NID (SERVIÇO TARV)";
        }

        @Override
        public String description() {
            return "Numero de Identificaçao de Doente, serviço TARV";
        }

        @Override
		public String format() {
            return "[0-9]{8}/[0-9]{2}/[0-9]{5}";
        }
        @Override
		public String formatDescription() {
            return "PPDDUUSS/AA/NNNNN";
        }

    };

    public static PatientIdentifierTypeDescriptor OPENMRS_ID = new PatientIdentifierTypeDescriptor() {
        @Override
        public String name() {
            return "OpenMRS ID";
        }

        @Override
        public String description() {
            return "OpenMRS patient identifier, with check-digit";
        }

        public String uuid() {
            return "05a29f94-c0ed-11e2-94be-8c13b969e334";
        }

        public Class<? extends IdentifierValidator> validator() {
            return LuhnMod30IdentifierValidator.class;
        }
    };
}
