package org.gooru.auth.handlers.processors;

import io.vertx.core.json.JsonObject;

import java.util.Map;

import org.gooru.auth.handlers.constants.ParameterConstants;

public class UserContext extends JsonObject {

  public UserContext() { 
    super();
  }
  public UserContext(Map<String, Object> map) { 
    super(map);
  }
  public String getUserId() {
    return getString(ParameterConstants.PARAM_USER_ID);
  }

  public JsonObject getPrefs() {
    return getJsonObject("prefs");
  }

  public String getClientId() {
    return getString(ParameterConstants.PARAM_CLIENT_ID);
  }

  public Integer getAccessTokenValidity() {
    return getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
  }
}
