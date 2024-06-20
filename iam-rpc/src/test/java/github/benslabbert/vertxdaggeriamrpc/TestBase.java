/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriamrpc;

import github.benslabbert.vertxdaggercommons.ConfigEncoder;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.config.Config.HttpConfig;
import github.benslabbert.vertxdaggeriamrpc.ioc.DaggerTestProvider;
import github.benslabbert.vertxdaggeriamrpc.ioc.TestProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public abstract class TestBase {

  protected TestProvider provider;

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    Config config = Config.builder().httpConfig(HttpConfig.builder().port(0).build()).build();

    provider = DaggerTestProvider.builder().vertx(vertx).config(config).build();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.rpcVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
