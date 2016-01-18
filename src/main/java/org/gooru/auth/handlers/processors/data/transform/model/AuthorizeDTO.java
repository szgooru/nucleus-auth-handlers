package org.gooru.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonObject;

import java.util.Map;

public class AuthorizeDTO extends JsonObject {

  public AuthorizeDTO() { 
    super();
  }
  
  public AuthorizeDTO(Map<String, Object> map) { 
    super(map);
  }

  private String clientId;
  
  private String clientKey;
  
  private String grantType;
  
  private String returnUrl;
  
  private UserDTO user;
  
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientKey() {
    return clientKey;
  }

  public void setClientKey(String clientKey) {
    this.clientKey = clientKey;
  }

  public String getGrantType() {
    return grantType;
  }

  public void setGrantType(String grantType) {
    this.grantType = grantType;
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  public UserDTO getUser() {
    return user;
  }

  public void setUser(UserDTO user) {
    this.user = user;
  }
}
