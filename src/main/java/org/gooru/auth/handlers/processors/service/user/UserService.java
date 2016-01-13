package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

public interface UserService {

  static UserService instance() {
    return new UserServiceImpl();
  }

  JsonObject createUserAccount(JsonObject userJson, String clientId, int expireAtInSeconds);
  
  JsonObject updateUser(String userId, JsonObject user);

  JsonObject getUser(String userId);

  JsonObject findUser(String username, String email);

  JsonObject resetAuthenticateUserPassword(String userId, String oldPassword, String newPassword);

  JsonObject resetUnAuthenticateUserPassword(String token, String password);
  
  JsonObject resetPassword(String emailId);
  
  JsonObject resendConfirmationEmail(String emailId);
  
  JsonObject confirmUserEmail(String userId, String token);
  
  JsonObject updateUserEmail(String emailId);

}
