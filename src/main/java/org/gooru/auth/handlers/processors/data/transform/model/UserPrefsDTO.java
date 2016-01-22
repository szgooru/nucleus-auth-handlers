package org.gooru.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class UserPrefsDTO extends JsonObject {

  public UserPrefsDTO() {
    super();
  }

  public UserPrefsDTO(Map<String, Object> map) {
    super(map);
  }  

  public JsonArray getStandardPreference() {
    return getJsonArray("standard_preference");
  }

  public Boolean getProfileVisiblity() {
    return getBoolean("profile_visiblity");
  }
}
