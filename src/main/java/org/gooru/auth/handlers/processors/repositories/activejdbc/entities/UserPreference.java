package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("user_preference")
public class UserPreference extends Model {

  public JsonArray getStandardPreference() {
    String json = getString("standard_preference");
    JsonArray prefs = null;
    if (json != null) {
      prefs = new JsonArray(json);
    }
    return prefs;
  }

}
