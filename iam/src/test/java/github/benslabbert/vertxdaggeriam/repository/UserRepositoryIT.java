/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.repository;

import static org.assertj.core.api.Assertions.assertThat;

import github.benslabbert.vertxdaggeriam.IntegrationTestBase;
import github.benslabbert.vertxdaggeriam.entity.ACL;
import github.benslabbert.vertxdaggeriam.entity.User;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserRepositoryIT extends IntegrationTestBase {

  @Test
  void updatePermissions(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    UserRepository userRepository = provider.userRepository();

    String name = UUID.randomUUID().toString();

    userRepository
        .register(name, "password", "token", "refreshToken", "group", "role", Set.of("p-1", "p-2"))
        .onComplete(testContext.succeeding(ignore -> testContext.verify(checkpoint::flag)))
        .compose(
            ignore ->
                userRepository.updatePermissions(
                    name, ACL.builder().group("g").role("r").permissions(Set.of("p")).build()))
        .compose(ignore -> userRepository.findByUsername(name))
        .onComplete(
            testContext.succeeding(
                user ->
                    testContext.verify(
                        () -> {
                          assertThat(user.acl())
                              .usingRecursiveComparison()
                              .isEqualTo(
                                  ACL.builder()
                                      .group("g")
                                      .role("r")
                                      .permissions(Set.of("p"))
                                      .build());
                          checkpoint.flag();
                        })));
  }

  @Test
  void register(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    UserRepository userRepository = provider.userRepository();

    String name = UUID.randomUUID().toString();
    userRepository
        .register(name, "password", "token", "refreshToken", "group", "role", Set.of("p-1", "p-2"))
        .onComplete(testContext.succeeding(ignore -> testContext.verify(checkpoint::flag)))
        .compose(ignore -> userRepository.findByUsername(name))
        .onComplete(
            ar ->
                testContext.verify(
                    () -> {
                      assertThat(ar.succeeded()).isTrue();
                      assertThat(ar.result())
                          .satisfies(
                              user -> {
                                User expected =
                                    new User(
                                        name,
                                        "password",
                                        "refreshToken",
                                        new ACL("group", "role", Set.of("p-1", "p-2")));

                                assertThat(user).usingRecursiveComparison().isEqualTo(expected);
                              });
                      checkpoint.flag();
                    }));
  }
}
