package org.openmrs.module.mentalhealth.utils;

import org.openmrs.Concept;

public class ValueCodedAnswers {

    public static String getValueCodedValues(Concept c){
        String value = "";
        if(c.equals(MhReportUtils.getConcept(MhConstants.YES))){
            value = "S";
        }
        else if(c.equals(MhReportUtils.getConcept(MhConstants.NO))){
            value = "N";
        }
        else if(c.equals(MhReportUtils.getConcept(MhConstants.NOT_PROVIDED))){
            value = "NP";
        }
        else {
            value = c.getName().getName();
        }
        return value;
    }
}
