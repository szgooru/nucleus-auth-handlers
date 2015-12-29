package org.gooru.auth.handlers.authentication.Dao;

import org.gooru.auth.handlers.authentication.model.AuthClient;

public interface AuthenticationDao {

  AuthClient getAuthClient(String clientId, String secretKey);
  
  AuthClient getAuthClient(String secretKey);
}
