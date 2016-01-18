package org.gooru.auth.handlers.processors.service.user;

import org.gooru.auth.handlers.processors.service.MessageResponse;

import io.vertx.core.json.JsonObject;

public interface UserPrefsService {
  static UserPrefsService instance() { 
    return new UserPrefsServiceImpl();
  }

  MessageResponse updateUserPreference(String userId, JsonObject userPrefs);

  MessageResponse getUserPreference(String userId);
}
