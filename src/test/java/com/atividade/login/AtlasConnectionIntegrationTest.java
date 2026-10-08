package com.atividade.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@EnabledIfSystemProperty(named = "atlas.integration.enabled", matches = "true")
@SpringJUnitConfig(AtlasConnectionIntegrationTest.MongoTestConfiguration.class)
@TestPropertySource(locations = "classpath:application.properties")
class AtlasConnectionIntegrationTest {

	private static final String COLLECTION_NAME = "teste_conexao_atlas";
	private static final String DATABASE_NAME = "login_seguro";
	private static final String TEST_VALUE = "documento-ficticio";

	@Autowired
	private MongoTemplate mongoTemplate;

	@Value("${spring.mongodb.uri}")
	private String mongoUri;

	@Test
	void deveInserirConsultarEExcluirDocumentoNoAtlas() {
		assertTrue(isAtlasUri(), "MONGODB_URI deve apontar para o MongoDB Atlas");

		String documentId = "teste-" + UUID.randomUUID();
		Query documentQuery = Query.query(Criteria.where("_id").is(documentId));
		Document testDocument = new Document("_id", documentId)
				.append("valor", TEST_VALUE);
		boolean documentInserted = false;

		try {
			assertFalse(mongoTemplate.exists(documentQuery, COLLECTION_NAME));

			mongoTemplate.insert(testDocument, COLLECTION_NAME);
			documentInserted = true;

			Document recoveredDocument = mongoTemplate.findById(
					documentId,
					Document.class,
					COLLECTION_NAME);

			assertNotNull(recoveredDocument);
			assertEquals(documentId, recoveredDocument.getString("_id"));
			assertEquals(TEST_VALUE, recoveredDocument.getString("valor"));
		} finally {
			if (documentInserted) {
				long deletedDocuments = mongoTemplate
						.remove(documentQuery, COLLECTION_NAME)
						.getDeletedCount();

				assertEquals(1, deletedDocuments);
				assertFalse(mongoTemplate.exists(documentQuery, COLLECTION_NAME));
			}
		}
	}

	private boolean isAtlasUri() {
		return mongoUri.startsWith("mongodb+srv://")
				&& mongoUri.contains(".mongodb.net")
				&& !mongoUri.contains("localhost")
				&& !mongoUri.contains("127.0.0.1");
	}

	@Configuration(proxyBeanMethods = false)
	static class MongoTestConfiguration {

		@Bean
		MongoClient mongoClient(@Value("${spring.mongodb.uri}") String mongoUri) {
			return MongoClients.create(mongoUri);
		}

		@Bean
		MongoTemplate mongoTemplate(
				MongoClient mongoClient,
				@Value("${spring.mongodb.uri}") String mongoUri) {
			String configuredDatabase = new ConnectionString(mongoUri).getDatabase();

			if (!DATABASE_NAME.equals(configuredDatabase)) {
				throw new IllegalStateException(
						"MONGODB_URI deve selecionar o banco login_seguro");
			}

			return new MongoTemplate(mongoClient, configuredDatabase);
		}
	}
}
