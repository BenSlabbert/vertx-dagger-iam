/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.config;

import dagger.Module;
import jakarta.validation.ValidatorFactory;

@Module(includes = {ModuleBindings.class, HibernateValidationConfig.class})
public interface ConfigModule {

  ValidatorFactory validatorFactory();
}
