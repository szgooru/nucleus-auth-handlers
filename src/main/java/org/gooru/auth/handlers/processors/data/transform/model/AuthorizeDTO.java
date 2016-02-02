package org.gooru.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.ParameterConstants;

public class AuthorizeDTO {

  private JsonObject requestBody;

  public AuthorizeDTO(JsonObject requestBody) {
    this.requestBody = requestBody;
  }

  public String getClientId() {
    return this.requestBody.getString(ParameterConstants.PARAM_CLIENT_ID);
  }

  public String getClientKey() {
    return this.requestBody.getString(ParameterConstants.PARAM_CLIENT_KEY);
  }

  public String getGrantType() {
    return this.requestBody.getString(ParameterConstants.PARAM_GRANT_TYPE);
  }

  public String getIdentityId() {
    return this.requestBody.getString(ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID);
  }

  public UserDTO getUser() {
    return new UserDTO(this.requestBody.getJsonObject(ParameterConstants.PARAM_USER));
  }
}
