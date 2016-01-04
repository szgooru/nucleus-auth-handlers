package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonObject;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("user_preference")
public class UserPreference extends Model {


  public JsonObject getStandardPreference() {
    return (JsonObject) get("standard_preference");
  }

  
}
