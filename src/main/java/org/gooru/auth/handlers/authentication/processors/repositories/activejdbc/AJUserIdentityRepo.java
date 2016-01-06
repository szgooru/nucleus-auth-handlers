package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc;

import org.gooru.auth.handlers.authentication.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserIdentity;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

public class AJUserIdentityRepo extends AJAbstractRepo implements UserIdentityRepo {

  private static final String GET_BY_USERNAME_PASSWORD = "username = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
  private static final String GET_BY_EMAIL_PASSWORD = "email_id = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
  private static final String GET_BY_EMAIL = "email_id = ? and status != 'deleted'";
  private static final String GET_BY_USERNAME = "username = ? and status != 'deleted'";
  private static final String GET_BY_REFERENCE = "reference_id = ? and status != 'deleted'";

  @Override
  public UserIdentity getUserIdentityByUsernameAndPassword(String username, String password) {
    return (UserIdentity) query(GET_BY_USERNAME_PASSWORD, username, password);
  }

  @Override
  public UserIdentity getUserIdentityByEmailIdAndPassword(String emailId, String password) {
    return (UserIdentity) query(GET_BY_EMAIL_PASSWORD, emailId, password);
  }

  @Override
  public UserIdentity getUserIdentityByEmailId(String emailId) {
    return (UserIdentity) query(GET_BY_EMAIL, emailId);
  }

  @Override
  public UserIdentity getUserIdentityByReferenceId(String referenceId) {
    return (UserIdentity) query(GET_BY_REFERENCE, referenceId);
  }

  @Override
  public UserIdentity getUserIdentityByUsername(String username) {
    return (UserIdentity) query(GET_BY_USERNAME, username);
  }

  @Override
  public UserIdentity saveOrUpdate(UserIdentity userIdentity) {
    Base.open(dataSource());
    userIdentity.saveIt();
    Base.close();
    return userIdentity;
  }

  @Override
  protected <T extends Model> T query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<T> results = UserIdentity.where(whereClause, params);
    T result = results.size() > 0 ? (T) results.get(0) : null;
    Base.close();
    return result;
  }

}
