package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

public class AuthorizeServiceImpl implements AuthorizeService {

  private AuthorizeService authorizeService;
  
  public AuthorizeServiceImpl() {
    setAuthorizeService(AuthorizeService.getInstance());
  }
  
  @Override
  public JsonObject authorize(String clientId, String clientKey, String grantType, String returnUrl) {
    
    return null;
  }

  public AuthorizeService getAuthorizeService() {
    return authorizeService;
  }

  public void setAuthorizeService(AuthorizeService authorizeService) {
    this.authorizeService = authorizeService;
  }



}
