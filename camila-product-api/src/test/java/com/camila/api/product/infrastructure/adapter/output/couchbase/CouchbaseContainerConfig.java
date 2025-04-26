package com.camila.api.product.infrastructure.adapter.output.couchbase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.shaded.okhttp3.Credentials;
import org.testcontainers.shaded.okhttp3.FormBody;
import org.testcontainers.shaded.okhttp3.OkHttpClient;
import org.testcontainers.shaded.okhttp3.Request;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public abstract class CouchbaseContainerConfig {
  private static final String BUCKET_NAME = "camila-product-bucket";
  private static final String BUCKET_SCOPE = "product";
  private static final String BUCKET_COLLECTION = "products";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "admin1234";

  private static final String PRODUCTS_JSON = Objects.requireNonNull(
    CouchbaseContainerConfig.class.getClassLoader().getResource("scripts/couchbase/products.json")
  ).getPath();

  private static final BucketDefinition BUCKET_DEFINITION = new BucketDefinition(BUCKET_NAME);

  private static final DockerImageName COUCHBASE_IMAGE = DockerImageName
    .parse("couchbase")
    .asCompatibleSubstituteFor("couchbase/server")
    .withTag("7.6.5");

  private static final CouchbaseContainer container = new CouchbaseContainer(COUCHBASE_IMAGE)
    .withCredentials(USERNAME, PASSWORD)
    .withBucket(BUCKET_DEFINITION)
    .withStartupTimeout(Duration.ofMinutes(3L))
    .waitingFor(Wait.forLogMessage(".*Couchbase started.*\\s", 1))
    .waitingFor(Wait.forLogMessage(".*Couchbase is ready for connections.*\\s", 1))
    .withCreateContainerCmdModifier(cmd ->
      Objects.requireNonNull(cmd
        .withName("camila-couchbase-testing-" + UUID.randomUUID())
        .getHostConfig())
      .withMemory(3L * 1024 * 1024 * 1024)
      .withMemorySwap(3L * 1024 * 1024 * 1024)
      .withCpuCount(1L));

  static {
    container.start();
    updateDataSourceProps();
    initBucket();
  }

  private static void createBucketContent(final String path, final String body) throws IOException {
    final var requestScope = new Request.Builder()
      .header("Authorization", Credentials.basic(USERNAME, PASSWORD))
      .url(path)
      .post(new FormBody.Builder().add("name", body).build())
      .build();
    try (var response = new OkHttpClient().newCall(requestScope).execute()) {
      if (response.body() != null) {
        log.debug(response.body().string());
      } else {
        log.warn("Response body is null for request: {}", path);
      }
    }
  }

  private static void loadBucketCollectionData(final Bucket bucket) throws IOException {
    final var productJsonFile = Paths.get(PRODUCTS_JSON).toFile();
    if (!productJsonFile.exists()) {
      throw new RuntimeException("File not found: %s".formatted(productJsonFile.getAbsolutePath()));
    }

    final List<Map<String, Object>> products = new ObjectMapper().readValue(
      Files.readAllBytes(productJsonFile.toPath()),
      new TypeReference<>() {
      }
    );
    final var collection = bucket.scope(BUCKET_SCOPE).collection(BUCKET_COLLECTION);

    products.forEach(productMap -> collection
      .upsert(UUID.randomUUID().toString(), JsonObject.from(productMap)));
  }

  private static void initBucket() {
    final var clusterOptions = ClusterOptions.clusterOptions(
      container.getUsername(),
      container.getPassword()).environment(
      ClusterEnvironment.builder().disableAppTelemetry(true).build()
    );

    try (var cluster = Cluster.connect(container.getConnectionString(), clusterOptions)) {
      cluster.waitUntilReady(Duration.ofSeconds(60L));

      final var bucket = cluster.bucket(BUCKET_NAME);
      createBucketContent(buildBasePath(bucket.name()), BUCKET_SCOPE);
      createBucketContent("%s/%s/collections".formatted(buildBasePath(bucket.name()), BUCKET_SCOPE), BUCKET_COLLECTION);
      bucket.waitUntilReady(Duration.ofSeconds(60L));
      loadBucketCollectionData(bucket);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String buildBasePath(final String bucketName) {
    return "%s://%s:%d/pools/default/buckets/%s/scopes".formatted(
      "http",
      container.getHost(),
      container.getBootstrapHttpDirectPort(),
      bucketName);
  }

  private static void updateDataSourceProps() {
    System.setProperty("spring.couchbase.connection-string", container.getConnectionString());
    System.setProperty("spring.couchbase.username", container.getUsername());
    System.setProperty("spring.couchbase.password", container.getPassword());
  }
}
