package org.openmrs.module.mentalhealth.Queries;

public class FollowupQueries {

    public static String  getQuery(int identifier_type) {
        return "SELECT p.patient_id AS Id,pa.identifier AS NID_Paciente FROM patient_identifier pa INNER JOIN patient p ON pa.patient_id=p.patient_id WHERE pa.identifier_type="+identifier_type;
    }
}
