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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserHandler {

  private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

  private final RequestValidator requestValidator;
  private final IamAuthApi iamAuthApi;

  @Inject
  UserHandler(IamAuthApi iamAuthApi, RequestValidator requestValidator) {
    this.iamAuthApi = iamAuthApi;
    this.requestValidator = requestValidator;
  }

  public void configureRoutes(Router router) {
    router.post(UserHandler_Login_ParamParser.PATH).handler(this::login);
    router.post(UserHandler_Refresh_ParamParser.PATH).handler(this::refresh);
    router.post(UserHandler_Register_ParamParser.PATH).handler(this::register);
    router.post(UserHandler_UpdatePermissions_ParamParser.PATH).handler(this::updatePermissions);
  }

  @RestHandler(path = "/login")
  void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Set<String> missingFields = LoginRequestDto.missingRequiredFields(body);
    if (!missingFields.isEmpty()) {
      JsonArray errors = new JsonArray();
      for (String missingField : missingFields) {
        errors.add(
            new JsonObject().put("field", missingField).put("message", "required field missing"));
      }
      JsonObject msg = new JsonObject().put("errors", errors);
      ResponseWriter.write(ctx, msg, HttpResponseStatus.BAD_REQUEST);
      return;
    }

    LoginRequestDto req = LoginRequestDto.fromJson(body);
    Set<ConstraintViolation<LoginRequestDto>> validations = requestValidator.validate(req);
    if (!validations.isEmpty()) {
      log.error("invalid login request params");
      ResponseWriter.writeBadRequest(ctx, validations);
      return;
    }

    iamAuthApi
        .login(req)
        .onFailure(
            err -> {
              log.error("failed to login user", err);
              if (err instanceof HttpException e) {
                ResponseWriter.write(
                    ctx, new JsonObject(), HttpResponseStatus.valueOf(e.getStatusCode()));
                return;
              }
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), CREATED));
  }

  @RestHandler(path = "/refresh")
  void refresh(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Set<String> missingFields = RefreshRequestDto.missingRequiredFields(body);
    if (!missingFields.isEmpty()) {
      // send errors
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    RefreshRequestDto req = RefreshRequestDto.fromJson(body);
    Set<ConstraintViolation<RefreshRequestDto>> validations = requestValidator.validate(req);
    if (!validations.isEmpty()) {
      log.error("invalid refresh request");
      ResponseWriter.writeBadRequest(ctx, validations);
      return;
    }

    iamAuthApi
        .refresh(req)
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
    Set<String> missingFields = RegisterRequestDto.missingRequiredFields(body);
    if (!missingFields.isEmpty()) {
      JsonArray errors = new JsonArray();
      for (String missingField : missingFields) {
        errors.add(
            new JsonObject().put("field", missingField).put("message", "required field missing"));
      }
      JsonObject msg = new JsonObject().put("errors", errors);
      ResponseWriter.write(ctx, msg, HttpResponseStatus.BAD_REQUEST);
      return;
    }

    RegisterRequestDto req = RegisterRequestDto.fromJson(body);
    Set<ConstraintViolation<RegisterRequestDto>> validations = requestValidator.validate(req);
    if (!validations.isEmpty()) {
      log.error("invalid register request");
      ResponseWriter.writeBadRequest(ctx, validations);
      return;
    }

    iamAuthApi
        .register(req)
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
    Set<String> missingFields = UpdatePermissionsRequestDto.missingRequiredFields(body);
    if (!missingFields.isEmpty()) {
      // send errors
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    UpdatePermissionsRequestDto req = UpdatePermissionsRequestDto.fromJson(body);
    Set<ConstraintViolation<UpdatePermissionsRequestDto>> validations =
        requestValidator.validate(req);
    if (!validations.isEmpty()) {
      log.error("invalid update permissions");
      ResponseWriter.writeBadRequest(ctx, validations);
      return;
    }

    iamAuthApi
        .updatePermissions(req)
        .onFailure(
            err -> {
              log.error("failed to update user permissions", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto.toJson(), NO_CONTENT));
  }
}
