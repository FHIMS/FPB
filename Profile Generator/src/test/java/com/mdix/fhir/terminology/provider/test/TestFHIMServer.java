package com.mdix.fhir.terminology.provider.test;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;

public class TestFHIMServer {

	// // http://localhost:8080/fhir/ConceptMap/$translate?code=C0349375&source=xxxxxxxxxx&target=1111222233334444
	// // VAVistA 2.16.840.1.113883.6.233 http://hl7.org/fhir/ValueSet/v3-ReligiousAffiliation
	//
	// // http://localhost:8080/fhir/ConceptMap/$translate?code=DIVINATION&source=VAVistA&target=http://hl7.org/fhir/ValueSet/v3-ReligiousAffiliation
	private static IGenericClient client;

	private static FhirContext ourCtx = FhirContext.forR4();

	private static int ourPort = 8180;

	private static String HOST = "http://localhost:";

	@BeforeClass
	public static void setUpClass() {
		client = ourCtx.newRestfulGenericClient("http://localhost:8180/fhim/fhir");
		client.setEncoding(EncodingEnum.JSON);
	}

	//
	// public static List<String> loadMapsxx(String directory) {
	//
	// // FhirContext fhirContext = FhirContext.forDstu3();
	// List<String> fileNames = new ArrayList<>();
	// try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
	// for (Path path : directoryStream) {
	// ConceptMap conceptMapFromTo = new ConceptMap();
	// ConceptMap conceptMapToFrom = new ConceptMap();
	// ConceptMapGroupComponent cmgcFromTo = conceptMapFromTo.addGroup();
	// ConceptMapGroupComponent cmgcToFrom = conceptMapToFrom.addGroup();
	// boolean firstLine = true;
	//
	// for (String line : Files.readAllLines(path)) {
	// String[] code2code = line.toString().split("\t");
	// if (firstLine) {
	// firstLine = false;
	//
	// if (code2code.length == 4) {
	//
	// UriType sourceuri = new UriType();
	// sourceuri.setValue(code2code[0]);
	//
	// conceptMapFromTo.setSource(sourceuri);
	// conceptMapToFrom.setTarget(sourceuri);
	//
	// UriType targeturi = new UriType();
	// targeturi.setValue(code2code[2]);
	//
	// conceptMapFromTo.setTarget(targeturi);
	// conceptMapToFrom.setSource(targeturi);
	//
	// cmgcFromTo.setSource(code2code[1]);
	// cmgcFromTo.setTarget(code2code[3]);
	//
	// cmgcToFrom.setTarget(code2code[1]);
	// cmgcToFrom.setSource(code2code[3]);
	//
	// } else {
	// System.out.println("invalid " + line);
	// }
	// } else {
	// if (code2code.length == 4) {
	//
	// SourceElementComponent secFromTo = cmgcFromTo.addElement();
	// CodeType aaa = new CodeType();
	// secFromTo.setCodeElement(aaa);
	// secFromTo.setCode(code2code[0]).addTarget().setCode(code2code[2]).setEquivalence(
	// ConceptMapEquivalence.EQUAL);
	//
	// SourceElementComponent secToFrom = cmgcToFrom.addElement();
	// CodeType aaa2 = new CodeType();
	// secToFrom.setCodeElement(aaa2);
	// secToFrom.setCode(code2code[2]).addTarget().setCode(code2code[0]).setEquivalence(
	// ConceptMapEquivalence.EQUAL);
	//
	// } else {
	// System.out.println("invalid " + line);
	// }
	// }
	// }
	//
	// System.out.println(
	// "Appointment JSon::" +
	// ourCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(conceptMapFromTo));
	//
	// client.setEncoding(EncodingEnum.JSON);
	// final MethodOutcome results = client.create().resource(conceptMapFromTo).prefer(
	// PreferReturnEnum.REPRESENTATION).execute();
	// System.out.println(results.getId());
	// client.create().resource(conceptMapToFrom).prefer(PreferReturnEnum.REPRESENTATION).execute();
	// }
	// } catch (IOException ex) {
	// }
	// return fileNames;
	// }
	//
	@Test
	public void queryStructureDefinitionByPackage() {

		StringClientParam thePackageName = new StringClientParam("packageName");

		// thePackageName.matches()

		// Perform a search
		Bundle results = client.search().forResource(StructureDefinition.class).where(
			thePackageName.matches().value("Allergies")).returnBundle(Bundle.class).execute();

		System.out.println(ourCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(results));
		// System.out.println(results);

		// TerminologyUtil.load(client, "src/test/resources/mappings/boom");
	}

	@Test
	public void queryStructureDefinitioByNamen() {

		// Perform a search
		Bundle results = client.search().forResource(StructureDefinition.class).where(
			StructureDefinition.NAME.matches().value("duck")).returnBundle(Bundle.class).execute();

		System.out.println(results);

		// TerminologyUtil.load(client, "src/test/resources/mappings/boom");
	}

	@Test
	public void getStructureDefinition() {
		StructureDefinition foo = client.read().resource(StructureDefinition.class).withId(
			"reactionobservation").execute();
	}

	@Test
	public void createStructureDefinition() {
		StructureDefinition structureDefinition = new StructureDefinition();

		structureDefinition.setName("NAMEOFSD");
		MethodOutcome methodOutcome = client.create().resource(structureDefinition).execute();

		StructureDefinition foo = client.read().resource(StructureDefinition.class).withId("NAMEOFSD").execute();

	}

	@Test
	public void updateStructureDefinition() {
		StructureDefinition structureDefinition = new StructureDefinition();
		structureDefinition.addIdentifier().setValue("aaaaaaaaaaaaa");
		structureDefinition.setId("bbbbbb");
		structureDefinition.setName("NAMEOFSD");
		MethodOutcome methodOutcome = client.update().resource(structureDefinition).execute();

	}

	@Test
	public void generateProfile() {
		StructureDefinition structureDefinition = new StructureDefinition();

		Class theOutputParameterType;
		Parameters foo = client.operation().onInstance(new IdDt("StructureDefinition", "1")).named(
			"$generate").withNoParameters(Parameters.class).execute();

		StructureDefinition foo2 = client.read().resource(StructureDefinition.class).withId(
			"reactionobservation").execute();

		System.out.println(ourCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(foo2));

	}

}
