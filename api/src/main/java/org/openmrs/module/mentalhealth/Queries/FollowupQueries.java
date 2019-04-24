package org.openmrs.module.mentalhealth.Queries;

public class FollowupQueries {

    public static String  getQuery(int identifier_type, int encounter_type) {
        return "SELECT p.patient_id AS Id,pi.identifier AS NID_Paciente, e.encounter_datetime AS visit FROM patient p INNER JOIN patient_identifier pi ON p.patient_id=pi.patient_id"
                +" INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN obs o ON p.patient_id=o.person_id"
                +" WHERE pi.identifier_type="+identifier_type
                +" AND e.encounter_type="+encounter_type;
    }
}
