package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

public class AuthorizeServiceImpl implements AuthorizeService {

  private AuthenticationService authenticationService;
  
  public AuthorizeServiceImpl() {
    setAuthenticationService(AuthenticationService.getInstance());
  }
  
  @Override
  public JsonObject authorize(String clientId, String clientKey, String grantType, String returnUrl) {
    
    return null;
  }

  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  public void setAuthenticationService(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

}
