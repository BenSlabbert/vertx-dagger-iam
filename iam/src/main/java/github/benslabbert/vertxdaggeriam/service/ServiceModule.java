/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.service;

import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.iam.auth.IamAuthApi;

@Module(includes = ModuleBindings.class)
public interface ServiceModule {

  IamAuthApi iamAuthApi();

  TokenService tokenService();
}
