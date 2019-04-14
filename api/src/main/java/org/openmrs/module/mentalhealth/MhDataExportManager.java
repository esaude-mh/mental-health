package org.openmrs.module.mentalhealth;

import org.apache.poi.util.IOUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MhDataExportManager extends MhReportManager {

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    public abstract String getExcelDesignUuid();

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        ReportDesign excelDesign = createExcelDesign(getExcelDesignUuid(), reportDefinition);
        l.add(excelDesign);
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report
     * design by adding properties and other metadata to the report design
     *
     * @return The report design
     */
    public abstract ReportDesign buildReportDesign(ReportDefinition reportDefinition);

    /**
     * Auto generated method comment
     *
     * @param reportDefinition the reportDesign to set
     * @param resourceName
     * @param reportDesignName
     * @param properties
     * @return
     * @throws IOException
     */
    public static ReportDesign createXlsReportDesign(
            ReportDefinition reportDefinition,
            String resourceName,
            String reportDesignName,
            String excelDesignUuid,
            Map<? extends Object, ? extends Object> properties)
            throws IOException {

        ReportService rs = Context.getService(ReportService.class);
        for (ReportDesign rdd : rs.getAllReportDesigns(false)) {
            if (reportDesignName.equals(rdd.getName())) {
                rs.purgeReportDesign(rdd);
            }
        }

        ReportDesignResource resource = new ReportDesignResource();
        resource.setName(resourceName);
        resource.setExtension("xls");
        InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
        resource.setContents(IOUtils.toByteArray(is));
        final ReportDesign design = new ReportDesign();
        design.setName(reportDesignName);
        design.setReportDefinition(reportDefinition);
        design.setRendererType(ExcelTemplateRenderer.class);
        design.addResource(resource);
        if (properties != null) {
            design.getProperties().putAll(properties);
        }
        if (excelDesignUuid != null && excelDesignUuid.length() > 1) {
            design.setUuid(excelDesignUuid);
        }
        resource.setReportDesign(design);

        return design;
    }
}
