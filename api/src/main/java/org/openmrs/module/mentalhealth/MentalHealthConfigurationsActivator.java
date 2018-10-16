/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mentalhealth;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.mentalhealth.activator.MhAppConfigurationInitializer;
import org.openmrs.module.mentalhealth.activator.MhHtmlFormsInitializer;
import org.openmrs.module.mentalhealth.activator.MhInitializer;
import org.openmrs.module.mentalhealth.activator.MhReportsInitializer;
import org.openmrs.module.mentalhealth.deploy.MentalHealthCommonMetadataBundle;
import org.openmrs.module.mentalhealth.handlers.FieldsetHandler;
import org.openmrs.module.mentalhealth.handlers.InputHandler;
import org.openmrs.module.mentalhealth.handlers.LabelHandler;
import org.openmrs.module.mentalhealth.handlers.SelectHandler;
import org.openmrs.module.mentalhealth.handlers.TextAreaHandler;
import org.openmrs.module.mentalhealth.handlers.OptionHandler;
import org.openmrs.module.metadatadeploy.api.MetadataDeployService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class MentalHealthConfigurationsActivator extends BaseModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());
		
	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Mental Health Configurations Module");
	}
	
	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Mental Health Configurations Module refreshed");
		
		final HtmlFormEntryService hfeService = Context.getService(HtmlFormEntryService.class);
        
		hfeService.addHandler("input", new InputHandler());
		hfeService.addHandler("label", new LabelHandler());
		
		hfeService.addHandler("fieldset", new FieldsetHandler());
		
		hfeService.addHandler("textarea", new TextAreaHandler());
		
		hfeService.addHandler("select", new SelectHandler());
		hfeService.addHandler("option", new OptionHandler());

	}
	
	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Mental Health Configurations Module");
	}
	
	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		MetadataDeployService deployService = Context.getService(MetadataDeployService.class);


		// install commonly used metadata
		installCommonMetadata(deployService);

		// run the initializers
		for (MhInitializer initializer : getInitializers()) {
			initializer.started();
		}

		log.info("Aihd Configurations Module started");
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Aihd Configurations Module");
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Aihd Configurations Module stopped");
	}

	private List<MhInitializer> getInitializers() {
		List<MhInitializer> l = new ArrayList<MhInitializer>();
		l.add(new MhAppConfigurationInitializer());
		l.add(new MhHtmlFormsInitializer());
		l.add(new MhReportsInitializer());
		return l;
	}

	private void installCommonMetadata(MetadataDeployService deployService) {
		try {
			deployService.installBundle(Context.getRegisteredComponents(MentalHealthCommonMetadataBundle.class).get(0));


		}
		catch (Exception e) {
			Module mod = ModuleFactory.getModuleById("mentalhealth");
			ModuleFactory.stopModule(mod);
			throw new RuntimeException("failed to install the common metadata ", e);
		}
	}


}
