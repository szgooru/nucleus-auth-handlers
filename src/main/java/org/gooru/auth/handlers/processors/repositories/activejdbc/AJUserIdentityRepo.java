package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJUserIdentityRepo extends AJAbstractRepo implements UserIdentityRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJUserIdentityRepo.class);

  private static final String GET_BY_USERNAME_PASSWORD = "username = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
  private static final String GET_BY_EMAIL_PASSWORD = "email_id = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
  private static final String GET_BY_EMAIL = "email_id = ? and status != 'deleted'";
  private static final String GET_BY_USERNAME = "username = ? and status != 'deleted'";
  private static final String GET_BY_REFERENCE = "reference_id = ? and status != 'deleted'";
  private static final String GET_BY_ID_PASSWORD = "user_id = ? and password = ? and status != 'deleted'";
  private static final String GET_BY_USER_ID = "user_id = ? and  status != 'deleted'";

  @Override
  public AJEntityUserIdentity getUserIdentityByUsernameAndPassword(final String username, final String password) {
    return query(GET_BY_USERNAME_PASSWORD, username, password);
  }

  @Override
  public AJEntityUserIdentity getUserIdentityByEmailIdAndPassword(final String emailId, final String password) {
    return query(GET_BY_EMAIL_PASSWORD, emailId, password);
  }

  @Override
  public AJEntityUserIdentity getUserIdentityByEmailId(final String emailId) {
    return query(GET_BY_EMAIL, emailId);
  }

  @Override
  public AJEntityUserIdentity getUserIdentityByReferenceId(final String referenceId) {
    return query(GET_BY_REFERENCE, referenceId);
  }

  @Override
  public AJEntityUserIdentity getUserIdentityByUsername(final String username) {
    return query(GET_BY_USERNAME, username);
  }

  @Override
  public AJEntityUserIdentity getUserIdentityByIdAndPassword(final String userId, final String password) {
    return query(GET_BY_ID_PASSWORD, userId, password);
  }

  @Override
  public AJEntityUserIdentity getUserIdentityById(final String userId) {
    return query(GET_BY_USER_ID, userId);
  }

  @Override
  public AJEntityUserIdentity createOrUpdate(final AJEntityUserIdentity userIdentity) {
    return (AJEntityUserIdentity) saveOrUpdate(userIdentity);
  }

  private AJEntityUserIdentity query(final String whereClause, final Object... params) {
    AJEntityUserIdentity userIdentity = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityUserIdentity> results = AJEntityUserIdentity.where(whereClause, params);
      userIdentity = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connetion to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return userIdentity;
  }
}