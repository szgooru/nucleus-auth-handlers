package org.gooru.auth.handlers.processors.service.authentication;

import io.vertx.core.json.JsonObject;

public interface AuthenticationGLAVersionService {
  static AuthenticationGLAVersionService instance() {
    return new AuthenticationGLAVersionServiceImpl();
  }

  JsonObject createAnonymousAccessToken(String clientKey, String requestDomain);

  JsonObject createBasicAuthAccessToken(String clientKey, String requestDomain, String username, String password);


}
