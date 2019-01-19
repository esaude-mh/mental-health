package org.openmrs.module.mentalhealth.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.module.mentalhealth.MhDataExportManager;
import org.openmrs.module.mentalhealth.reporting.library.dimension.MhCommonDimension;
import org.openmrs.module.mentalhealth.reporting.library.indicator.MhSampleIndicators;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sample indicator report that will output to the excel template
 */
@Component
public class EpilepsyCascadeAnalysisReport extends MhDataExportManager {

    @Autowired
    private MhSampleIndicators mhSampleIndicators;

    @Autowired
    private MhCommonDimension mhCommonDimension;

    public static final String XLS_TEMPLATE_UUID = "c30729e6-6995-4195-916c-4591f1b96a72";
    public static final String REPORT_DEFINITION_UUID = "fb263416-8f24-4fe2-af7a-c17ef22eef20";
    public static final String REPORT_NAME = "Epilepsy Cascade Analysis Report";

    @Override
    public String getExcelDesignUuid() {
        return XLS_TEMPLATE_UUID;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report
     * design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "Epilepsy_Cascade_Analysis_Report.xls");
        rd.setName(REPORT_NAME);
        return rd;
    }

    @Override
    public String getUuid() {
        return REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return REPORT_NAME;
    }

    @Override
    public String getDescription() {
        return "Report to display mental health epilepsy cascade analysis";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        //rd.
        rd.addDataSetDefinition("catch", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(new Parameter("endDate","Data Final Inclusão", Date.class),
                            new Parameter("facility", "Unidade Sanitária", Location.class),
                            new Parameter("pop","Catchment Population",Integer.class),
                            //new Parameter("dx","Diagnosis", String.class),
                            new Parameter("numMonths","Number of months for catchment", Integer.class)
                            );
    }

    private DataSetDefinition dataSetDefinition() {

    	//this name is used like "#queryName.resultcolumnname#"
    	//in the template
    	String queryName = "catch";

    	//store re-usable query parts
    	String prevInCare =
    			" FROM patient P"+
    			" JOIN encounter E"+
    			" ON P.patient_id = E.patient_id"+
    			" JOIN obs O"+
    			" ON E.encounter_id = O.encounter_id"+
    			" JOIN concept C"+
    			" ON O.concept_id = C.concept_id"+
    			" WHERE C.uuid IN ('af2d7516-8be6-476f-9776-fb5a6b95ffa6')"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH >= E.encounter_datetime"+
    			" AND E.location_id=:facility"+
				" AND E.voided = 0"+
    			" AND O.voided = 0";
    	
    	String newDx = 
    			" FROM patient P"+
    			" JOIN encounter E"+
    			" ON P.patient_id = E.patient_id"+
    			" JOIN obs O"+
    			" ON E.encounter_id = O.encounter_id"+
    			" JOIN concept C"+
    			" ON O.concept_id = C.concept_id"+
				" JOIN (SELECT DISTINCT P.patient_id, E.encounter_datetime"+
    			"       FROM patient P"+
		        "    	JOIN encounter E"+
    			"    	ON P.patient_id = E.patient_id"+
		        "    	JOIN encounter_type ET"+
    			"    	ON E.encounter_type = ET.encounter_type_id"+
		        "    	WHERE "+
    			"    	ET.uuid = 'e7c5643e-9efe-11e8-a4c3-2b65da6977a7'"+
    			"		AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
    			"    	AND E.voided = 0"+
		        "    	AND E.location_id=:facility) X1"+
    			" ON X1.patient_id = P.patient_id"+
		        " JOIN (SELECT DISTINCT P.patient_id, E.encounter_datetime"+
    			"       FROM patient P"+
		        "    	JOIN encounter E"+
    			"    	ON P.patient_id = E.patient_id"+
		        "    	JOIN encounter_type ET"+
    			"    	ON E.encounter_type = ET.encounter_type_id"+
		        "    	WHERE "+
    			"    	ET.uuid = '0b3012b6-9eff-11e8-a0a6-cb6dac4515ee'"+
    			"		AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
    			"    	AND E.voided = 0"+
		        "    	AND E.location_id=:facility) X2"+
    			" ON X2.patient_id = P.patient_id"+
		        " WHERE C.uuid IN ('af2d7516-8be6-476f-9776-fb5a6b95ffa6')"+
		        " AND X1.encounter_datetime = X2.encounter_datetime"+
    			" AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
		        " AND E.location_id=:facility"+
				" AND E.voided = 0"+
		        " AND O.voided = 0";
    	
    	String numRx =
    			" AND P.patient_ID IN "+
    			" (SELECT DISTINCT P.patient_id"+
                " FROM patient P"+
                " JOIN encounter E"+
                " ON P.patient_id = E.patient_id"+
                " JOIN obs O"+
                " ON E.encounter_id = O.encounter_id"+
                " JOIN concept C"+
                " ON O.concept_id = C.concept_id"+
                " WHERE C.uuid ='e1da0ab2-1d5f-11e0-b929-000c29ad1d07'"+
                " AND E.location_id=:facility"+
                " AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" AND E.voided = 0"+
                " AND O.voided = 0)";
    	
    	String numFu =
	    		" AND P.patient_id IN "+
	    		" (SELECT DISTINCT P.patient_id"+
	    		" FROM patient P"+
	    		" JOIN encounter E"+
	    		" ON P.patient_id = E.patient_id"+
	    		" JOIN obs O"+
	    		" ON E.encounter_id = O.encounter_id"+
	    		" JOIN concept C"+
	    		" ON O.concept_id = C.concept_id"+
	    		" WHERE C.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'"+
	    		" AND E.location_id=:facility"+
                " AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" AND E.voided = 0"+
	    		" AND O.voided = 0)";
    	
    	String allFuDue =
				" AND P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN encounter_type ET "+
				" ON ET.encounter_type_id = E.encounter_type"+
				" JOIN obs O"+
				" ON O.encounter_id = E.encounter_id"+
				" JOIN concept C"+
				" ON C.concept_id = O.concept_id"+
				" WHERE C.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'"+
				" AND ET.uuid = '0b3012b6-9eff-11e8-a0a6-cb6dac4515ee'"+
				" AND O.value_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" AND E.voided = 0"+
				" AND O.voided = 0)";

    	String fuKept =
				" AND P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN obs O"+
				" ON E.encounter_id = O.encounter_id"+
				" JOIN (SELECT P.patient_id, MAX(E.encounter_datetime) as most_recent_fu"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN encounter_type ET"+
				" ON ET.encounter_type_id = E.encounter_type"+
				" WHERE ET.uuid = '0b3012b6-9eff-11e8-a0a6-cb6dac4515ee'"+
				" AND E.location_id=:facility"+
				" AND E.voided = 0"+
		        " AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" GROUP BY P.patient_id) X"+
				" ON X.patient_id = P.patient_id"+
		        " JOIN concept C"+
		        " ON C.concept_id = O.concept_id"+
		        " WHERE C.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'"+
		        " AND O.value_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" AND E.location_id=:facility"+
				" AND E.encounter_datetime < X.most_recent_fu"+
				" AND E.voided = 0"+
				" AND O.voided = 0";

    	String fuOntime =
		    	" AND X.most_recent_fu BETWEEN STR_TO_DATE(O.value_datetime, '%Y-%m-%d') - INTERVAL 5 DAY AND STR_TO_DATE(O.value_datetime, '%Y-%m-%d') + INTERVAL 5 DAY";
    	
    	String adherent =
				" AND P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN obs O"+
				" ON E.encounter_id = O.encounter_id"+
    			" JOIN (SELECT MEDS_DURATION_OBS.encounter_id, MEDS_DURATION_OBS.value_numeric"+ 
    	        " FROM obs MEDS_DURATION_OBS"+
    	        " JOIN concept MEDS_DUR_CNCPT"+
    	        " ON MEDS_DUR_CNCPT.concept_id = MEDS_DURATION_OBS.concept_id"+
    	        " WHERE MEDS_DUR_CNCPT.uuid = '591b988b-8f9c-4b99-84d0-d655bd9c1fd5'"+
    	        " AND MEDS_DURATION_OBS.voided = 0) MEDS_DUR"+
    			" ON MEDS_DUR.encounter_id = E.encounter_id"+
				" JOIN (SELECT P.patient_id, MAX(E.encounter_datetime) as most_recent_fu"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN encounter_type ET"+
				" ON ET.encounter_type_id = E.encounter_type"+
				" WHERE ET.uuid = '0b3012b6-9eff-11e8-a0a6-cb6dac4515ee'"+
				" AND E.location_id=:facility"+
				" AND E.voided = 0"+
		        " AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" GROUP BY P.patient_id) X"+
				" ON X.patient_id = P.patient_id"+
		        " JOIN concept C"+
		        " ON C.concept_id = O.concept_id"+
		        " WHERE C.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'"+
		        " AND O.value_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" AND E.location_id=:facility"+
				" AND E.encounter_datetime < X.most_recent_fu"+
                " AND E.encounter_datetime + INTERVAL MEDS_DUR.value_numeric DAY >= X.most_recent_fu"+
				" AND E.voided = 0"+
				" AND O.voided = 0";
    			
    	String improved =
				" AND P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				" FROM patient P"+
				" JOIN (SELECT P.patient_id, E.encounter_id"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN encounter_type ET"+
				" ON ET.encounter_type_id = E.encounter_type"+
				" WHERE ET.uuid = 'e7c5643e-9efe-11e8-a4c3-2b65da6977a7'"+
				" AND E.location_id=:facility"+
				" AND E.voided = 0"+
				" GROUP BY P.patient_id, E.encounter_id) BASE"+
				" ON BASE.patient_id = P.patient_id"+
				" JOIN (SELECT P.patient_id, MAX(E.encounter_datetime) as most_recent_fu"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN encounter_type ET"+
				" ON ET.encounter_type_id = E.encounter_type"+
				" WHERE ET.uuid = '0b3012b6-9eff-11e8-a0a6-cb6dac4515ee'"+
				" AND E.location_id=:facility"+
				" AND E.voided = 0"+
		        " AND E.encounter_datetime BETWEEN STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH AND STR_TO_DATE(:endDate, '%Y-%m-%d')"+
				" GROUP BY P.patient_id) X"+
				" ON X.patient_id = P.patient_id"+
				" JOIN (SELECT P.patient_id, E.encounter_id, E.encounter_datetime"+
				" FROM patient P"+
				" JOIN encounter E"+
				" ON P.patient_id = E.patient_id"+
				" JOIN encounter_type ET"+
				" ON ET.encounter_type_id = E.encounter_type"+
				" WHERE ET.uuid = '0b3012b6-9eff-11e8-a0a6-cb6dac4515ee'"+
				" AND E.location_id=:facility"+
				" AND E.voided = 0"+
				" GROUP BY P.patient_id, E.encounter_id, E.encounter_datetime) X2"+
				" ON X2.patient_id = X.patient_id"+
				" AND X2.encounter_datetime = X.most_recent_fu"+
    			" JOIN (SELECT FU_WHODAS_OBS.encounter_id, FU_WHODAS_OBS.value_numeric"+ 
    	        " FROM obs FU_WHODAS_OBS"+
    	        " JOIN concept FU_WHODAS_CNCPT"+
    	        " ON FU_WHODAS_CNCPT.concept_id = FU_WHODAS_OBS.concept_id"+
    	        " WHERE FU_WHODAS_CNCPT.uuid = 'd14dec0e-833b-49d4-b3f2-02c4111ab4f9'"+
    	        " AND FU_WHODAS_OBS.voided = 0) FU_WHODAS"+
    			" ON FU_WHODAS.encounter_id = X2.encounter_id"+
    			" JOIN (SELECT BASE_WHODAS_OBS.encounter_id, BASE_WHODAS_OBS.value_numeric"+ 
    	        " FROM obs BASE_WHODAS_OBS"+
    	        " JOIN concept BASE_WHODAS_CNCPT"+
    	        " ON BASE_WHODAS_CNCPT.concept_id = BASE_WHODAS_OBS.concept_id"+
    	        " WHERE BASE_WHODAS_CNCPT.uuid = 'd14dec0e-833b-49d4-b3f2-02c4111ab4f9'"+
    	        " AND BASE_WHODAS_OBS.voided = 0) BASE_WHODAS"+
    			" ON BASE_WHODAS.encounter_id = BASE.encounter_id"+
		        " WHERE (FU_WHODAS.value_numeric <= 10"+
    			" OR FU_WHODAS.value_numeric <= BASE_WHODAS.value_numeric * 0.5))";
    			
    	//pass the parameters that we're supplied as UDV
    	//back into the xls template renderer
    	String sqlQuery = "SELECT 'Epilepsy' AS dx,"+
    			" :pop AS pop,"+
    			" (SELECT L.name from location L where L.location_id = :facility) as facility,"+
    			" (SELECT CONCAT( DATE_FORMAT(STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH, '%Y-%m-%d'), ' to ', DATE(:endDate))) as months,"+
    			//patients previous in care with epilepsy
    			//
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
    			prevInCare+
    			")"+
    			//
    			" AS previncare,"+
    			//patients newly diagnosed with epilepsy
				//
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
				newDx+
    			")"+
    			//
    			" AS newdx,"+
    			//new patients with epilepsy in the period
    			//
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
    			newDx+
    			numRx+
				")"+
				//
    			" AS numrx,"+
    			//patients newly diagnosed with epilepsy with rx and fu
    			//
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
    			newDx+
    			numRx+
    			numFu+
    			")"+
    			//
    			" AS numfu,"+
    			//
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
				" FROM patient P" +
    			" WHERE "+
				" (P.patient_id IN"+
    			" (SELECT DISTINCT P.patient_id"+
				prevInCare+
				") OR P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				newDx+
				numRx+
				numFu+
				"))"+
				allFuDue+
				")"+
				//
				" AS allfudue,"+
				//
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
				" FROM patient P" +
    			" WHERE "+
				" (P.patient_id IN"+
    			" (SELECT DISTINCT P.patient_id"+
				prevInCare+
				") OR P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				newDx+
				numRx+
				numFu+
				"))"+
				allFuDue+
				fuKept+
				"))"+
				//
    			" AS fukept,"+
		        //
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
				" FROM patient P" +
    			" WHERE "+
				" (P.patient_id IN"+
    			" (SELECT DISTINCT P.patient_id"+
				prevInCare+
				") OR P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				newDx+
				numRx+
				numFu+
				"))"+
				allFuDue+
				fuKept+
				fuOntime+
				"))"+
				//
    			" AS fuontime,"+
	            //
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
				" FROM patient P" +
    			" WHERE "+
				" (P.patient_id IN"+
    			" (SELECT DISTINCT P.patient_id"+
				prevInCare+
				") OR P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				newDx+
				numRx+
				numFu+
				"))"+
				allFuDue+
				adherent+
				fuOntime+
				"))"+
				//
    			" AS adherent,"+
                //
    			" (SELECT COUNT(DISTINCT P.patient_id)"+
				" FROM patient P" +
    			" WHERE "+
				" (P.patient_id IN"+
    			" (SELECT DISTINCT P.patient_id"+
				prevInCare+
				") OR P.patient_id IN"+
				" (SELECT DISTINCT P.patient_id"+
				newDx+
				numRx+
				numFu+
				"))"+
				allFuDue+
				adherent+
				fuOntime+
				")"+
				improved+
				")"+
				//
    			" AS improved";

    	SqlDataSetDefinition dsd = new SqlDataSetDefinition(queryName, getDescription(), sqlQuery);
    	dsd.setParameters(getParameters());

    	return dsd;
    }

}
