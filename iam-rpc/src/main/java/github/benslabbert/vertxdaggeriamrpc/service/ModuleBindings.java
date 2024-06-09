/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriamrpc.service;

import dagger.Binds;
import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;

@Module
interface ModuleBindings {

  @Binds
  TokenService bindsTokenService(TokenServiceImpl tokenService);

  @Binds
  IamRpcService bindsIamRpcService(IamRpcServiceImpl iamRpcService);
}
