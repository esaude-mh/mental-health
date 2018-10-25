package org.openmrs.module.mentalhealth.reports;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.mentalhealth.MhDataExportManager;
import org.openmrs.module.mentalhealth.reporting.library.dimension.MhCommonDimension;
import org.openmrs.module.mentalhealth.reporting.library.indicator.MhSampleIndicators;
import org.openmrs.module.mentalhealth.utils.MhColumnParameters;
import org.openmrs.module.mentalhealth.utils.MhEmrReportingUtils;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Sample indicator report that will output to the excel template
 */
@Component
public class CascadeAnalysisReport extends MhDataExportManager {

    @Autowired
    private MhSampleIndicators mhSampleIndicators;

    @Autowired
    private MhCommonDimension mhCommonDimension;

    public static final String XLS_TEMPLATE_UUID = "c30729e6-6995-4195-916c-4591f1b96a71";
    public static final String REPORT_DEFINITION_UUID = "fb263416-8f24-4fe2-af7a-c17ef22eef19";
    public static final String REPORT_NAME = "Cascade Analysis Report";
    
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "Cascade_Analysis_Report.xls");
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
        return "Report to display mental health cascade analysis";
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
                            new Parameter("dx","Diagnosis", String.class),
                            new Parameter("numMonths","Number of months for catchment", Integer.class)
                            );
    }

    private DataSetDefinition dataSetDefinition() {
    	
    	//this name is used like "#queryName.resultcolumnname#"
    	//in the template
    	String queryName = "catch";
    	
    	//pass the parameters that we're supplied as UDV
    	//back into the xls template renderer
    	String sqlQuery = "SELECT :dx AS dx," +
    			" :pop AS pop," +
    			" :facility as facility," +
    			" (SELECT CONCAT( DATE_FORMAT(STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH, '%Y-%m-%d'), ' to ', :endDate)) as months," +
    			//patients previous in care with schizophrenia
    			/**/
    			" (SELECT COUNT(DISTINCT P.patient_id)" +
    			" FROM patient P" +
    			" JOIN encounter E"+
    			" ON P.patient_id = E.patient_id"+
    			" JOIN obs O"+
    			" ON E.encounter_id = O.encounter_id"+ 
    			" JOIN concept C"+
    			" ON O.value_coded = C.concept_id"+ 
    			" WHERE (C.uuid IN ('e1d25e8e-1d5f-11e0-b929-000c29ad1d07')"+
				" OR C.uuid IN (SELECT VC.uuid"+
				" FROM concept VC"+
				" JOIN concept_name CN"+
				" WHERE CN.name like 'F20%'))"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH >= E.encounter_datetime"+
    			//" AND Location=:facility"
    			" AND O.voided = 0)" +
    			/**/
    			//
    			//" (1)" +
    			" AS previncare," +
    			//patients newly diagnosed with schizophrenia
				/**/
    			" (SELECT COUNT(DISTINCT P.patient_id)" +
    			" FROM patient P" +
    			" JOIN encounter E"+
    			" ON P.patient_id = E.patient_id"+
    			" JOIN obs O"+
    			" ON E.encounter_id = O.encounter_id"+ 
    			" JOIN concept C"+
    			" ON O.value_coded = C.concept_id"+ 
    			" WHERE (C.uuid IN ('e1d25e8e-1d5f-11e0-b929-000c29ad1d07')"+
				" OR C.uuid IN (SELECT VC.uuid"+
				" FROM concept VC"+
				" JOIN concept_name CN"+
				" WHERE CN.name like 'F20%'))"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime"+
    			" AND O.voided = 0" +
    			" AND P.patient_id NOT IN (SELECT DISTINCT P.patient_id" +
	    			" FROM patient P" +
	    			" JOIN encounter E"+
	    			" ON P.patient_id = E.patient_id"+
	    			" JOIN obs O"+
	    			" ON E.encounter_id = O.encounter_id"+ 
	    			" JOIN concept C"+
	    			" ON O.value_coded = C.concept_id"+ 
	    			" WHERE (C.uuid IN ('e1d25e8e-1d5f-11e0-b929-000c29ad1d07')"+
					" OR C.uuid IN (SELECT VC.uuid"+
					" FROM concept VC"+
					" JOIN concept_name CN"+
					" WHERE CN.name like 'F20%'))"+
	    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH >= E.encounter_datetime"+
	    			" AND O.voided = 0)" +
    	    	")" +
    			/**/
    			//
    			//" (2)" +
    			" AS newdx," +
    			//new patients with schizophrenia in the last 3 mos
    			/**/
    			" (SELECT COUNT(DISTINCT P.patient_id)" + 
    			" FROM patient P" + 
    			" JOIN encounter E" + 
    			" ON P.patient_id = E.patient_id" + 
    			" JOIN obs O" + 
    			" ON E.encounter_id = O.encounter_id" + 
    			" JOIN concept C" + 
    			" ON O.value_coded = C.concept_id" + 
    			" WHERE (C.uuid IN ('e1d25e8e-1d5f-11e0-b929-000c29ad1d07')" + 
    			" OR C.uuid IN (SELECT VC.uuid"+
				" FROM concept VC"+
				" JOIN concept_name CN"+
				" WHERE CN.name like 'F20%'))"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime" + 
    			" AND O.voided = 0" +
    			" AND P.patient_ID IN " +
    			//patient ids of patients subscribed medication in the last 3 mos
    			" (SELECT P.patient_id" + 
    	    		" FROM patient P" + 
	    			" JOIN encounter E" + 
	    			" ON P.patient_id = E.patient_id" + 
	    			" JOIN obs O" + 
	    			" ON E.encounter_id = O.encounter_id" + 
	    			" JOIN concept C" + 
	    			" ON O.value_coded = C.concept_id" + 
	    			" WHERE O.uuid ='e1da0ab2-1d5f-11e0-b929-000c29ad1d07'" + 
	    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime" + 
	    			" AND P.patient_id NOT IN (SELECT DISTINCT P.patient_id" +
	    				" FROM patient P" +
	    				" JOIN encounter E"+
	    				" ON P.patient_id = E.patient_id"+
	    				" JOIN obs O"+
	    				" ON E.encounter_id = O.encounter_id"+ 
	    				" JOIN concept C"+
	    				" ON O.value_coded = C.concept_id"+ 
	    				" WHERE (C.uuid IN ('e1d25e8e-1d5f-11e0-b929-000c29ad1d07')"+
						" OR C.uuid IN (SELECT VC.uuid"+
						" FROM concept VC"+
						" JOIN concept_name CN"+
						" WHERE CN.name like 'F20%'))"+
						" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH >= E.encounter_datetime"+
						" AND O.voided = 0)" +
					")"+
				")"+	
    			/**/
    			//" (3)" +
    			" AS numrx," +
    			//e1dae630-1d5f-11e0-b929-000c29ad1d07
    			//patients newly diagnosed with schizophrenia 
    			//with rx and fu
    			/**/
    			" (SELECT COUNT(DISTINCT P.patient_id)" + 
    			" FROM patient P" + 
    			" JOIN encounter E" + 
    			" ON P.patient_id = E.patient_id" + 
    			" JOIN obs O" + 
    			" ON E.encounter_id = O.encounter_id" + 
    			" JOIN concept C" + 
    			" ON O.value_coded = C.concept_id" + 
    			" WHERE (C.uuid IN ('e1d25e8e-1d5f-11e0-b929-000c29ad1d07')" +
    			" OR C.uuid IN (SELECT VC.uuid"+
				" FROM concept VC"+
				" JOIN concept_name CN"+
				" WHERE CN.name like 'F20%'))"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime" + 
    			" AND O.voided = 0" +
    			" AND P.patient_ID IN " +
    			//patient ids of patients subscribed medication in the last 3 mos
					" (SELECT P.patient_id" + 
	    			" FROM patient P" + 
	    			" JOIN encounter E" + 
	    			" ON P.patient_id = E.patient_id" + 
	    			" JOIN obs O" + 
	    			" ON E.encounter_id = O.encounter_id" + 
	    			" JOIN concept C" + 
	    			" ON O.value_coded = C.concept_id" + 
	    			" WHERE O.uuid ='e1da0ab2-1d5f-11e0-b929-000c29ad1d07'" + 
	    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime" + 
	    			" AND O.voided = 0"+
	    			" AND P.patient_id IN " +
	    			//patients with follow-up appointments made in the last 3 mos
	    				" (SELECT P.patient_id" + 
	    				" FROM patient P" + 
	    				" JOIN encounter E" + 
	    				" ON P.patient_id = E.patient_id" + 
	    				" JOIN obs O" + 
	    				" ON E.encounter_id = O.encounter_id" + 
	    				" JOIN concept C" + 
	    				" ON O.value_coded = C.concept_id" + 
	    				//with follow-up appointments
	    				" WHERE O.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'" + 
	    				" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime" + 
	    				" AND P.patient_id NOT IN (SELECT DISTINCT P.patient_id" +
		    			" FROM patient P" +
		    			" JOIN encounter E"+
		    			" ON P.patient_id = E.patient_id"+
		    			" JOIN obs O"+
		    			" ON E.encounter_id = O.encounter_id"+ 
		    			" JOIN concept C"+
		    			" ON O.value_coded = C.concept_id"+ 
		    			" WHERE (C.uuid IN ('e1d25e8e-1d5f-11e0-b929-000c29ad1d07')"+
						" OR C.uuid IN (SELECT VC.uuid"+
						" FROM concept VC"+
						" JOIN concept_name CN"+
						" WHERE CN.name like 'F20%'))"+
		    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH >= E.encounter_datetime"+
		    			" AND O.voided = 0)" +
	    				" AND O.voided = 0)"
    	    		+ ")"
    			+ ")" +
    			/**/
    			//" (4) " +
    			" AS numfu";

    	SqlDataSetDefinition dsd = new SqlDataSetDefinition(queryName, getDescription(), sqlQuery);
    	dsd.setParameters(getParameters());
    	
    	return dsd;
    }
}
