package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonObject;

public class UserServiceImpl implements UserService {

  private UserService userService;
  
  public UserServiceImpl() { 
    setUserService(UserService.getInstance());
  }
  
  @Override
  public JsonObject createUser() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject updateUser() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject getUser() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject getUserPreference() {
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
