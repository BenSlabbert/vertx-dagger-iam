/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam;

import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.config.Config.HttpConfig;
import github.benslabbert.vertxdaggercommons.config.Config.RedisConfig;
import github.benslabbert.vertxdaggercommons.test.ConfigEncoder;
import github.benslabbert.vertxdaggercommons.test.DockerContainers;
import github.benslabbert.vertxdaggeriam.ioc.DaggerProvider;
import github.benslabbert.vertxdaggeriam.ioc.Provider;
import github.benslabbert.vertxdaggeriam.verticle.ApiVerticle;
import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(VertxExtension.class)
public abstract class IntegrationTestBase {

  protected Provider provider;
  protected ApiVerticle verticle;

  protected static final GenericContainer<?> redis = DockerContainers.REDIS;

  static {
    redis.start();
  }

  @BeforeAll
  static void beforeAll() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    Config config =
        Config.builder()
            .httpConfig(HttpConfig.builder().port(0).build())
            .redisConfig(
                RedisConfig.builder()
                    .host("127.0.0.1")
                    .port(redis.getMappedPort(6379))
                    .database(0)
                    .build())
            .build();

    this.provider =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .build();

    JsonObject cfg = ConfigEncoder.encode(config);
    this.verticle = provider.apiVerticle();

    vertx
        .deployVerticle(verticle, new DeploymentOptions().setConfig(cfg))
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                RestAssured.baseURI = "http://127.0.0.1";
                RestAssured.port = this.verticle.getPort();
                testContext.completeNow();
              } else {
                testContext.failNow(ar.cause());
              }
            });
  }

  @AfterEach
  void after() {
    RestAssured.reset();
  }
}
