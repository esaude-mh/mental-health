package org.openmrs.module.mentalhealth.reports;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.mentalhealth.MhDataExportManager;
import org.openmrs.module.mentalhealth.reporting.library.dimension.MhCommonDimension;
import org.openmrs.module.mentalhealth.reporting.library.indicator.MhSampleIndicators;
import org.openmrs.module.mentalhealth.utils.MhColumnParameters;
import org.openmrs.module.mentalhealth.utils.MhEmrReportingUtils;
import org.openmrs.module.mentalhealth.utils.MhReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
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
//@Component
public class SampleReport extends MhDataExportManager {

    @Autowired
    private MhSampleIndicators mhSampleIndicators;

    @Autowired
    private MhCommonDimension mhCommonDimension;

	public static final String XLS_TEMPLATE_UUID = "83684b4c-d062-11e8-89d2-938f982d9cb7";
	public static final String REPORT_DEFINITION_UUID = "94110b96-d062-11e8-9f06-a3af958caf7f";
    
    @Override
    public String getExcelDesignUuid() {
        return XLS_TEMPLATE_UUID;//"83684b4c-d062-11e8-89d2-938f982d9cb7";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "sample.xls");
        rd.setName("Sample");
        return rd;
    }

    @Override
    public String getUuid() {
        return REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "Sample Report";
    }

    @Override
    public String getDescription() {
        return "Report to display sample mental health parameters";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(dataSetDefinition()));
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
        return Arrays.asList(new Parameter("startDate", "Data Inicial Inclusão", Date.class),
                            new Parameter("endDate","Data Final Inclusão", Date.class),
                            new Parameter("location", "Unidade Sanitária", Location.class),
                            new Parameter("concept", "Concept", Concept.class),
                            new Parameter("months", "Months", Integer.class),
                            new Parameter("alive", "Alive", Boolean.class),
                            new Parameter("encounter", "Encounter Type", EncounterType.class));
    }

    private DataSetDefinition dataSetDefinition() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.setName("S");

        String indParams = "startDate=${startDate},endDate=${endDate},location=${location},concept=${concept},months=${months},alive=${alive},encounter=${encounter}";
        //add dimensions to the dsd
        dsd.addDimension("gender", MhReportUtils.map(mhCommonDimension.gender()));

        //bulid the column parameters here
        MhColumnParameters male = new MhColumnParameters("male", "Male", "gender=M");
        MhColumnParameters female = new MhColumnParameters("female", "Female", "gender=F");
        MhColumnParameters total = new MhColumnParameters("total", "Total", "");

        //form columns as list to be used in the dsd
        List<MhColumnParameters> allColumns = Arrays.asList(male, female, total);

        //build the patient row passing the dataset and all other parameters
        MhEmrReportingUtils.addRow(dsd, "S1", "", MhReportUtils.map(mhSampleIndicators.allPatients(), indParams), allColumns, Arrays.asList("01", "02", "03"));
        return dsd;
    }
}
