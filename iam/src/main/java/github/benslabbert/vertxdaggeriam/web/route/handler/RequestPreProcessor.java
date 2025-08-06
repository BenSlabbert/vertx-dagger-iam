/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.web.route.handler;

import github.benslabbert.vertxdaggercommons.web.ResponseWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
class RequestPreProcessor {

  private final RequestValidator requestValidator;

  @Inject
  RequestPreProcessor(RequestValidator requestValidator) {
    this.requestValidator = requestValidator;
  }

  <T> void process(
      RoutingContext ctx, Function<JsonObject, T> fromJsonFunction, Consumer<T> nextFunction) {

    JsonObject body = ctx.body().asJsonObject();

    T req = fromJsonFunction.apply(body);
    Set<ConstraintViolation<T>> validations = requestValidator.validate(req);
    if (!validations.isEmpty()) {
      ResponseWriter.writeBadRequest(ctx, validations);
      return;
    }

    nextFunction.accept(req);
  }
}
