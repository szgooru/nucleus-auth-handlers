package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc;

import org.gooru.auth.handlers.authentication.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserPreference;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJUserPreferenceRepo extends AJAbstractRepo implements UserPreferenceRepo {

  @Override
  public UserPreference getUserPreference(String userId) {
    Base.open(dataSource());
    LazyList<UserPreference> results = UserPreference.where("user_id = ?", userId);
    UserPreference userPreference = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userPreference;
  }
}
