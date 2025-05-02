/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriamrpc.verticle;

import static org.assertj.core.api.Assertions.assertThat;

import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.dto.CheckTokenRequestDto;
import github.benslabbert.vertxdaggeriamrpc.TestBase;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

class IamRpcServiceTest extends TestBase {

  @Test
  void checkSession(VertxTestContext testContext) {
    IamRpcService rpcService = provider.iamRpcService();

    rpcService
        .check(CheckTokenRequestDto.builder().token("blah").build())
        .onComplete(
            testContext.failing(
                err ->
                    testContext.verify(
                        () -> {
                          assertThat(err).isInstanceOf(IllegalArgumentException.class);
                          assertThat(err).hasMessageContaining("Invalid format for JWT");
                          testContext.completeNow();
                        })));
  }
}
