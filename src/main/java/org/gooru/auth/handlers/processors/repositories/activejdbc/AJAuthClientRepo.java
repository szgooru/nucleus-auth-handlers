package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

public class AJAuthClientRepo extends AJAbstractRepo implements AuthClientRepo {

  private static final String GET_AUTH_CLIENT_ID_AND_KEY = "client_id = ? and client_key = ?";
  private static final String GET_AUTH_CLIENT_KEY = "client_key = ?";

  @Override
  public AJEntityAuthClient getAuthClient(String clientId, String clientKey) {
    return (AJEntityAuthClient) query(GET_AUTH_CLIENT_ID_AND_KEY, clientId, clientKey);
  }

  @Override
  public AJEntityAuthClient getAuthClient(String clientKey) {
    return (AJEntityAuthClient) query(GET_AUTH_CLIENT_KEY, clientKey);
  }

  public <T extends Model> T query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<T> results = AJEntityAuthClient.<T> where(whereClause, params);
    T result = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return result;
  }

}
