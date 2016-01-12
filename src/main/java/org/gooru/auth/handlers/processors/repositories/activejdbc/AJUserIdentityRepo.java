package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserIdentity;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJUserIdentityRepo extends AJAbstractRepo implements UserIdentityRepo {

  private static final String GET_BY_USERNAME_PASSWORD = "username = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
  private static final String GET_BY_EMAIL_PASSWORD = "email_id = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
  private static final String GET_BY_EMAIL = "email_id = ? and status != 'deleted'";
  private static final String GET_BY_USERNAME = "username = ? and status != 'deleted'";
  private static final String GET_BY_REFERENCE = "reference_id = ? and status != 'deleted'";
  private static final String GET_BY_ID_PASSWORD = "user_id = ? and password = ? and status != 'deleted'";
  private static final String GET_BY_USER_ID = "user_id = ? and  status != 'deleted'";

  @Override
  public UserIdentity getUserIdentityByUsernameAndPassword(final String username, final String password) {
    return query(GET_BY_USERNAME_PASSWORD, username, password);
  }

  @Override
  public UserIdentity getUserIdentityByEmailIdAndPassword(final String emailId, final String password) {
    return query(GET_BY_EMAIL_PASSWORD, emailId, password);
  }

  @Override
  public UserIdentity getUserIdentityByEmailId(final String emailId) {
    return query(GET_BY_EMAIL, emailId);
  }

  @Override
  public UserIdentity getUserIdentityByReferenceId(final String referenceId) {
    return query(GET_BY_REFERENCE, referenceId);
  }

  @Override
  public UserIdentity getUserIdentityByUsername(final String username) {
    return query(GET_BY_USERNAME, username);
  }
  
  @Override
  public UserIdentity getUserIdentityByIdAndPassword(final String userId, final String password) {
    return query(GET_BY_ID_PASSWORD, userId, password);
  }
  
  @Override
  public UserIdentity getUserIdentityById(String userId) {
    return query(GET_BY_USER_ID, userId);
  }

  @Override
  public UserIdentity saveOrUpdate(final UserIdentity userIdentity) {
    Base.open(dataSource());
    userIdentity.saveIt();
    Base.commitTransaction();
    Base.close();
    return userIdentity;
  }

  private UserIdentity query(final String whereClause, final Object... params) {
    Base.open(dataSource());
    LazyList<UserIdentity> results = UserIdentity.where(whereClause, params);
    UserIdentity result = results.size() > 0 ? (UserIdentity) results.get(0) : null;
    Base.close();
    return result;
  }
}
