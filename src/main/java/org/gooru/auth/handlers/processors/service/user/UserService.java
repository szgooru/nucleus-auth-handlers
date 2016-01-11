package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

public interface UserService {

  static UserService instance() {
    return new UserServiceImpl();
  }

  JsonObject createUser(JsonObject userJson, String clientId, JsonObject cdnUrls , Integer expireAtInSeconds);

  JsonObject updateUser(String userId, JsonObject user);

  JsonObject getUser(String userId);
}
