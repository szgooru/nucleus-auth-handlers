package org.gooru.auth.handlers.processors.service.authentication;

import io.vertx.core.json.JsonObject;

public interface AuthenticationService {
  static AuthenticationService instance() {
    return new AuthenticationServiceImpl();
  }

  JsonObject createAnonymousAccessToken(String clientId, String clientKey, String grantType, String requestDomain);

  JsonObject createBasicAuthAccessToken(String clientId, String clientKey, String grantType, String requestDomain, String basicAuthCredentials);

  boolean deleteAccessToken(String token);

}
