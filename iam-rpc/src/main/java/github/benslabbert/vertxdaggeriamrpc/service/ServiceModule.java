/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriamrpc.service;

import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;

@Module(includes = ModuleBindings.class)
public interface ServiceModule {

  IamRpcService iamRpcService();
}
