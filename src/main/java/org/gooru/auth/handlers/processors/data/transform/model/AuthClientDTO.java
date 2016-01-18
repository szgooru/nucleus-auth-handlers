package org.gooru.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonObject;

import java.util.Map;

import org.gooru.auth.handlers.constants.ParameterConstants;

public class AuthClientDTO extends JsonObject {

  public AuthClientDTO() {
    super();
  }

  public AuthClientDTO(Map<String, Object> map) {
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

}
