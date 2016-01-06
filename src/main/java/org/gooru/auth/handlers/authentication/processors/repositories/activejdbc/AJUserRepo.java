package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc;

import org.gooru.auth.handlers.authentication.processors.repositories.UserRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

public class AJUserRepo extends AJAbstractRepo implements UserRepo {

  @Override
  public User getUser(String user) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public User saveOrUpdate(User user) {
    Base.open(dataSource());
    user.saveIt();
    Base.close();
    return user;
  }

  @Override
  protected <T extends Model> T query(String whereClause, Object... params) {
    // TODO Auto-generated method stub
    return null;
  }

}
