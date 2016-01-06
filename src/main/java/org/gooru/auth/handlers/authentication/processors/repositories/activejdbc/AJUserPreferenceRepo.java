package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc;

import org.gooru.auth.handlers.authentication.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserPreference;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

public class AJUserPreferenceRepo extends AJAbstractRepo implements UserPreferenceRepo {

  public static final String GET_USER_PREFERENCE = "user_id = ?";

  @Override
  public UserPreference getUserPreference(String userId) {
    return (UserPreference) query(GET_USER_PREFERENCE, userId);
  }

  @Override
  protected <T extends Model> T query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<T> results = UserPreference.where(whereClause, params);
    T userPreference = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userPreference;
  }
}
