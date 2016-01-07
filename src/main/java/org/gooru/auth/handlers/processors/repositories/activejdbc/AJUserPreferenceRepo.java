package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserPreference;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJUserPreferenceRepo extends AJAbstractRepo implements UserPreferenceRepo {

  public static final String GET_USER_PREFERENCE = "user_id = ?";

  @Override
  public UserPreference getUserPreference(String userId) {
    return query(GET_USER_PREFERENCE, userId);
  }

  @Override
  public UserPreference updatePreference(UserPreference userPreference) {
    Base.open(dataSource());
    userPreference.saveIt();
    Base.commitTransaction();
    Base.close();
    return userPreference;
  }

  private UserPreference query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<UserPreference> results = UserPreference.where(whereClause, params);
    UserPreference userPreference = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userPreference;
  }
}
