package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AuthClient;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJAuthClientRepo extends AJAbstractRepo implements AuthClientRepo {

  
  @Override
  public AuthClient getAuthClient(String clientId, String clientKey) {
    Base.open(dataSource());
    LazyList<AuthClient> results =  AuthClient.where("client_id = ? and client_key = ?", clientId, clientKey);
    AuthClient authClient = results.get(0);
    Base.close();
    return authClient;
  }

  @Override
  public AuthClient getAuthClient(String clientKey) {
    Base.open(dataSource());
    LazyList<AuthClient> results =  AuthClient.where("client_key = ?", clientKey);
    AuthClient authClient = results.get(0);
    Base.close();
    return authClient;
  }

}
