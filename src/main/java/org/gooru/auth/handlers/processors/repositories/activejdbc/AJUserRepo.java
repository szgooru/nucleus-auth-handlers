package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJUserRepo extends AJAbstractRepo implements UserRepo {

  private static final String GET_USER = "id = ?";

  @Override
  public AJEntityUser getUser(String userId) {
    return query(GET_USER, userId);
  }

  @Override
  public AJEntityUser create(AJEntityUser user) {
    Base.open(dataSource());
    user.setId(user.getId());
    user.toInsert();
    user.insert();
    Base.commitTransaction();
    Base.close();
    return user;
  }

  @Override
  public AJEntityUser update(AJEntityUser user) {
    Base.open(dataSource());
    user.saveIt();
    Base.commitTransaction();
    Base.close();
    return user;
  }

  private AJEntityUser query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<AJEntityUser> results = AJEntityUser.where(whereClause, params);
    AJEntityUser result = results.size() > 0 ? (AJEntityUser) results.get(0) : null;
    Base.close();
    return result;
  }

}
