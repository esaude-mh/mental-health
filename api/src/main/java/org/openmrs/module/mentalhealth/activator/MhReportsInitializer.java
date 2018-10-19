package org.openmrs.module.mentalhealth.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mentalhealth.MhReportManager;
import org.openmrs.module.mentalhealth.reports.CascadeAnalysisReport;
import org.openmrs.module.mentalhealth.reports.SampleReport;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.util.ReportUtil;

public class MhReportsInitializer implements MhInitializer {

	static final String SAMPLE_XLS_TEMPLATE_UUID = "83684b4c-d062-11e8-89d2-938f982d9cb7";
	
    protected static final Log log = LogFactory.getLog(MhReportsInitializer.class);

    @Override
    public void started() {
        //remove all old reports and reload, this should be removed when time get for a release,
        //it is only for rapid development
        removeOldReports();
        //any class that will extent MhReportManager will be registered automatically when the module starts
        ReportManagerUtil.setupAllReports(MhReportManager.class);
        ReportUtil.updateGlobalProperty(ReportingConstants.GLOBAL_PROPERTY_DATA_EVALUATION_BATCH_SIZE, "-1");

    }

    @Override
    public void stopped() {

    }

    //for rapid development, instead of increasing the version number to be able to force changes to be reflected in the database
    //I will just drop the entire report definition and let them be rebuilt at activation
    private void removeOldReports() {
        AdministrationService as = Context.getAdministrationService();
        // the purpose of this snipet is to allow rapid development other than
        // going to change the report version all the time for change
        log.warn("Removing old reports");
        
        String []oldReportDesignUUIDs = 	{
        								SampleReport.XLS_TEMPLATE_UUID,
        								CascadeAnalysisReport.XLS_TEMPLATE_UUID//""
        							};
        
        for(int i=0; i<oldReportDesignUUIDs.length; i++) {
        	// getting id of the loaded report designs
        	String report_resource_sample_id = "select id from reporting_report_design where uuid='"+oldReportDesignUUIDs[i]+"'";
        	// deleting the resource already loaded
        	as.executeSQL("delete from reporting_report_design_resource where report_design_id =("
                + report_resource_sample_id + ");", false);
        	// deleting the actual designs now
        	as.executeSQL("delete from reporting_report_design where uuid='"+oldReportDesignUUIDs[i]+"';", false);
        }
        // deleting all report requests and managers
        as.executeSQL("delete from reporting_report_request;", false);
        as.executeSQL("delete from global_property WHERE property LIKE 'reporting.reportManager%';", false);

        String []oldReportDefinitionUUIDs = {
        										SampleReport.REPORT_DEFINITION_UUID,
        										CascadeAnalysisReport.REPORT_DEFINITION_UUID,
        };
        
        for(int i=0; i<oldReportDefinitionUUIDs.length; i++) {
        	// deleting the actual report definitions from the db
        	as.executeSQL("delete from serialized_object WHERE uuid = '"+oldReportDefinitionUUIDs[i]+"';", false);
        }
    }
}
