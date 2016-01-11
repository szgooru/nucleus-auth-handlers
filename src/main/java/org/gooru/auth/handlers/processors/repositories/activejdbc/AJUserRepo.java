package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJUserRepo extends AJAbstractRepo implements UserRepo {

  private static final String GET_USER = "id = ?";

  @Override
  public User getUser(String userId) {
    return query(GET_USER, userId);
  }

  @Override
  public User create(User user) {
    Base.open(dataSource());
    user.setId(user.getId());
    user.toInsert();
    user.insert();
    Base.commitTransaction();
    Base.close();
    return user;
  }

  @Override
  public User update(User user) {
    Base.open(dataSource());
    user.toUpdate();
    Base.commitTransaction();
    Base.close();
    return null;
  }

  private User query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<User> results = User.where(whereClause, params);
    User result = results.size() > 0 ? (User) results.get(0) : null;
    Base.close();
    return result;
  }

}
