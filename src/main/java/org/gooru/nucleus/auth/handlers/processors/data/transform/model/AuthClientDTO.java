package org.gooru.nucleus.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

public class AuthClientDTO {

  private JsonObject requestBody;

  public AuthClientDTO(JsonObject requestBody) {
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

}
