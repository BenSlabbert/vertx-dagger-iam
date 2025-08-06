/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.web.route.handler;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

@Singleton
class RequestValidator {

  private final Validator v;

  @Inject
  RequestValidator(ValidatorFactory validatorFactory) {
    this.v = validatorFactory.getValidator();
  }

  <T> Set<ConstraintViolation<T>> validate(T candidate) {
    return v.validate(candidate);
  }
}
