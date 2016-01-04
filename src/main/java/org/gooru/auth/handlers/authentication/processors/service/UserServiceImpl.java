package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.User;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserIdentity;

public class UserServiceImpl implements UserService {

  private UserService userService;

  public UserServiceImpl() {
    setUserService(UserService.getInstance());
  }

  @Override
  public JsonObject createUser(User user, UserIdentity userIdentity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject updateUser(User user, UserIdentity userIdentity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject getUser(String userId, boolean includeIdentities) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject updateUserPreference(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject getUserPreference(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  public UserService getUserService() {
    return userService;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

}
