/*******************************************************************************
 * Copyright (c) 2018 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     seanmuir - initial API and implementation
 *
 *******************************************************************************/
package com.mdix.fhir.terminology.provider.test;

import java.util.ArrayList;
import java.util.Iterator;

import org.hl7.fhir.r4.model.ConceptMap;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

/**
 * @author seanmuir
 *
 */
public class TestDynamoDB {

	static String tableName = "TerminologyMaps";

	static AmazonDynamoDB client;

	static DynamoDB dynamoDB;

	static Table table;

	static FhirContext ourCtx;

	@BeforeClass
	public static void setUpClass() throws Exception {
		tableName = "TerminologyMaps";
		client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		dynamoDB = new DynamoDB(client);
		table = dynamoDB.getTable(tableName);
		ourCtx = FhirContext.forDstu3();
	}

	@Test
	public void testQueryByURIs() {
		QuerySpec spec = new QuerySpec().withKeyConditionExpression(
			"sourceUri = :v_id and targetUri = :t_id").withValueMap(
				new ValueMap().withString(":v_id", "ccc").withString(":t_id", "ddd"));

		ItemCollection<QueryOutcome> items = table.query(spec);

		Iterator<Item> iterator = items.iterator();
		Item item = null;
		while (iterator.hasNext()) {
			item = iterator.next();
			System.out.println(item.toJSONPretty());
			IParser parser = ourCtx.newJsonParser();
			ConceptMap map = parser.parseResource(ConceptMap.class, item.toJSON());
			String encoded = ourCtx.newJsonParser().encodeResourceToString(map);
			System.out.println(encoded);
		}
	}

	@Test
	public void testQueryById() {

		Index index = table.getIndex("IdIndex");

		QuerySpec spec = new QuerySpec().withKeyConditionExpression("id = :v_id").withValueMap(
			new ValueMap().withString(":v_id", "14a61135-1110-458e-a231-976c01577844"));

		ItemCollection<QueryOutcome> items = index.query(spec);

		Iterator<Item> iterator = items.iterator();
		Item item = null;
		while (iterator.hasNext()) {
			item = iterator.next();
			System.out.println(item.toJSONPretty());

			IParser parser = ourCtx.newJsonParser();
			ConceptMap map = parser.parseResource(ConceptMap.class, item.toJSON());

			String encoded = ourCtx.newJsonParser().encodeResourceToString(map);
			System.out.println(encoded);

		}
	}

	@Test
	public void createTable() {

		String tableName = "TerminologyMaps";
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		DynamoDB dynamoDB = new DynamoDB(client);

		// Attribute definitions
		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

		attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("sourceUri").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("targetUri").withAttributeType("S"));

		// Key schema for table
		ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<KeySchemaElement>();
		tableKeySchema.add(new KeySchemaElement().withAttributeName("sourceUri").withKeyType(KeyType.HASH)); // Partition
																												// key
		tableKeySchema.add(new KeySchemaElement().withAttributeName("targetUri").withKeyType(KeyType.RANGE)); // Sort
																												// key

		// Initial provisioned throughput settings for the indexes
		ProvisionedThroughput ptIndex = new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(
			1L);

		// CreateDateIndex
		GlobalSecondaryIndex createIdIndex = new GlobalSecondaryIndex().withIndexName(
			"IdIndex").withProvisionedThroughput(ptIndex).withKeySchema(
				new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH)).withProjection(
					new Projection().withProjectionType("ALL"));

		CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(
			tableName).withProvisionedThroughput(
				new ProvisionedThroughput().withReadCapacityUnits((long) 1).withWriteCapacityUnits(
					(long) 1)).withAttributeDefinitions(attributeDefinitions).withKeySchema(
						tableKeySchema).withGlobalSecondaryIndexes(createIdIndex);

		System.out.println("Creating table " + tableName + "...");
		dynamoDB.createTable(createTableRequest);

		// Wait for table to become active
		System.out.println("Waiting for " + tableName + " to become ACTIVE...");
		try {
			Table table = dynamoDB.getTable(tableName);
			table.waitForActive();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPutItem() {
		Item myItem = Item.fromJSON(
			"{\"resourceType\":\"ConceptMap\",\"id\":\"14a61135-1110-458e-a231-976c01577844\",\"sourceUri\":\"ccc\",\"targetUri\":\"ddd\",\"url\":\"VAVistA\",\"group\":[{\"source\":\"2.16.840.1.113883.6.233\",\"target\":\"http://hl7.org/fhir/v3/ReligiousAffiliation\",\"element\":[{\"code\":\"ADVENTIST\",\"target\":[{\"code\":\"1001\",\"equivalence\":\"equal\"}]},{\"code\":\"SEVENTH DAY ADVENTIST\",\"target\":[{\"code\":\"1001\",\"equivalence\":\"equal\"}]},{\"code\":\"AFRICAN RELIGIONS\",\"target\":[{\"code\":\"1002\",\"equivalence\":\"equal\"}]},{\"code\":\"AFRO-CARIBBEAN RELIGIONS\",\"target\":[{\"code\":\"1003\",\"equivalence\":\"equal\"}]},{\"code\":\"AGNOSTICISM\",\"target\":[{\"code\":\"1004\",\"equivalence\":\"equal\"}]},{\"code\":\"AGNOSTIC\",\"target\":[{\"code\":\"1004\",\"equivalence\":\"equal\"}]},{\"code\":\"ANGLICAN\",\"target\":[{\"code\":\"1005\",\"equivalence\":\"equal\"}]},{\"code\":\"ANIMISM\",\"target\":[{\"code\":\"1006\",\"equivalence\":\"equal\"}]}]}]}\n" +
					"");
		table.putItem(myItem);
	}

}
