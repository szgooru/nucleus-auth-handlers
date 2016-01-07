package org.gooru.auth.handlers.processors.service.authorize;

import io.vertx.core.json.JsonObject;

public interface AuthorizeService {
  static AuthorizeService instance() {
    return new AuthorizeServiceImpl();
  }

  JsonObject authorize(String clientId, String clientKey, String grantType, String returnUrl);
}
