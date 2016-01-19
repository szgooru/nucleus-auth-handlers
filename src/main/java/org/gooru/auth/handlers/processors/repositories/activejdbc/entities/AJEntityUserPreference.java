package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_PREFERENCE)
@IdName(SchemaConstants.USER_ID)
public class AJEntityUserPreference extends Model {

  public String getUserId() {
    return getString("user_id");
  }

  public void setUserId(String userId) {
    setId(userId);
    set("user_id", userId);
  }

  public JsonArray getStandardPreference() {
    String json = getString("standard_preference");
    JsonArray prefs = null;
    if (json != null) {
      prefs = new JsonArray(json);
    }
    return prefs;
  }

  public void setStandardPreference(JsonObject standardPreference) {
    set("standard_preference", DBEnums.jsonObject(standardPreference));
  }

  public Boolean getProfileVisiblity(Boolean profileVisiblity) {
    return getBoolean("profile_visiblity");
  }

  public void setProfileVisiblity(Boolean profileVisiblity) {
    set("profile_visiblity", profileVisiblity);
  }

}
