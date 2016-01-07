package org.gooru.auth.handlers.processors.service.authorize;

import io.vertx.core.json.JsonObject;

public class AuthorizeServiceImpl implements AuthorizeService {

  private AuthorizeService authorizeService;
  
  public AuthorizeServiceImpl() {
    setAuthorizeService(AuthorizeService.instance());
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
