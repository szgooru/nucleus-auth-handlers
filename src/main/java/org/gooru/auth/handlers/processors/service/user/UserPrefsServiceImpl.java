package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HttpConstants.HttpStatus;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.auth.handlers.processors.service.MessageResponse;

public class UserPrefsServiceImpl implements UserPrefsService {

  private UserPreferenceRepo userPreferenceRepo;

  public UserPrefsServiceImpl() {
    setUserPreferenceRepo(UserPreferenceRepo.instance());
  }

  @Override
  public MessageResponse updateUserPreference(String userId, JsonObject UserPreference) {
    return null;
  }

  @Override
  public MessageResponse getUserPreference(String userId) {
    AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userId);
    JsonObject json = null;
    if (userPreference != null) {
      json = new JsonObject(userPreference.toMap());
    }
    return new MessageResponse.Builder().setResponseBody(json).setContentTypeJson().setStatusHttpCode(HttpStatus.ACCEPTED).build();
  }

  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  }

  public void setUserPreferenceRepo(UserPreferenceRepo userPreferenceRepo) {
    this.userPreferenceRepo = userPreferenceRepo;
  }

}
