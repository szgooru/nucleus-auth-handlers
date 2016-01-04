package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.User;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserIdentity;

public interface UserService {

  static UserService getInstance() {
    return new UserServiceImpl();
  }

  JsonObject createUser(User user, UserIdentity userIdentity);

  JsonObject updateUser(User user, UserIdentity userIdentity);

  JsonObject getUser(String userId, boolean includeIdentities);

  JsonObject updateUserPreference(String userId);
  
  JsonObject getUserPreference(String userId);
}
