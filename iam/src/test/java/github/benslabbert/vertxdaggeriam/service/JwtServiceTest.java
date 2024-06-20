/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.service;

import static org.assertj.core.api.Assertions.assertThat;

import github.benslabbert.vertxdaggeriam.UnitTestBase;
import github.benslabbert.vertxdaggeriam.entity.ACL;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.junit5.VertxTestContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JwtServiceTest extends UnitTestBase {

  private JWTAuth jwtAuth;
  private JWTAuth jwtRefresh;

  @BeforeEach
  void before(Vertx vertx) {
    jwtAuth =
        JWTAuth.create(
            vertx,
            new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("authKey1")
                        .setAlgorithm("HS256")
                        .setBuffer("123supersecretkey789"))
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("authKey2")
                        .setAlgorithm("HS256")
                        .setBuffer("321supersecretkey987")));

    jwtRefresh =
        JWTAuth.create(
            vertx,
            new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("refreshKey1")
                        .setAlgorithm("HS256")
                        .setBuffer("123anothersupersecretkey789")));
  }

  @Test
  void test() throws Exception {
    record Test(
        @NotBlank @Size(min = 3, max = 5) String a,
        @NotBlank String b,
        @Valid @NotNull Inner other) {
      record Inner(@NotBlank String c, @NotBlank String d) {}
    }

    ValidatorFactory validatorFactory = provider.validatorFactory();
    Validator validator = validatorFactory.getValidator();

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    var futures =
        IntStream.range(0, 100)
            .mapToObj(
                ignore ->
                    executorService.submit(
                        () -> {
                          Set<ConstraintViolation<Test>> validate =
                              validator.validate(new Test("a", "b", new Test.Inner("", "")));
                          JsonObject convert = convert(validate);
                          assertThat(convert).isNotNull();
                        }))
            .toList();

    for (java.util.concurrent.Future<?> future : futures) {
      future.get();
    }

    Set<ConstraintViolation<Test>> validate =
        validator.validate(new Test("a", "b", new Test.Inner("", "")));
    JsonObject convert = convert(validate);
    assertThat(convert).isNotNull();

    executorService.close();
  }

  <T> JsonObject convert(Set<ConstraintViolation<T>> violations) {
    return new JsonObject()
        .put(
            "errors",
            violations.stream()
                .map(
                    violation ->
                        new JsonObject()
                            .put("field", violation.getPropertyPath().toString())
                            .put("message", violation.getMessage()))
                .toList());
  }

  @Test
  void authToken(VertxTestContext testContext) {
    String token =
        provider
            .tokenService()
            .authToken(
                "name",
                ACL.builder()
                    .group("admin")
                    .role("role")
                    .permissions(Set.of("p-1", "p-2"))
                    .build());

    assertThat(token).isNotNull();

    jwtAuth.authenticate(
        new TokenCredentials(token),
        testContext.succeeding(
            user ->
                testContext.verify(
                    () -> {
                      assertThat(user).isNotNull();

                      JsonObject p = user.principal();
                      assertThat(p).isNotNull();
                      assertThat(p.stream().count()).isEqualTo(6L);

                      JsonObject acl = p.getJsonObject("acl");
                      assertThat(acl.getString("group")).isEqualTo("admin");
                      assertThat(acl.getString("role")).isEqualTo("role");
                      assertThat(acl.getJsonArray("permissions"))
                          .containsExactlyInAnyOrder("p-1", "p-2");

                      assertThat(token).isEqualTo(p.getString("access_token"));
                      assertThat(p.getString("aud")).isEqualTo("vertx-dagger-app");
                      assertThat(p.getString("iss")).isEqualTo("iam");
                      assertThat(p.getString("sub")).isEqualTo("name");
                      assertThat(p.getString("additional-props")).isEqualTo("new-prop");

                      JsonObject a = user.attributes();
                      assertThat(a).isNotNull();
                      assertThat(a.stream().count()).isEqualTo(5L);

                      JsonObject accessToken = a.getJsonObject("accessToken");
                      assertThat(accessToken.getInteger("iat")).isNotNull();
                      assertThat(accessToken.getInteger("exp")).isNotNull();
                      assertThat(accessToken.getString("aud")).isEqualTo("vertx-dagger-app");
                      assertThat(accessToken.getString("iss")).isEqualTo("iam");
                      assertThat(accessToken.getString("sub")).isEqualTo("name");

                      assertThat(a.getInteger("exp")).isNotNull();
                      assertThat(a.getInteger("iat")).isNotNull();
                      assertThat(a.getString("sub")).isEqualTo("name");
                      assertThat(a.getString("rootClaim")).isEqualTo("accessToken");

                      testContext.completeNow();
                    })));
  }

  static Stream<Arguments> isValidRefreshSource() {
    return Stream.of(
        Arguments.of("", false), Arguments.of("invalid", false), Arguments.of(null, true));
  }

  @ParameterizedTest
  @MethodSource("isValidRefreshSource")
  void isValidRefresh(String token, boolean valid, VertxTestContext testContext) {
    var tokenService = provider.tokenService();

    if (null == token) {
      token = tokenService.refreshToken("name");
    }

    Future<User> validRefresh = tokenService.isValidRefresh(token);
    if (valid) {
      validRefresh.onComplete(
          testContext.succeeding(
              user -> {
                testContext.verify(
                    () -> {
                      assertThat(user).isNotNull();
                      testContext.completeNow();
                    });
              }));
    } else {
      validRefresh.onComplete(
          testContext.failing(
              err -> {
                testContext.verify(
                    () -> {
                      assertThat(err).isNotNull();
                      testContext.completeNow();
                    });
              }));
    }
  }

  @Test
  void refreshToken(VertxTestContext testContext) {
    var tokenService = provider.tokenService();
    var username = "name";
    var token = tokenService.refreshToken(username);
    assertThat(token).isNotNull();

    jwtRefresh.authenticate(
        new TokenCredentials(token),
        testContext.succeeding(
            user ->
                testContext.verify(
                    () -> {
                      assertThat(user).isNotNull();

                      JsonObject p = user.principal();
                      assertThat(p).isNotNull();
                      assertThat(p.stream().count()).isEqualTo(5L);

                      assertThat(token).isEqualTo(p.getString("access_token"));
                      assertThat(p.getString("additional-props")).isEqualTo("new-prop");
                      assertThat(p.getString("aud")).isEqualTo("vertx-dagger-app");
                      assertThat(p.getString("iss")).isEqualTo("iam");
                      assertThat(p.getString("sub")).isEqualTo(username);

                      JsonObject a = user.attributes();
                      assertThat(a).isNotNull();
                      assertThat(a.stream().count()).isEqualTo(5L);

                      JsonObject accessToken = a.getJsonObject("accessToken");
                      assertThat(accessToken.getString("additional-props")).isEqualTo("new-prop");
                      assertThat(accessToken.getInteger("iat")).isNotNull();
                      assertThat(accessToken.getInteger("exp")).isNotNull();
                      assertThat(accessToken.getString("aud")).isEqualTo("vertx-dagger-app");
                      assertThat(accessToken.getString("iss")).isEqualTo("iam");
                      assertThat(accessToken.getString("sub")).isEqualTo(username);

                      assertThat(a.getInteger("exp")).isNotNull();
                      assertThat(a.getInteger("iat")).isNotNull();
                      assertThat(a.getString("sub")).isEqualTo(username);
                      assertThat(a.getString("rootClaim")).isEqualTo("accessToken");

                      testContext.completeNow();
                    })));
  }
}
