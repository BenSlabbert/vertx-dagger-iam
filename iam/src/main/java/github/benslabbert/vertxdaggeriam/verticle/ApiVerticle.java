/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
import github.benslabbert.vertxdaggercommons.future.MultiCompletePromise;
import github.benslabbert.vertxdaggeriam.web.route.handler.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.redis.client.RedisAPI;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);

  private final ClosingService closingService;
  private final UserHandler userHandler;
  private final RedisAPI redisAPI;
  private final Config config;

  private HttpServer server;

  @Inject
  ApiVerticle(
      ClosingService closingService, UserHandler userHandler, RedisAPI redisAPI, Config config) {
    this.closingService = closingService;
    this.userHandler = userHandler;
    this.redisAPI = redisAPI;
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream("svelte/index.html")) {
      if (inputStream == null) {
        log.warn("inputStream is null");
      } else {
        log.info("loading index length: {}", inputStream.readAllBytes().length);
      }
    } catch (IOException e) {
      log.error("unhandled exception while loading index", e);
    }

    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    userHandler.configureRoutes(apiRouter);

    StaticHandler staticHandler = StaticHandler.create("svelte");

    mainRouter
        .get()
        .handler(
            ctx -> {
              String path = ctx.request().path();

              if (path.startsWith("/api")
                  || path.startsWith("/health")
                  || path.startsWith("/ping")) {
                ctx.next();
                return;
              }
              if (path.equals("/") || path.endsWith(".js") || path.endsWith(".css")) {
                staticHandler.handle(ctx);
                return;
              }

              vertx
                  .fileSystem()
                  .readFile("svelte/index.html")
                  .onComplete(
                      ar -> {
                        if (ar.failed()) {
                          ctx.response().setStatusCode(NOT_FOUND.code()).end();
                          return;
                        }

                        ctx.response().setStatusCode(OK.code()).end(ar.result());
                      });
            });

    log.info("ping redis before starting http server");
    redisAPI
        .ping(List.of())
        .onFailure(startPromise::fail)
        .map(
            ignore -> {
              Config.HttpConfig httpConfig = config.httpConfig();
              log.info("starting api verticle on port: {}", httpConfig.port());
              return vertx
                  .createHttpServer(
                      new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
                  .requestHandler(mainRouter)
                  .listen()
                  .onComplete(
                      res -> {
                        if (res.succeeded()) {
                          this.server = res.result();
                          log.info("started http server");
                          startPromise.complete();
                        } else {
                          log.error("failed to start verticle", res.cause());
                          startPromise.fail(res.cause());
                        }
                      });
            });

    log.info("Configured routes");
    log.info("-------------------------");
    mainRouter
        .getRoutes()
        .forEach(
            route -> {
              log.info("Path: {}", route.getPath());
              log.info("Methods: {}", route.methods());
              log.info("-------------------------");
            });
  }

  public int getPort() {
    return server.actualPort();
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");

    Set<AutoCloseable> closeables = closingService.closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

    int idx = 0;
    for (AutoCloseable service : closeables) {
      try {
        System.err.printf("closing: [%d/%d]%n", idx++, closeables.size());
        service.close();
      } catch (Exception e) {
        System.err.println("unable to close resources: " + e);
      }
    }

    var multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);
    if (null != server) {
      server.close().onComplete(multiCompletePromise::complete);
    } else {
      multiCompletePromise.complete();
    }

    System.err.println("awaitTermination...start");
    FutureUtil.awaitTermination()
        .onComplete(
            ar -> {
              System.err.printf("awaitTermination...end: %b%n", ar.result());
              multiCompletePromise.complete();
            });
  }
}
