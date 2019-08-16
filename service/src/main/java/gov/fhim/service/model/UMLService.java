/*******************************************************************************
 * Copyright (c) 2011 Sean Muir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sean Muir - initial API and implementation
 *
 * $Id$
 *******************************************************************************/
package gov.fhim.service.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.mdht.uml.fhir.FHIRPackage;
import org.eclipse.mdht.uml.fhir.transform.ModelExporter;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPlugin;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.eclipse.uml2.uml.util.UMLSwitch;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.util.FhirResourceFactoryImpl;
import org.mdmi.uml.profile.MDMIPackage;

import ca.uhn.fhir.context.FhirContext;

public class UMLService {

	public static UMLService umlService = null;

	private FhirContext fhirContext = null;

	/**
	 * @param fhirContext2
	 */
	public UMLService(FhirContext fhirContext2) {
		fhirContext = fhirContext2;
	}

	static public UMLService INSTANCE(FhirContext fhirContext) {
		if (umlService == null) {
			umlService = new UMLService(fhirContext);
		}
		return umlService;
	}

	public static Package fhimPackage = null;

	public static Package fhirPackage = null;

	public static Package targetPackage = null;

	static Profile fhirProfile = null;

	Resource umlResource = null;

	// Resource fhirProfileResource = null;

	ModelExporter me = new ModelExporter();

	public void saveUml(ServletContext sce) throws IOException {

		if (fhimPackage == null) {
			return;
		}

		String tempPath = (String) sce.getAttribute("javax.servlet.context.tempdir");

		URI modelURI = URI.createFileURI(tempPath + "/FHIM.uml");

		umlResource.setURI(modelURI);

		umlResource.save(null);
	}

	public String addClass(ServletContext sce, StructureDefinition structureDefinition) throws IOException {

		if (fhimPackage == null) {
			umlService.loadModels(this, sce);
		}

		Package praxisPackage = (Package) fhimPackage.getPackagedElement("PRAXIS");

		if (praxisPackage == null) {
			praxisPackage = (Package) fhimPackage.createPackagedElement("PRAXIS", UMLPackage.eINSTANCE.getPackage());
		}

		Class profileClass = praxisPackage.createOwnedClass(structureDefinition.getName(), false);

		for (ElementDefinition elementDefinition : structureDefinition.getSnapshot().getElement()) {
			profileClass.createOwnedAttribute(
				elementDefinition.getShort(),
				fhimPackage.getOwnedType("string", true, UMLPackage.eINSTANCE.getPrimitiveType(), false));
		}

		return profileClass.getQualifiedName();

	}

	public StructureDefinition StructureDefinitionFromClass(Class theClass) {

		org.hl7.fhir.StructureDefinition sd = me.createStrucureDefinition(theClass);

		if (sd != null) {
			URI resourceURI = URI.createFileURI(theClass.getName() + ".xml");

			FhirResourceFactoryImpl fhirResourceFactory = new FhirResourceFactoryImpl();
			Resource resource = fhirResourceFactory.createResource(resourceURI);
			resource.getContents().add(sd);
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resource.save(baos, null);
				System.out.println(baos.toString());
				IBaseResource structureDefinition = fhirContext.newXmlParser().parseResource(baos.toString());
				return (StructureDefinition) structureDefinition;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Classifier getClassifierByName(Package basePackage, final String localName, final EClass eClass) {
		if (basePackage == null || localName == null) {
			return null;
		}

		Classifier classifier = null;

		UMLSwitch<Object> umlSwitch = new UMLSwitch<Object>() {
			@Override
			public Object caseClassifier(Classifier classifier) {
				System.out.println(classifier.getName());
				if (localName.equalsIgnoreCase(classifier.getName())) {
					if (eClass == null) {
						return classifier;
					} else {
						return eClass == classifier.eClass()
								? classifier
								: null;
					}
				} else {
					return null;
				}
			}

			@Override
			public Object casePackage(Package pkg) {
				Object result = null;
				for (NamedElement namedElement : pkg.getOwnedMembers()) {
					result = doSwitch(namedElement);
					if (result != null) {
						break;
					}
				}
				return result;
			}
		};

		classifier = (Classifier) umlSwitch.doSwitch(basePackage);

		return classifier;
	}

	public StructureDefinition getStructureDefinition(ServletContext sce, String umlClassName) throws IOException {

		if (fhimPackage == null) {
			umlService.loadModels(this, sce);
		}

		Class theClass = (Class) getClassifierByName(fhimPackage, umlClassName, null);
		return StructureDefinitionFromClass(theClass);

		// org.hl7.fhir.StructureDefinition sd = me.createStrucureDefinition(theClass);
		//
		// if (sd != null) {
		// URI resourceURI = URI.createFileURI(umlClassName + ".xml");
		//
		// FhirResourceFactoryImpl fhirResourceFactory = new FhirResourceFactoryImpl();
		// Resource resource = fhirResourceFactory.createResource(resourceURI);
		// resource.getContents().add(sd);
		// try {
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// resource.save(baos, null);
		// System.out.println(baos.toString());
		// IBaseResource structureDefinition = fhirContext.newXmlParser().parseResource(baos.toString());
		// return (StructureDefinition) structureDefinition;
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }

		// return null;

	}

	ResourceSet resourceSet;

	/**
	 * @return the resourceSet
	 */
	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public List<StructureDefinition> getStructuredDefinitionsByPackage(ServletContext sce, String packageName)
			throws IOException {
		umlService.loadModels(this, sce);

		List<StructureDefinition> result = new ArrayList<StructureDefinition>();

		for (PackageableElement p : fhimPackage.getPackagedElements()) {
			System.out.println(packageName);
			System.out.println(p.getName());
			if (p instanceof Package && packageName.equalsIgnoreCase(p.getName())) {
				Package thePackage = (Package) p;
				for (Type t : thePackage.getOwnedTypes()) {

					if (t instanceof org.eclipse.uml2.uml.Class) {
						result.add(StructureDefinitionFromClass((Class) t));
					}
				}
			}
		}

		return result;

	}

	/**
	 * @param servletContext
	 * @return
	 * @throws IOException
	 */
	public Profile getFhirProfile(ServletContext sce) throws IOException {

		// FHIRFactory.eINSTANCE.

		if (fhirProfile == null) {
			umlService.loadModels(this, sce);
		}
		return fhirProfile;
	}

	public void loadModels(UMLService umlService, ServletContext sce) throws IOException {

		if (fhimPackage != null) {
			return;
		}

		UMLResourcesUtil.initGlobalRegistries();

		ResourceSet resourceSet = new ResourceSetImpl();
		UMLResourcesUtil.init(resourceSet);
		resourceSet.getPackageRegistry().put(MDMIPackage.eNS_URI, MDMIPackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(FHIRPackage.eNS_URI, FHIRPackage.eINSTANCE);

		UMLPlugin.getEPackageNsURIToProfileLocationMap().put(
			MDMIPackage.eNS_URI, URI.createURI("pathmap://MDMI_PROFILE/mdmi.profile.uml#_cxOJEIEVEd6H8o-hO3-B4Q"));

		UMLPlugin.getEPackageNsURIToProfileLocationMap().put(
			FHIRPackage.eNS_URI, URI.createURI("pathmap://MDHT_FHIR/FHIR.profile.uml#_dtdbUDDkEeWn6bT0Ftydnw"));

		String mdmiJar = "jar:file:/Users/seanmuir/git/FHIMProfileBuilderService/gov.fhim.service.fhir/target/gov.fhim.service.fhir/WEB-INF/lib/org.mdmi.resource-1.5.0-SNAPSHOT.jar!/";

		String fhirJar = "jar:file:/Users/seanmuir/git/FHIMProfileBuilderService/gov.fhim.service.fhir/target/gov.fhim.service.fhir/WEB-INF/lib/org.eclipse.mdht.uml.fhir-0.5.0-SNAPSHOT.jar!/";

		URI mdmiPathmapURI = URI.createURI("pathmap://MDMI_PROFILE/");
		URI mdmiJarURI = URI.createURI(mdmiJar);
		URI fhirPathmap = URI.createURI("pathmap://MDHT_FHIR/");
		URI fhirJarURI = URI.createURI(fhirJar);

		resourceSet.getURIConverter().getURIMap().put(
			mdmiPathmapURI, mdmiJarURI.appendSegment("model").appendSegment(""));
		resourceSet.getURIConverter().getURIMap().put(fhirPathmap, fhirJarURI.appendSegment("model").appendSegment(""));

		URI modelFile = URI.createFileURI("fhim.uml");

		Resource fhimResource = resourceSet.createResource(modelFile);

		fhimResource.load(sce.getResourceAsStream("/WEB-INF/model/FHIM.uml"), null);

		fhimPackage = (Package) EcoreUtil.getObjectByType(
			fhimResource.getContents(), UMLPackage.eINSTANCE.getPackage());

		if (fhimPackage != null) {
			EcoreUtil.resolveAll(UMLService.fhimPackage);
		}

		URI fhirBaseFile = URI.createFileURI("FHIR-Core.uml");

		Resource fhirResource = resourceSet.createResource(fhirBaseFile);

		fhirResource.load(sce.getResourceAsStream("/WEB-INF/model/FHIR-Core.uml"), null);

		fhirPackage = (Package) EcoreUtil.getObjectByType(
			fhirResource.getContents(), UMLPackage.eINSTANCE.getPackage());

		if (fhirPackage != null) {
			EcoreUtil.resolveAll(UMLService.fhirPackage);
		}

		Profile fhirProfile = UMLUtil.getProfile(MDMIPackage.eINSTANCE, fhirPackage);
		Profile mdmiProfile = UMLUtil.getProfile(FHIRPackage.eINSTANCE, fhirPackage);

		URI targetFile = URI.createFileURI(sce.getRealPath("/WEB-INF/model") + "/targetfhir.uml");
		Resource targetResource = resourceSet.createResource(targetFile);

		targetPackage = UMLFactory.eINSTANCE.createPackage();
		targetPackage.setName("targetpackage");

		targetResource.getContents().add(targetPackage);

		targetPackage.applyProfile(fhirProfile);
		targetPackage.applyProfile(mdmiProfile);

		// System.out.println(fhirProfile);
		// System.out.println(mdmiProfile);

		Map<String, String> saveOptions = new HashMap<String, String>();
		targetResource.save(saveOptions);

		System.out.println("FHIMPACKAGELOADED");
		// UMLResourcesUtil.initEPackageNsURIToProfileLocationMap(ePackageNsURIToProfileLocationMap)

		;

		// UMLResourcesUtil.

		// UMLResourcesUtil.initEPackageNsURIToProfileLocationMap

		// if (false && UMLService.fhimPackage != null) {
		// return;
		// }
		//
		// Profile foobar4 = UMLUtil.getProfile(MDMIPackage.eINSTANCE, MDMIPackage.eINSTANCE.getMappedElement());
		//
		// umlService.resourceSet = new ResourceSetImpl();
		//
		// UMLPackage.eINSTANCE.eClass();
		// // Initialize registry
		// Registry packageRegistry = umlService.resourceSet.getPackageRegistry();
		// packageRegistry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		// packageRegistry.put(Ecore2XMLPackage.eNS_URI, Ecore2XMLPackage.eINSTANCE);
		// packageRegistry.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		//
		// MDMIPackage.eINSTANCE.getMappedElement();
		// packageRegistry.put(MDMIPackage.eNS_URI, MDMIPackage.eINSTANCE);
		//
		// // Initialize pathmaps
		// umlService.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
		// UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		// Map<URI, URI> uriMap = umlService.resourceSet.getURIConverter().getURIMap();
		//
		// URL umlPluginLocation = Package.class.getProtectionDomain().getCodeSource().getLocation();
		//
		// // Create and Add UML PathMaps
		// URI uri = URI.createURI(umlPluginLocation.getPath());
		// uriMap.put(URI.createURI(UMLResource.LIBRARIES_PATHMAP), uri.appendSegment("libraries").appendSegment(""));
		// uriMap.put(URI.createURI(UMLResource.METAMODELS_PATHMAP), uri.appendSegment("metamodels").appendSegment(""));
		// uriMap.put(URI.createURI(UMLResource.PROFILES_PATHMAP), uri.appendSegment("profiles").appendSegment(""));
		//
		// String foo2 =
		// "jar:file:/Users/seanmuir/git/FHIMProfileBuilderService/gov.fhim.service.fhir/target/gov.fhim.service.fhir/WEB-INF/lib/org.mdmi.resource-1.5.0-SNAPSHOT.jar!/";
		//
		// String foo3 =
		// "jar:file:/Users/seanmuir/git/FHIMProfileBuilderService/gov.fhim.service.fhir/target/gov.fhim.service.fhir/WEB-INF/lib/org.mdmi.resource-1.5.0-SNAPSHOT.jar!/model/mdmi.profile.uml";
		//
		// URI mdmiresourceURI = URI.createURI(foo2);
		// URI mdmiPathmap = URI.createURI("pathmap://MDMI_PROFILE/");
		// uriMap.put(mdmiPathmap, mdmiresourceURI.appendSegment("model").appendSegment(""));
		// UMLPlugin.getEPackageNsURIToProfileLocationMap().put(
		// MDMIPackage.eNS_URI, URI.createURI("pathmap://MDMI_PROFILE/mdmi.profile.uml#_cxOJEIEVEd6H8o-hO3-B4Q"));
		//
		// String foo =
		// "jar:file:/Users/seanmuir/git/FHIMProfileBuilderService/gov.fhim.service.fhir/target/gov.fhim.service.fhir/WEB-INF/lib/org.eclipse.mdht.uml.fhir-0.5.0-SNAPSHOT.jar!/model/FHIR.profile.uml";
		//
		// // URL umlFHIRProfileLocation = FHIRPackage.class.getProtectionDomain().getCodeSource().getLocation();
		//
		// UMLResource profileResource = (UMLResource) resourceSet.getResource(URI.createURI(foo), true);
		//
		// Profile fhirProfile = (Profile) profileResource.getContents().get(0);
		//
		// UMLResource profileResource2 = (UMLResource) resourceSet.getResource(URI.createURI(foo3), true);
		//
		// Profile mdmiProfile = (Profile) profileResource2.getContents().get(0);
		//
		// Profile foobar2 = UMLUtil.getProfile(MDMIPackage.eINSTANCE, MDMIPackage.eINSTANCE.getMappedElement());
		//
		// /*
		// * URI modelFile = URI.createFileURI("src/m3o-profiled.uml");
		// * URI profileFile = URI.createFileURI("src/OWL.profile.uml");
		// * UMLResource modelResource = (UMLResource)resourceSet.getResource(modelFile, true);
		// * UMLResource profileResource = (UMLResource)resourceSet.getResource(profileFile, true);
		// *
		// */
		// // URI fhiruri = URI.createURI(foo);
		//
		// // uriMap.put(
		// // URI.createURI(org.eclipse.mdht.uml.fhir.util.ProfileUtil.PROFILES_PATHMAP),
		// // fhiruri.appendSegment("model").appendSegment(""));
		//
		// umlService.resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
		//
		// // Open the model
		// URI modelFile = URI.createFileURI("fhim.uml");
		//
		// Resource fhimResource = umlService.resourceSet.createResource(modelFile);
		//
		// fhimResource.load(sce.getResourceAsStream("/WEB-INF/model/FHIM.uml"), null);
		//
		// UMLService.fhimPackage = (Package) EcoreUtil.getObjectByType(
		// fhimResource.getContents(), UMLPackage.eINSTANCE.getPackage());
		//
		// if (UMLService.fhimPackage != null) {
		// EcoreUtil.resolveAll(UMLService.fhimPackage);
		// }
		//
		// URI fhirBaseFile = URI.createFileURI("FHIR-Core.uml");
		//
		// Resource fhirResource = umlService.resourceSet.createResource(fhirBaseFile);
		//
		// fhirResource.load(sce.getResourceAsStream("/WEB-INF/model/FHIR-Core.uml"), null);
		//
		// UMLService.fhirPackage = (Package) EcoreUtil.getObjectByType(
		// fhirResource.getContents(), UMLPackage.eINSTANCE.getPackage());
		//
		// if (UMLService.fhimPackage != null) {
		// EcoreUtil.resolveAll(UMLService.fhimPackage);
		// }
		//
		// // String modelName = uri.lastSegment();
		// // modelName = modelName.substring(0, modelName.lastIndexOf("."));
		//
		// targetPackage = UMLFactory.eINSTANCE.createPackage();
		// targetPackage.setName("target");
		//
		// if (FHIRPackage.eINSTANCE instanceof org.eclipse.uml2.uml.Package) {
		// System.out.println("i guess");
		// }
		//
		// // Profile fhirProfile = (Profile) getPackage(FHIRPackage.eINSTANCE);
		// //
		// // fhirProfile = (Profile) getPackage(MDMIPackage.eINSTANCE);
		//
		// // EList<Profile> elist = new BasicEList<Profile>();
		// // targetPackage.applyProfiles(elist);
		//
		// ProfileApplication pa = targetPackage.createProfileApplication();
		// // pa.setAppliedProfile(arg0);
		// // UML2Util.getp
		//
		// // targetPackage.applyProfile(fhirProfile);
		// // targetPackage.applyProfile(mdmiProfile);
		//
		// String webInfPath = sce.getRealPath("/WEB-INF/model");
		//
		// // Profile::define();
		//
		// // Profile p = null;
		// // p.define();
		//
		// URI targetFile = URI.createFileURI(webInfPath + "/targetfhir.uml");
		//
		// Resource targetResource = resourceSet.createResource(targetFile);
		// targetResource.getContents().add(targetPackage);
		//
		// Map<String, String> saveOptions = new HashMap<String, String>();
		// targetResource.save(saveOptions);
		//
		// // URI profleFile = URI.createFileURI("FHIR.profile.uml");
		// //
		// // umlService.fhirProfileResource = umlService.resourceSet.createResource(profleFile);
		// //
		// // umlService.fhirProfileResource.load(sce.getResourceAsStream("/WEB-INF/model/FHIR.profile.uml"), null);
		// //
		// // UMLService.fhirProfile = (Profile) umlService.fhirProfileResource.getContents().get(0);
		//
		// // (Profile) EcoreUtil.getObjectByType(
		// // fhirProfileResource.getContents(), UMLPackage.eINSTANCE.getProfile());
		// System.out.println(UMLService.fhirProfile);

	}

	public static org.eclipse.uml2.uml.Package getPackage(EPackage definition) {
		EObject eContainer = definition.eContainer();

		if (eContainer instanceof EAnnotation) {
			EAnnotation eAnnotation = (EAnnotation) eContainer;

			if (UML2_UML_PACKAGE_2_0_NS_URI.equals(eAnnotation.getSource())) {

				eContainer = eAnnotation.eContainer();

				if (eContainer instanceof org.eclipse.uml2.uml.Package) {
					return (org.eclipse.uml2.uml.Package) eContainer;
				}
			}
		}

		return null;
	}

	public static final String UML2_UML_PACKAGE_2_0_NS_URI = "http://www.eclipse.org/uml2/2.0.0/UML"; //$NON-NLS-1$

}
