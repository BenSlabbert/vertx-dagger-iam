/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.service;

import dagger.Binds;
import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.iam.auth.IamAuthApi;

@Module
interface ServiceModuleBindings {

  @Binds
  IamAuthApi createUserService(IamAuthApiImpl iamAuthApi);

  @Binds
  TokenService createTokenService(JwtService jwtService);
}
