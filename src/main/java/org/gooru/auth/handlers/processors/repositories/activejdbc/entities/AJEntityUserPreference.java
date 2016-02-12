package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_PREFERENCE)
@IdName(SchemaConstants.USER_ID)
public class AJEntityUserPreference extends Model {

  public String getUserId() {
    return getString(ParameterConstants.PARAM_USER_ID);
  }

  public void setUserId(Object userId) {
    setId(userId);
    set(ParameterConstants.PARAM_USER_ID, userId);
  }

  public JsonObject getStandardPreference() {
    String json = getString(ParameterConstants.PARAM_STANDARD_PREFERENCE);
    JsonObject prefs = null;
    if (json != null) {
      prefs = new JsonObject(json);
    }
    return prefs;
  }

  public void setStandardPreference(JsonObject standardPreference) {
    set(ParameterConstants.PARAM_STANDARD_PREFERENCE, DBEnums.jsonObject(standardPreference));
  }

  public Boolean getProfileVisiblity(Boolean profileVisiblity) {
    return getBoolean(ParameterConstants.PARAM_PROFILE_VISIBILITY);
  }

  public void setProfileVisiblity(Boolean profileVisiblity) {
    set(ParameterConstants.PARAM_PROFILE_VISIBILITY, profileVisiblity);
  }

}
