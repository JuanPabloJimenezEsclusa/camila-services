package com.camila.api.product.infrastructure.adapter.output.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.okhttp3.Credentials;
import org.testcontainers.shaded.okhttp3.FormBody;
import org.testcontainers.shaded.okhttp3.OkHttpClient;
import org.testcontainers.shaded.okhttp3.Request;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
@Testcontainers
public class ProductCouchbaseContainerConfig implements BeforeAllCallback, AfterAllCallback {
  private static final String BUCKET_NAME = "camila-product-bucket";
  private static final String BUCKET_SCOPE = "product";
  private static final String BUCKET_COLLECTION = "products";
  private static final String USERNAME = "Administrator";
  private static final String PASSWORD = "password";
  private static final String PRODUCTS_JSON = "src/test/resources/scripts/couchbase/products.json";

  private static final BucketDefinition BUCKET_DEFINITION = new BucketDefinition(BUCKET_NAME);
  private static final DockerImageName COUCHBASE_IMAGE = DockerImageName
    .parse("couchbase")
    .asCompatibleSubstituteFor("couchbase/server")
    .withTag("7.6.5");

  @Container
  private static final CouchbaseContainer couchbaseContainer = new CouchbaseContainer(COUCHBASE_IMAGE)
    .withCredentials(USERNAME, PASSWORD)
    .withBucket(BUCKET_DEFINITION)
    .withStartupTimeout(Duration.ofSeconds(180))
    .waitingFor(Wait.forHealthcheck())
    .withCreateContainerCmdModifier(cmd ->
      Objects.requireNonNull(cmd
          .withName("camila-couchbase-testing")
          .getHostConfig())
        .withMemory(2L * 1024 * 1024 * 1024)
        .withMemorySwap(2L * 1024 * 1024 * 1024)
        .withCpuCount(1L));

  private static void initBucket() throws IOException {
    try (var cluster = Cluster.connect(couchbaseContainer.getConnectionString(),
      couchbaseContainer.getUsername(), couchbaseContainer.getPassword())) {
      final var bucket = cluster.bucket(BUCKET_NAME);

      bucket.waitUntilReady(Duration.ofSeconds(30));
      createBucketContent(buildBasePath(bucket.name()), BUCKET_SCOPE);
      createBucketContent("%s/%s/collections".formatted(buildBasePath(bucket.name()), BUCKET_SCOPE), BUCKET_COLLECTION);
      loadBucketCollectionData(bucket);
    }
  }

  // https://docs.couchbase.com/server/current/rest-api/scopes-and-collections-api.html#apis-in-this-section
  private static String buildBasePath(final String bucketName) {
    return "http://%s:%d/pools/default/buckets/%s/scopes".formatted(couchbaseContainer.getHost(),
      couchbaseContainer.getBootstrapHttpDirectPort(), bucketName);
  }

  private static void createBucketContent(final String path, final String body) throws IOException {
    final var requestScope = new Request.Builder()
      .header("Authorization", Credentials.basic(USERNAME, PASSWORD))
      .url(path)
      .post((new FormBody.Builder()).add("name", body).build())
      .build();
    try (var response = new OkHttpClient().newCall(requestScope).execute()) {
      log.debug(response.body().string());
    }
  }

  private static void loadBucketCollectionData(final Bucket bucket) throws IOException {
    final List<Map<String, Object>> products = new ObjectMapper().readValue(
      Files.readAllBytes(Paths.get(PRODUCTS_JSON)),
      new TypeReference<List<Map<String, Object>>>() {}
    );
    final var collection = bucket.scope(BUCKET_SCOPE).collection(BUCKET_COLLECTION);
    products.forEach(productMap -> collection.upsert(
      UUID.randomUUID().toString(),
      JsonObject.from(productMap)));
  }

  private static void updateDataSourceProps() {
    //Overwrite couchbase parameters
    System.setProperty("spring.couchbase.connection-string", couchbaseContainer.getConnectionString());
    System.setProperty("spring.couchbase.username", couchbaseContainer.getUsername());
    System.setProperty("spring.couchbase.password", couchbaseContainer.getPassword());
  }

  @Override
  public void afterAll(final ExtensionContext context) {
    couchbaseContainer.stop();
  }

  @Override
  public void beforeAll(final ExtensionContext context) {
    couchbaseContainer.start();
    await().until(couchbaseContainer::isRunning);

    try {
      updateDataSourceProps();
      initBucket();
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize Couchbase bucket", e);
    }
  }
}
