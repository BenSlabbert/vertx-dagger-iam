/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

import github.benslabbert.vertxdaggerapp.api.iam.auth.IamAuthApi;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.LoginRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RefreshRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RegisterRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.UpdatePermissionsRequestDto;
import github.benslabbert.vertxdaggercodegen.annotation.url.RestHandler;
import github.benslabbert.vertxdaggercommons.web.ResponseWriter;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserHandler {

  private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

  private final RequestPreProcessor preProcessor;
  private final IamAuthApi iamAuthApi;

  @Inject
  UserHandler(IamAuthApi iamAuthApi, RequestPreProcessor preProcessor) {
    this.iamAuthApi = iamAuthApi;
    this.preProcessor = preProcessor;
  }

  public void configureRoutes(Router router) {
    router.post(UserHandler_Login_ParamParser.PATH).handler(this::login);
    router.post(UserHandler_Refresh_ParamParser.PATH).handler(this::refresh);
    router.post(UserHandler_Register_ParamParser.PATH).handler(this::register);
    router.post(UserHandler_UpdatePermissions_ParamParser.PATH).handler(this::updatePermissions);
  }

  @RestHandler(path = "/login")
  private void login(RoutingContext ctx) {
    preProcessor.process(
        ctx,
        LoginRequestDto::missingRequiredFields,
        LoginRequestDto::fromJson,
        req ->
            iamAuthApi
                .login(req)
                .onFailure(
                    err -> {
                      log.error("failed to login user", err);
                      ResponseWriter.writeError(ctx, err);
                    })
                .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), CREATED)));
  }

  @RestHandler(path = "/refresh")
  private void refresh(RoutingContext ctx) {
    preProcessor.process(
        ctx,
        RefreshRequestDto::missingRequiredFields,
        RefreshRequestDto::fromJson,
        req ->
            iamAuthApi
                .refresh(req)
                .onFailure(
                    err -> {
                      log.error("failed to refresh user", err);
                      ResponseWriter.writeError(ctx, err);
                    })
                .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), CREATED)));
  }

  @RestHandler(path = "/register")
  private void register(RoutingContext ctx) {
    preProcessor.process(
        ctx,
        RegisterRequestDto::missingRequiredFields,
        RegisterRequestDto::fromJson,
        req ->
            iamAuthApi
                .register(req)
                .onFailure(
                    err -> {
                      log.error("failed to register user", err);
                      ResponseWriter.writeError(ctx, err);
                    })
                .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), NO_CONTENT)));
  }

  @RestHandler(path = "/update-permissions")
  private void updatePermissions(RoutingContext ctx) {
    preProcessor.process(
        ctx,
        UpdatePermissionsRequestDto::missingRequiredFields,
        UpdatePermissionsRequestDto::fromJson,
        req ->
            iamAuthApi
                .updatePermissions(req)
                .onFailure(
                    err -> {
                      log.error("failed to update user permissions", err);
                      ResponseWriter.writeError(ctx, err);
                    })
                .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), NO_CONTENT)));
  }
}
