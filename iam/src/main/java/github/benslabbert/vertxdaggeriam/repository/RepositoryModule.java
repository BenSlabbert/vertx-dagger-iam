/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.repository;

import dagger.Module;

@Module(includes = RepositoryModuleBindings.class)
public interface RepositoryModule {

  UserRepository userRepository();
}
