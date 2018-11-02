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
    			" (SELECT L.name from location L where L.location_id = :facility) as facility," +
    			" (SELECT CONCAT( DATE_FORMAT(STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH, '%Y-%m-%d'), ' to ', DATE(:endDate))) as months," +
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
    			" AND E.location_id=:facility"+
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
    			" JOIN encounter_type ET"+
    			" ON E.encounter_type = ET.encounter_type_id"+
    			" WHERE "+
    			" ET.uuid = 'e7c5643e-9efe-11e8-a4c3-2b65da6977a7'"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime"+
    			" AND O.voided = 0" +
    			" AND E.location_id=:facility"+
    			" AND P.patient_id IN (SELECT DISTINCT P.patient_id" +
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
					" AND E.location_id=:facility"+
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
    			" JOIN encounter E"+
    			" ON P.patient_id = E.patient_id"+
    			" JOIN obs O"+
    			" ON E.encounter_id = O.encounter_id"+
    			" JOIN concept C"+
    			" ON O.value_coded = C.concept_id"+
    			" JOIN encounter_type ET"+
    			" ON E.encounter_type = ET.encounter_type_id"+
    			" WHERE "+
    			" ET.uuid = 'e7c5643e-9efe-11e8-a4c3-2b65da6977a7'"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime"+
    			" AND O.voided = 0" +
    			" AND E.location_id=:facility"+
    			" AND P.patient_id IN (SELECT DISTINCT P.patient_id" +
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
					" AND E.location_id=:facility"+
	    			" AND O.voided = 0)" +
    			" AND P.patient_ID IN " +
    			//patient ids of patients prescribed medication in the last 3 mos
          "(SELECT P.patient_id" +
                   " FROM patient P" +
                    " JOIN encounter E" +
                    " ON P.patient_id = E.patient_id" +
                    " JOIN obs O" +
                    " ON E.encounter_id = O.encounter_id" +
                    " JOIN concept C" +
                    " ON O.concept_id = C.concept_id" +
                    " WHERE C.uuid ='e1da0ab2-1d5f-11e0-b929-000c29ad1d07'" +
                    " AND E.location_id=:facility"+
                    " AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime" + ")"+
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
    			" JOIN encounter E"+
    			" ON P.patient_id = E.patient_id"+
    			" JOIN obs O"+
    			" ON E.encounter_id = O.encounter_id"+
    			" JOIN concept C"+
    			" ON O.value_coded = C.concept_id"+
    			" JOIN encounter_type ET"+
    			" ON E.encounter_type = ET.encounter_type_id"+
    			" WHERE "+
    			" ET.uuid = 'e7c5643e-9efe-11e8-a4c3-2b65da6977a7'"+
    			" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime"+
    			" AND O.voided = 0" +
    			" AND E.location_id=:facility"+
    			" AND P.patient_id IN (SELECT DISTINCT P.patient_id" +
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
					" AND E.location_id=:facility"+
	    			" AND O.voided = 0" +
    			" AND P.patient_ID IN " +
    			//patient ids of patients prescribed medication in the last 3 mos
					" (SELECT P.patient_id" +
	    			" FROM patient P" +
	    			" JOIN encounter E" +
	    			" ON P.patient_id = E.patient_id" +
	    			" JOIN obs O" +
	    			" ON E.encounter_id = O.encounter_id" +
	    			" JOIN concept C" +
	    			" ON O.concept_id = C.concept_id" +
	    			" WHERE O.uuid ='e1da0ab2-1d5f-11e0-b929-000c29ad1d07'" +
	    			" AND E.location_id=:facility"+
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
	    				" ON O.concept_id = C.concept_id" +
	    				//with follow-up appointments
	    				" WHERE C.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'" +
	    				" AND E.location_id=:facility"+
	    				" AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH < E.encounter_datetime" +
	    				" AND O.voided = 0)"
    	    		+ ")" +
    			 ")" +
    			 ")" +
    			" AS numfu,"+
          /**/
          //" (4) " +
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
                " AND E.location_id=:facility"+
               " AND P.patient_id IN"+
                " (SELECT P.patient_id"+
               " FROM patient P"+
                " JOIN encounter E"+
               " ON P.patient_id = E.patient_id"+
                " JOIN obs O"+
               " ON O.encounter_id = E.encounter_id"+
                " JOIN concept C"+
               " ON C.concept_id = O.concept_id"+
                " WHERE C.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'" +
               //followup apt is during this period
                " AND STR_TO_DATE(:endDate, '%Y-%m-%d') - INTERVAL :numMonths MONTH <= O.value_datetime"+
                " AND O.value_datetime <= STR_TO_DATE(:endDate, '%Y-%m-%d'))"+
               " AND P.patient_id IN"+
                " (SELECT P.patient_id"+
               " FROM patient P"+
                " JOIN encounter E"+
               " ON P.patient_id = E.patient_id"+
                " JOIN obs O"+
               " ON O.encounter_id = E.encounter_id"+
                " JOIN concept C"+
               " ON C.concept_id = O.concept_id"+
        " JOIN (SELECT P.patient_id, MAX(O.value_datetime) as latest_next_visit"+
               " FROM patient P"+
               " JOIN encounter E"+
            " ON P.patient_id = E.patient_id"+
               " JOIN obs O"+
            " ON O.encounter_id = E.encounter_id"+
               " JOIN concept C"+
            " ON C.concept_id = O.concept_id"+
               " WHERE C.uuid = 'e1dae630-1d5f-11e0-b929-000c29ad1d07') X"+
           " ON X.patient_id = P.patient_id"+
                " WHERE C.uuid ='e1dae630-1d5f-11e0-b929-000c29ad1d07'" +
               //followup apt is >= 5 days before latest 'next consult date'
                " AND STR_TO_DATE(X.latest_next_visit, '%Y-%m-%d') - INTERVAL 5 DAY <= O.value_datetime"+
                " AND O.value_datetime <= STR_TO_DATE(:endDate, '%Y-%m-%d'))"+
                " AND O.voided = 0)" +
    			" AS allfudue,"+
    			" 'not implemented'"+
    			" AS fukept,"+
    			" 'not implemented'"+
    			" AS fuontime,"+
    			" 'not implemented'"+
    			" AS adherent,"+
    			" 'not implemented'"+
    			" AS improved";

    	SqlDataSetDefinition dsd = new SqlDataSetDefinition(queryName, getDescription(), sqlQuery);
    	dsd.setParameters(getParameters());

    	return dsd;
    }

}
