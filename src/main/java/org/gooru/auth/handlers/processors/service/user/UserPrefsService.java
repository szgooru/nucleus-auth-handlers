package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserPreference;

public interface UserPrefsService {
  static UserPrefsService instance() { 
    return new UserPrefsServiceImpl();
  }
  JsonObject updateUserPreference(JsonObject userPrefs);

  JsonObject getUserPreference(String userId);
}
