package org.gooru.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonObject;

import java.util.Map;

import org.gooru.auth.handlers.constants.ParameterConstants;

public class AuthorizeDTO extends JsonObject {

  public AuthorizeDTO() {
    super();
  }

  public AuthorizeDTO(Map<String, Object> map) {
    super(map);
  }

  public String getClientId() {
    return getString(ParameterConstants.PARAM_CLIENT_ID);
  }

  public String getClientKey() {
    return getString(ParameterConstants.PARAM_CLIENT_KEY);
  }

  public String getGrantType() {
    return getString(ParameterConstants.PARAM_GRANT_TYPE);
  }

  public String getReturnUrl() {
    return getString(ParameterConstants.PARAM_RETURN_URL);
  }
  
  public String getIdentityId() {
    return getString(ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID);
  }

  public UserDTO getUser() {
    return new UserDTO(getJsonObject(ParameterConstants.PARAM_USER).getMap());
  }
}
