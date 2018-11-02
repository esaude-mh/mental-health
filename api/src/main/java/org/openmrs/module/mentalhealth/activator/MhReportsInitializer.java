package org.openmrs.module.mentalhealth.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mentalhealth.MhReportManager;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.util.ReportUtil;

public class MhReportsInitializer implements MhInitializer {

    protected static final Log log = LogFactory.getLog(MhReportsInitializer.class);

    @Override
    public void started() {

        for (ReportManager reportManager : Context.getRegisteredComponents(MhReportManager.class)) {
            if (reportManager.getClass().getAnnotation(Deprecated.class) != null) {
                // remove depricated reports
                MhReportUtils.purgeReportDefinition(reportManager);
                log.info("Report " + reportManager.getName() + " is deprecated.  Removing it from database.");
            } else {
                // setup MH active reports
                MhReportUtils.setupReportDefinition(reportManager);
                log.info("Setting up report " + reportManager.getName() + "...");
            }
        }
        ReportUtil.updateGlobalProperty(ReportingConstants.GLOBAL_PROPERTY_DATA_EVALUATION_BATCH_SIZE, "-1");
    }

    @Override
    public void stopped() {
        purgeReports();
    }

    /**
     * Purges all EPTS reports from database.
     *
     * @throws Exception
     */
    private void purgeReports() {
        for (ReportManager reportManager : Context.getRegisteredComponents(MhReportManager.class)) {
            MhReportUtils.purgeReportDefinition(reportManager);
            log.info("Report " + reportManager.getName() + " removed from database.");
        }
    }
}
