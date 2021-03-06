package org.openmrs.module.mentalhealth.elements;

import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.mentalhealth.elements.interfaces.IChildElement;
import org.openmrs.module.mentalhealth.elements.interfaces.IHandleHTMLEdit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OptionElement extends TranslatingElement implements IHandleHTMLEdit, IChildElement{

	/*
	 * Instance Initialization Block (IIB) in Java
	 * Initialization blocks are executed whenever the class is initialized and before constructors are invoked.
	 * They are typically placed above the constructors within braces.
	 * It is not at all necessary to include them in your classes.
	*/
	
	private ParentElement m_parentSelect;
	
	public OptionElement(FormEntrySession session, Map<String, String> parameters, Node originalNode, ParentElement parentElement) {
		super(session, parameters, originalNode);
		
		/*
		 * form
This attribute lets you specify the form element to which the select element is associated (that is, its "form owner"). If this attribute is specified, its value must be the same as the id of a form element in the same document. This enables you to place select elements anywhere within a document, not just as descendants of their form elements.
		 * */
		
		m_parentSelect = parentElement;
		
		m_parentSelect.addHTMLValueConceptMapping(this);
	
	}
	
	@Override
	public void takeActionForEditMode(FormEntryContext context) {

		if(m_openMRSConcept == null || m_parentSelect == null) {
			return;
		}
		
		if((Boolean)m_parentSelect.getValueStoredInOpenMRS(this)) {
			
			((Element)m_originalNode).setAttribute("selected", "true");
			
		} else {

			((Element)m_originalNode).removeAttribute("selected");
			
		}
	}

	@Override
	protected boolean requiresName() {
		return false;
	}
	
	@Override
	protected boolean requiresValue() {
		return true;
	}
	
	@Override
	public Concept getConcept() {
		return m_openMRSConcept;
	}

	@Override
	public Map<String, String> getAttrs() {
		return m_parameters;
	}

	@Override
	public Object getDefaultStateFromNode() {
		Boolean selected = m_parameters.get("selected")!=null;
		return selected;
	}

	@Override
	public String getTagName() {
		// TODO Auto-generated method stub
		return "option";
	}

}
