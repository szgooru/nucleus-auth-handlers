package org.gooru.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.ParameterConstants;

public class UserPrefsDTO  {

  private JsonObject requestBody;
  
  public UserPrefsDTO(JsonObject requestBody) {
    this.requestBody = requestBody;
  }

  public JsonObject getStandardPreference() {
    return this.requestBody.getJsonObject(ParameterConstants.PARAM_STANDARD_PREFERENCE);
  }

  public Boolean getProfileVisibility() {
    return this.requestBody.getBoolean(ParameterConstants.PARAM_PROFILE_VISIBILITY);
  }
}
