package org.gooru.auth.handlers.authentication.Dao;

import org.gooru.auth.handlers.authentication.model.AuthClient;

public class AuthenticationImplDao extends AbstractDao implements AuthenticationDao {

  private static final String GET_AUTH_CLIENT = "select * from auth_client where";
  
  @Override
  public AuthClient getAuthClient(String clientId, String secretKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AuthClient getAuthClient(String secretKey) {
    // TODO Auto-generated method stub
    return null;
  }

}
