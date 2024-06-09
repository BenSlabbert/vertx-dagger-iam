/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

import github.benslabbert.vertxdaggerapp.api.iam.auth.IamAuthApi;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.LoginRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RefreshRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RegisterRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.UpdatePermissionsRequestDto;
import github.benslabbert.vertxdaggercodegen.annotation.url.RestHandler;
import github.benslabbert.vertxdaggercommons.web.ResponseWriter;
import github.benslabbert.vertxdaggeriam.web.SchemaValidatorDelegator;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserHandler {

  private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

  private final IamAuthApi iamAuthApi;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Inject
  UserHandler(IamAuthApi iamAuthApi, SchemaValidatorDelegator schemaValidatorDelegator) {
    this.iamAuthApi = iamAuthApi;
    this.schemaValidatorDelegator = schemaValidatorDelegator;
  }

  public void configureRoutes(Router router) {
    router.post(UserHandler_Login_ParamParser.PATH).handler(this::login);
    router.post(UserHandler_Refresh_ParamParser.PATH).handler(this::refresh);
    router.post(UserHandler_Register_ParamParser.PATH).handler(this::register);
    router.post(UserHandler_UpdatePermissions_ParamParser.PATH).handler(this::updatePermissions);

    log.info("Configured routes for UserHandler");
    log.info("-------------------------");
    router
        .getRoutes()
        .forEach(
            route -> {
              log.info("Path: {}", route.getPath());
              log.info("Methods: {}", route.methods());
              log.info("-------------------------");
            });
  }

  @RestHandler(path = "/login")
  void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(LoginRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid login request params");
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    iamAuthApi
        .login(LoginRequestDto.fromJson(body))
        .onFailure(
            err -> {
              log.error("failed to login user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), CREATED));
  }

  @RestHandler(path = "/refresh")
  void refresh(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(RefreshRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid refresh request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .refresh(RefreshRequestDto.fromJson(body))
        .onFailure(
            err -> {
              log.error("failed to refresh user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), CREATED));
  }

  @RestHandler(path = "/register")
  void register(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(RegisterRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid register request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .register(RegisterRequestDto.fromJson(body))
        .onFailure(
            err -> {
              log.error("failed to register user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), NO_CONTENT));
  }

  @RestHandler(path = "/update-permissions")
  void updatePermissions(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(UpdatePermissionsRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid register request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .updatePermissions(UpdatePermissionsRequestDto.fromJson(body))
        .onFailure(
            err -> {
              log.error("failed to update user permissions", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), NO_CONTENT));
  }
}
