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
  public User save(User user) {
    Base.open(dataSource());
    user.setId(user.getId());
    user.toInsert();
    user.insert();
    Base.commitTransaction();
    Base.close();
    return user;
  }

  @Override
  protected <T extends Model> T query(String whereClause, Object... params) {
    // TODO Auto-generated method stub
    return null;
  }

}
