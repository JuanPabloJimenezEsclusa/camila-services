package com.camila.api.product.infrastructure.adapter.output.cache.redis;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The type Redis container config.
 */
class RedisContainerConfig {

  private static final String REDIS_PASSWORD = "camila";

  private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:8.6.3-alpine");

  private static final RedisContainer container = new RedisContainer(REDIS_IMAGE)
    .withStartupTimeout(Duration.ofMinutes(2L)).withReuse(true)
    .withCommand("redis-server --save 20 1 --requirepass %s".formatted(REDIS_PASSWORD))
    .withCreateContainerCmdModifier(cmd -> Objects
      .requireNonNull(
        cmd.withName("camila-redis-testing-%s".formatted(UUID.randomUUID())).getHostConfig())
      .withMemory(2L * 1024 * 1024 * 1024).withMemorySwap(2L * 1024 * 1024 * 1024)
      .withMemorySwappiness(2L * 1024 * 1024 * 1024).withCpuCount(1L));

  private RedisContainerConfig() {
  }

  static void init() {
    container.start();
    updateDataSourceProps();
  }

  private static void updateDataSourceProps() {
    System.setProperty("spring.data.redis.host", container.getRedisHost());
    System.setProperty("spring.data.redis.port", "%d".formatted(container.getRedisPort()));
  }
}
