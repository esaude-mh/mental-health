package org.openmrs.module.mentalhealth.utils;

import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
/**
* Created by Nicholas Ingosi on 6/20/17.
*/
public class MhReportUtils {

    /**
     * Maps a parameterizable item with no parameters
     *
     * @param parameterizable the parameterizable item
     * @param <T>
     * @return the mapped item
     */
    public static <T extends Parameterizable> Mapped<T> map(T parameterizable) {
        if (parameterizable == null) {
            throw new IllegalArgumentException("Parameterizable cannot be null");
        }
        return new Mapped<T>(parameterizable, null);
    }

    /**
     * Maps a parameterizable item using a string list of parameters and values
     *
     * @param parameterizable the parameterizable item
     * @param mappings the string list of mappings
     * @param <T>
     * @return the mapped item
     */
    public static <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new IllegalArgumentException("Parameterizable cannot be null");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

    /**
     * Maps a parameterizable item using a string list of parameters and values
     *
     * @param parameterizable the parameterizable item
     * @param mappings the string list of mappings
     * @param <T>
     * @return the mapped item
     */
    public static <T extends Parameterizable> Mapped<T> map(T parameterizable, Object... mappings) {
        if (parameterizable == null) {
            throw new IllegalArgumentException("Parameterizable cannot be null");
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (int m = 0; m < mappings.length; m += 2) {
            String param = (String) mappings[m];
            Object value = mappings[m + 1];
            paramMap.put(param, value);
        }
        return new Mapped<T>(parameterizable, paramMap);
    }

    /**
     * @return a new ReportDesign for a standard Excel output
     */
    public static ReportDesign createExcelDesign(String reportDesignUuid, ReportDefinition reportDefinition) {
        ReportDesign design = ReportManagerUtil.createExcelDesign(reportDesignUuid, reportDefinition);
        return design;
    }

    /**
     * @return a new ReportDesign for a standard Excel output
     */
    public static ReportDesign createExcelDesignWithProperties(String reportDesignUuid, ReportDefinition reportDefinition,
                                                               Properties props) {
        ReportDesign design = ReportManagerUtil.createExcelDesign(reportDesignUuid, reportDefinition);
        design.setProperties(props);
        return design;
    }

    /**
     * Purges a Report Definition from the database
     *
     * @param reportManager the Report Definition
     */
    public static void purgeReportDefinition(ReportManager reportManager) {
        ReportDefinition findDefinition = findReportDefinition(reportManager.getUuid());
        ReportDefinitionService reportService = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
        if (findDefinition != null) {
            reportService.purgeDefinition(findDefinition);

            // Purge Global property used to track version of report definition
            String gpName = "reporting.reportManager." + reportManager.getUuid() + ".version";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(gpName);
            if (gp != null) {
                Context.getAdministrationService().purgeGlobalProperty(gp);
            }
        }
    }

    /**
     * Returns the Report Definition matching the provided uuid.
     *
     * @param uuid Report Uuid
     * @throws RuntimeException a RuntimeException if the Report Definition can't be found
     * @return Report Definition object
     */

    public static ReportDefinition findReportDefinition(String uuid) {
        ReportDefinitionService reportService = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
        ReportDefinition reportDefinition = reportService.getDefinitionByUuid(uuid);
        if (reportDefinition != null) {
            return reportDefinition;
        }

        throw new RuntimeException("Couldn't find Report Definition " + uuid);
    }

    /**
     * Setup a Report Definition in a database
     *
     * @param reportManager the Report Definition
     */
    public static void setupReportDefinition(ReportManager reportManager) {
        ReportManagerUtil.setupReport(reportManager);
    }

    /**
     * Get concepts give a uuid
     */
    public static Concept getConcept(String uuid) {
        return Context.getConceptService().getConceptByUuid(uuid);
    }

    public static String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null?"":dateFormatter.format(date);
    }

}
