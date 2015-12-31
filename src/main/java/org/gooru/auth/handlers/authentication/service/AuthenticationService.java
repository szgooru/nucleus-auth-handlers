package org.gooru.auth.handlers.authentication.service;

import io.vertx.core.json.JsonObject;

public interface AuthenticationService {
  static AuthenticationService create() {
    return new AuthenticationServiceImpl(); 
  }
  
  JsonObject createAnonymousAccessToken(String  clientId, String clientKey);
  
  JsonObject createBasicAuthAccessToken(String  clientId, String clientKey, String basicAuthCredentials);
  
  JsonObject deleteAccessToken(String token);
  
  JsonObject authorize();
}
