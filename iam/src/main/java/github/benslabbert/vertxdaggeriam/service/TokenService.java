/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.service;

import github.benslabbert.vertxdaggeriam.entity.ACL;
import io.vertx.core.Future;
import io.vertx.ext.auth.User;

public interface TokenService {

  Future<User> isValidRefresh(String token);

  Future<User> authenticate(String token);

  String authToken(String username, ACL acl);

  String refreshToken(String username);
}
