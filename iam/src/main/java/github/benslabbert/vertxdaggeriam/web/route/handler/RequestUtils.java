/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.web.route.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

import io.vertx.ext.web.RoutingContext;

class RequestUtils {

  private RequestUtils() {}

  static boolean isRequestFromBrowser(RoutingContext ctx) {
    String userAgent = ctx.request().getHeader(USER_AGENT);

    if (userAgent == null) {
      return false;
    }

    return userAgent.contains("Mozilla")
        || userAgent.contains("Chrome")
        || userAgent.contains("Safari")
        || userAgent.contains("Edg")
        || userAgent.contains("Firefox");
  }
}
