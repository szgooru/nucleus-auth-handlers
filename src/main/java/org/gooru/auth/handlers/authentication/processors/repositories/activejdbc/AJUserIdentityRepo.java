package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc;

import org.gooru.auth.handlers.authentication.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserIdentity;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJUserIdentityRepo extends AJAbstractRepo implements UserIdentityRepo {

  @Override
  public UserIdentity getUserIdentityByUsernameAndPassword(String username, String password) {
    Base.open(dataSource());
    LazyList<UserIdentity> results = UserIdentity.where("username = ?  and password = ? and login_type = 'credential' and status != 'deleted'", username, password);
    UserIdentity userIdentity = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userIdentity;
  }

  @Override
  public UserIdentity getUserIdentityByEmailIdAndPassword(String emailId, String password) {
    Base.open(dataSource());
    LazyList<UserIdentity> results = UserIdentity.where("email_id = ?  and password = ? and login_type = 'credential' and status != 'deleted'", emailId, password);
    UserIdentity userIdentity = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userIdentity;
  }

  @Override
  public UserIdentity getUserIdentityByEmailId(String emailId) {
    Base.open(dataSource());
    LazyList<UserIdentity> results = UserIdentity.where("email_id = ?", emailId);
    UserIdentity userIdentity = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userIdentity;
  }

  @Override
  public UserIdentity getUserIdentityByReferenceId(String referenceId) {
    Base.open(dataSource());
    LazyList<UserIdentity> results = UserIdentity.where("reference_id = ?", referenceId);
    UserIdentity userIdentity = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userIdentity;
  }

}
