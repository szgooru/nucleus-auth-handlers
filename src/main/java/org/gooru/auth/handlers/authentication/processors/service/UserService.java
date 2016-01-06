package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

public interface UserService {

  static UserService getInstance() {
    return new UserServiceImpl();
  }

  JsonObject createUser(JsonObject userJson, String clientId);

  JsonObject updateUser(String userId, JsonObject user);

  JsonObject getUser(String userId);

  JsonObject updateUserPreference(String userId);
  
  JsonObject getUserPreference(String userId);
}
