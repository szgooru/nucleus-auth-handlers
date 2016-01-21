package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJUserRepo extends AJAbstractRepo implements UserRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJUserRepo.class);

  private static final String GET_USER = "id = ?";

  @Override
  public AJEntityUser create(AJEntityUser user) {
    return (AJEntityUser) save(user);
  }

  @Override
  public AJEntityUser update(AJEntityUser user) {
    return (AJEntityUser) saveOrUpdate(user);
  }

  @Override
  public AJEntityUser getUser(String userId) {
    return query(GET_USER, userId);
  }

  private AJEntityUser query(final String whereClause, final Object... params) {
    AJEntityUser user = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityUser> results = AJEntityUser.where(whereClause, params);
      user = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connetion to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return user;
  }

}
