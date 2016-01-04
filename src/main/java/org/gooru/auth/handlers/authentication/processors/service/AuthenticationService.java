package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

public interface AuthenticationService {
  static AuthenticationService getInstance() {
    return new AuthenticationServiceImpl();
  }

  JsonObject createAnonymousAccessToken(String clientId, String clientKey, String grantType, String requestDomain);

  JsonObject createBasicAuthAccessToken(String clientId, String clientKey, String grantType, String requestDomain, String basicAuthCredentials);

  JsonObject getAccessToken(String token);

  boolean deleteAccessToken(String token);

  JsonObject authorize();
}
