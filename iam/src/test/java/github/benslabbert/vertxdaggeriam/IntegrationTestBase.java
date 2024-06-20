/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam;

import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.docker.DockerContainers;
import github.benslabbert.vertxdaggeriam.ioc.DaggerProvider;
import github.benslabbert.vertxdaggeriam.ioc.Provider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(VertxExtension.class)
public abstract class IntegrationTestBase {

  protected Provider provider;

  protected static final GenericContainer<?> redis = DockerContainers.REDIS;

  static {
    redis.start();
  }

  @BeforeEach
  void prepare(Vertx vertx) {
    Config config =
        Config.builder()
            .httpConfig(Config.HttpConfig.builder().port(0).build())
            .redisConfig(
                Config.RedisConfig.builder()
                    .host("127.0.0.1")
                    .port(redis.getMappedPort(6379))
                    .database(0)
                    .build())
            .build();

    provider =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .build();
  }
}
