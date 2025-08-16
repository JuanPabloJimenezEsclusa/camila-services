package com.camila.api.product.infrastructure.adapter.output.mongo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public abstract class MongoContainerConfig {

    private static final String DATABASE_NAME = "camila-product-db";
    private static final String COLLECTION_NAME = "products";
    private static final String PRODUCTS_JSON = Objects.requireNonNull(MongoContainerConfig.class
      .getClassLoader().getResource("scripts/mongo/products.json")
    ).getPath();

    private static final DockerImageName MONGO_IMAGE = DockerImageName
      .parse("mongodb/mongodb-community-server")
      .withTag("8.0.12-ubi9");

    private static final MongoDBContainer container = new MongoDBContainer(MONGO_IMAGE)
      .withStartupTimeout(Duration.ofMinutes(1L))
      .withReuse(true)
      .withCreateContainerCmdModifier(cmd ->
        Objects.requireNonNull(cmd
            .withName("camila-mongodb-testing-%s".formatted(UUID.randomUUID()))
            .getHostConfig())
          .withMemory(1024L * 1024 * 1024)
          .withMemorySwap(1024L * 1024 * 1024)
          .withMemorySwappiness(2L * 1024 * 1024 * 1024)
          .withCpuCount(1L));

    static {
        container.start();
        updateDataSourceProps();
        initDatabase();
    }

    private static void initDatabase() {
        try (var mongoClient = com.mongodb.client.MongoClients.create(container.getConnectionString())) {
            var database = mongoClient.getDatabase(DATABASE_NAME);
            var collection = database.getCollection(COLLECTION_NAME);

            final var productJsonFile = Paths.get(PRODUCTS_JSON).toFile();
            if (!productJsonFile.exists()) {
                throw new RuntimeException("File not found: %s".formatted(productJsonFile.getAbsolutePath()));
            }

            final List<Map<String, Object>> products = new ObjectMapper().readValue(
                    Files.readAllBytes(productJsonFile.toPath()),
                    new TypeReference<>() { }
            );

            products.forEach(productMap -> collection.insertOne(new Document(productMap)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize MongoDB container", e);
        }
    }

    private static void updateDataSourceProps() {
        System.setProperty("spring.data.mongodb.uri", container.getConnectionString());
        System.setProperty("spring.data.mongodb.database", DATABASE_NAME);
    }
}
