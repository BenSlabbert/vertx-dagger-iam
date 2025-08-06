/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.repository;

import github.benslabbert.vertxdaggercommons.redis.RedisConstants;
import github.benslabbert.vertxdaggeriam.entity.ACL;
import github.benslabbert.vertxdaggeriam.entity.User;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class RedisDB implements UserRepository, AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(RedisDB.class);

  private final RedisAPI redisAPI;

  @Inject
  RedisDB(RedisAPI redisAPI) {
    this.redisAPI = redisAPI;
    this.redisAPI
        .ping(List.of(""))
        .onFailure(err -> log.error("failed to ping redis", err))
        .onSuccess(ignore -> log.info("pinged redis"));
  }

  @Override
  public void close() {
    redisAPI.close();
  }

  @Override
  public Future<User> findByUsername(String username) {
    return redisAPI
        .jsonGet(List.of(prefixId(username), RedisConstants.DOCUMENT_ROOT))
        .map(
            resp -> {
              if (null == resp) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              if (ResponseType.BULK != resp.type()) {
                throw VertxException.noStackTrace("expected BULK response type");
              }

              JsonArray array = new JsonArray(resp.toBuffer());

              if (array.isEmpty()) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              if (1 != array.size()) {
                throw VertxException.noStackTrace("expected only one element in array");
              }

              JsonObject o = (JsonObject) array.iterator().next();
              return User.fromJson(o);
            });
  }

  @Override
  public Future<Void> login(String username, String password, String token, String refreshToken) {
    return redisAPI
        .jsonGet(List.of(prefixId(username), RedisConstants.DOCUMENT_ROOT_PREFIX + "password"))
        .compose(
            resp -> {
              if (null == resp) {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code());
              }

              String passwordFromDB = resp.toString();

              if (null == passwordFromDB) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              passwordFromDB = passwordFromDB.substring(2, passwordFromDB.length() - 2);

              if (!password.equals(passwordFromDB)) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              return Future.<Void>succeededFuture();
            })
        .compose(ignore -> updateRefreshToken(username, refreshToken))
        .onSuccess(ignore -> log.info("user logged in"));
  }

  @Override
  public Future<Void> refresh(
      String username, String oldRefreshToken, String newToken, String newRefreshToken) {

    return redisAPI
        .jsonGet(List.of(prefixId(username), RedisConstants.DOCUMENT_ROOT_PREFIX + "refreshToken"))
        .compose(
            resp -> {
              if (null == resp) {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code());
              }

              String refreshTokenFromDb = resp.toString();
              refreshTokenFromDb = refreshTokenFromDb.substring(2, refreshTokenFromDb.length() - 2);

              if (!oldRefreshToken.equals(refreshTokenFromDb)) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              return Future.<Void>succeededFuture();
            })
        .compose(ignore -> updateRefreshToken(username, newRefreshToken))
        .onSuccess(ignore -> log.info("user refreshed"));
  }

  @Override
  public Future<Void> register(
      String username,
      String password,
      String token,
      String refreshToken,
      String group,
      String role,
      Set<String> permissions) {
    if (permissions.isEmpty()) {
      throw VertxException.noStackTrace("permissions cannot be empty");
    }

    return redisAPI
        .jsonSet(
            List.of(
                prefixId(username),
                RedisConstants.DOCUMENT_ROOT,
                new User(
                        username,
                        password,
                        refreshToken,
                        ACL.builder().group(group).role(role).permissions(permissions).build())
                    .toJson()
                    .encode(),
                RedisConstants.SET_IF_DOES_NOT_EXIST))
        .compose(
            resp -> {
              if (null == resp) {
                // value could not be set
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return Future.<Void>succeededFuture();
            })
        .onSuccess(resp -> log.info("user registered"));
  }

  @Override
  public Future<Void> updatePermissions(String username, ACL acl) {
    return redisAPI
        .jsonSet(
            List.of(
                prefixId(username),
                RedisConstants.DOCUMENT_ROOT_PREFIX + "acl",
                acl.toJson().encode(),
                RedisConstants.SET_IF_EXIST))
        .map(
            resp -> {
              if (null == resp) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              if (ResponseType.SIMPLE != resp.type()) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              if (RedisConstants.OK.equals(resp.toString())) {
                return null;
              }

              throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            });
  }

  private Future<Void> updateRefreshToken(String username, String newRefreshToken) {
    return redisAPI
        .jsonSet(
            List.of(
                prefixId(username),
                RedisConstants.DOCUMENT_ROOT_PREFIX + "refreshToken",
                // must quote values back to redis
                "\"" + newRefreshToken + "\"",
                RedisConstants.SET_IF_EXIST))
        .map(ignore -> null);
  }

  private static String prefixId(String username) {
    return "user:" + username;
  }
}
