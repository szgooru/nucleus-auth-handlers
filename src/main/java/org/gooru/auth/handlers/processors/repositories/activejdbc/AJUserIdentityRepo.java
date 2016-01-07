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

  @Override
  public UserIdentity getUserIdentityByUsernameAndPassword(String username, String password) {
    return query(GET_BY_USERNAME_PASSWORD, username, password);
  }

  @Override
  public UserIdentity getUserIdentityByEmailIdAndPassword(String emailId, String password) {
    return query(GET_BY_EMAIL_PASSWORD, emailId, password);
  }

  @Override
  public UserIdentity getUserIdentityByEmailId(String emailId) {
    return query(GET_BY_EMAIL, emailId);
  }

  @Override
  public UserIdentity getUserIdentityByReferenceId(String referenceId) {
    return query(GET_BY_REFERENCE, referenceId);
  }

  @Override
  public UserIdentity getUserIdentityByUsername(String username) {
    return query(GET_BY_USERNAME, username);
  }

  @Override
  public UserIdentity saveOrUpdate(UserIdentity userIdentity) {
    Base.open(dataSource());
    userIdentity.saveIt();
    Base.commitTransaction();
    Base.close();
    return userIdentity;
  }

  private UserIdentity query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<UserIdentity> results = UserIdentity.where(whereClause, params);
    UserIdentity result = results.size() > 0 ? (UserIdentity) results.get(0) : null;
    Base.close();
    return result;
  }

}
