/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriamrpc.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcServiceVertxEBProxyHandler;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(RpcVerticle.class);

  private final IamRpcService iamRpcService;
  private final Config config;

  private MessageConsumer<JsonObject> consumer;

  @Inject
  RpcVerticle(IamRpcService iamRpcService, Config config) {
    this.iamRpcService = iamRpcService;
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));
    log.info("starting");

    vertx
        .eventBus()
        .addInboundInterceptor(
            ctx -> {
              log.info("inbound interceptor");
              ctx.next();
            })
        .addOutboundInterceptor(
            ctx -> {
              log.info("outbound interceptor");
              ctx.next();
            });

    this.consumer =
        new IamRpcServiceVertxEBProxyHandler(vertx, iamRpcService)
            .register(vertx.eventBus(), IamRpcService.ADDRESS)
            .fetch(10)
            .exceptionHandler(err -> log.error("exception in event bus", err))
            .endHandler(ignore -> log.info("end handler"));

    Router mainRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    Config.HttpConfig httpConfig = config.httpConfig();
    log.info("starting on port: {}", httpConfig.port());
    vertx
        .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
        .requestHandler(mainRouter)
        .listen()
        .onComplete(
            res -> {
              if (res.succeeded()) {
                log.info("started http server");
                startPromise.complete();
              } else {
                log.error("failed to start verticle", res.cause());
                startPromise.fail(res.cause());
              }
            });
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    consumer
        .unregister()
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                System.err.println("stopped");
                stopPromise.complete();
              } else {
                System.err.println("failed to stop: " + ar.cause());
                stopPromise.fail(ar.cause());
              }
            });
  }
}
