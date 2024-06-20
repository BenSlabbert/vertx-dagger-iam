/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.config;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import jakarta.validation.ValidatorFactory;

@Module
interface ModuleBindings {

  @Binds
  @IntoSet
  AutoCloseable hibernateValidationConfig(ValidatorFactory validatorFactory);
}
