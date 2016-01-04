package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

public interface AuthorizeService {
  static AuthorizeService getInstance() {
    return new AuthorizeServiceImpl();
  }

  JsonObject authorize(String clientId, String clientKey, String grantType, String returnUrl);
}
