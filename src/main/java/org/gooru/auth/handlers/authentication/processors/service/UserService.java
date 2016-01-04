package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

public interface UserService {

  static UserService getInstance() {
    return new UserServiceImpl();
  }

  JsonObject createUser();

  JsonObject updateUser();

  JsonObject getUser();

  JsonObject getUserPreference();
}
