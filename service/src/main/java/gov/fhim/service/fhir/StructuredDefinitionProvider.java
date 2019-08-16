/*******************************************************************************
 * Copyright (c) 2019 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     seanmuir - initial API and implementation
 *
 *******************************************************************************/
package gov.fhim.service.fhir;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.mdmi.ant.GenerateImplementation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import gov.fhim.service.model.UMLService;

/**
 * @author seanmuir
 *
 */
public class StructuredDefinitionProvider implements IResourceProvider {

	ServletContext servletContext;

	FhirContext fhirContext;

	// org.hl7.fhir.StructureDefinition structuredDefinition;

	org.eclipse.uml2.uml.Class theProfile = null;

	/**
	 * @param servletContext
	 * @param fhirContext
	 */
	public StructuredDefinitionProvider(ServletContext servletContext, FhirContext fhirContext) {
		super();
		this.servletContext = servletContext;
		this.fhirContext = fhirContext;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ca.uhn.fhir.rest.server.IResourceProvider#getResourceType()
	 */
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return StructureDefinition.class;
	}

	@Read()
	public StructureDefinition getResourceById(@IdParam IdType theId) {

		return UMLService.INSTANCE(fhirContext).StructureDefinitionFromClass(theProfile);

	}

	@Create
	public MethodOutcome createStructureDefinition(@ResourceParam StructureDefinition theStructureDefinition) {

		MethodOutcome retVal = new MethodOutcome();

		try {
			String result = UMLService.INSTANCE(fhirContext).addClass(servletContext, theStructureDefinition);
			retVal.setId(new IdType("StructureDefinition", result, "1"));
			return retVal;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		retVal.setId(new IdType("StructureDefinition", "3746", "1"));
		return retVal;
	}

	@Update
	public MethodOutcome updateStructureDefinition(@IdParam IdType theId,
			@ResourceParam StructureDefinition theStructureDefinition) {

		// the ID and Version ID for the newly saved resource
		MethodOutcome retVal = new MethodOutcome();

		try {
			String newVersion = "2"; // may be null if the server is not version aware
			retVal.setId(theId.withVersion(newVersion));
			UMLService.INSTANCE(fhirContext).addClass(servletContext, theStructureDefinition);
		} catch (IOException e) {
			// retVal.se
		}

		// This method returns a MethodOutcome object which contains

		return retVal;
	}

	@Search()
	public List<StructureDefinition> getStructuredDefinitionsByName(
			@RequiredParam(name = StructureDefinition.SP_NAME) StringParam theStructureDefinitionName) {
		StructureDefinition structureDefinition = new StructureDefinition();

		structureDefinition.setId("aaaaaaaaaaaaaaaa");

		try {
			return UMLService.INSTANCE(fhirContext).getStructuredDefinitionsByPackage(
				servletContext, theStructureDefinitionName.getValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Collections.singletonList(structureDefinition);
	}

	@Search()
	public List<StructureDefinition> getStructuredDefinitionsByPackage(
			@RequiredParam(name = "packageName") StringParam theStructureDefinitionName) {
		StructureDefinition structureDefinition = new StructureDefinition();

		structureDefinition.setId("aaaaaaaaaaaaaaaa");

		try {
			return UMLService.INSTANCE(fhirContext).getStructuredDefinitionsByPackage(
				servletContext, theStructureDefinitionName.getValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Collections.singletonList(structureDefinition);
	}

	@Operation(name = "$generate")
	public Parameters generate(@IdParam IdType theId) {
		// @formatter:on
		Parameters retVal = new Parameters();

		retVal.addParameter("ID", "SOMEID");

		// l

		;
		String[] references = { "AllergyObservationContainer" };
		try {

			UMLService.INSTANCE(fhirContext).umlService.loadModels(UMLService.INSTANCE(fhirContext), servletContext);

			GenerateImplementation generateImplementation = new GenerateImplementation();

			generateImplementation.setFhimPackage(UMLService.INSTANCE(fhirContext).fhimPackage);
			generateImplementation.setBasePackage(UMLService.INSTANCE(fhirContext).fhirPackage);
			generateImplementation.setTargetModel(UMLService.INSTANCE(fhirContext).targetPackage);

			// generateImplementation.setTarget("FHIM2FHIR-US-Core.uml");
			// generateImplementation.setBase("FHIR-Core.uml");

			// generateImplementation.setResourceSet(UMLService.INSTANCE(fhirContext).getResourceSet());

			// generateImplementation.set
			// generateImplementation.setr

			// generateImplementation.initialize(UMLService.INSTANCE(fhirContext).getFhirProfile(servletContext));
			generateImplementation.go(references);

			// ModelExporter umlExporter = new ModelExporter();

			theProfile = (org.eclipse.uml2.uml.Class) UMLService.INSTANCE(fhirContext).targetPackage.getNestedPackage(
				"Profiles").getOwnedMember("IntoleranceCondition");

			// structuredDefinition = umlExporter.createStrucureDefinition(theProfile);

			// for (UMLService.INSTANCE(fhirContext).targetPackage.getNestedPackage("Profiles").getOwnedComments())
			// {
			//
			// }

		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

}
