package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJAuthClientRepo extends AJAbstractRepo implements AuthClientRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJAuthClientRepo.class);

  private static final String GET_AUTH_CLIENT_ID_AND_KEY = "client_id = ? and client_key = ?";
  private static final String GET_AUTH_CLIENT_KEY = "client_key = ?";

  @Override
  public AJEntityAuthClient getAuthClient(String clientId, String clientKey) {
    return query(GET_AUTH_CLIENT_ID_AND_KEY, clientId, clientKey);
  }

  @Override
  public AJEntityAuthClient getAuthClient(String clientKey) {
    return query(GET_AUTH_CLIENT_KEY, clientKey);
  }

  public AJEntityAuthClient query(String whereClause, Object... params) {
    AJEntityAuthClient authClient = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityAuthClient> results = AJEntityAuthClient.where(whereClause, params);
      authClient = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connetion to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return authClient;
  }

}