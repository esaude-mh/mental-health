package org.openmrs.module.mentalhealth.extension.html;

import org.openmrs.api.context.Context;
import org.openmrs.module.web.extension.PatientDashboardTabExt;

public class PatientDashboardMentalHealthTabExt extends PatientDashboardTabExt {

    @Override
    public String getPortletUrl() {
        return "mentalHealthPortlet";
    }

    @Override
    public String getRequiredPrivilege() {
        return "";
    }

    @Override
    public String getTabId() {
        return "mentalHealth";
    }

    @Override
    public String getTabName() {
        return Context.getMessageSourceService().getMessage("mentalhealth.dashboard.portlet.tab.title");
    }

}
