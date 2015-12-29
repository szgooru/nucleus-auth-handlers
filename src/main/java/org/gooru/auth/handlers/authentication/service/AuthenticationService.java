package org.gooru.auth.handlers.authentication.service;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.model.AuthClient;

import rx.Observable;

public interface AuthenticationService {
  static AuthenticationService create() {
    return new AuthenticationServiceImpl(); 
  }
  
  Observable<JsonObject> createAccessToken(AuthClient authClient);
  
  JsonObject deleteAccessToken(String token);
  
  JsonObject authorize();
}
