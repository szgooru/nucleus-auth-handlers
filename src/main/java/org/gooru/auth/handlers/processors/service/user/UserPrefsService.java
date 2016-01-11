package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

public interface UserPrefsService {
  static UserPrefsService instance() { 
    return new UserPrefsServiceImpl();
  }

  JsonObject updateUserPreference(String userId, JsonObject userPrefs);

  JsonObject getUserPreference(String userId);
}
