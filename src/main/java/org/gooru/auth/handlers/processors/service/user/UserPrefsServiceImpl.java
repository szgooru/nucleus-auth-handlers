package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserPreference;

public class UserPrefsServiceImpl implements UserPrefsService {

  private UserPreferenceRepo userPreferenceRepo;

  public UserPrefsServiceImpl() {
    setUserPreferenceRepo(UserPreferenceRepo.instance());
  }

  @Override
  public JsonObject updateUserPreference(String userId, JsonObject UserPreference) {
    return null;
  }

  @Override
  public JsonObject getUserPreference(String userId) {
    UserPreference userPreference = getUserPreferenceRepo().getUserPreference(userId);
    JsonObject json = null;
    if (userPreference != null) {
      json = new JsonObject(userPreference.toMap());
    }
    return json;
  }

  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  }

  public void setUserPreferenceRepo(UserPreferenceRepo userPreferenceRepo) {
    this.userPreferenceRepo = userPreferenceRepo;
  }

}
