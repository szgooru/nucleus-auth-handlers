package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJUserPreferenceRepo extends AJAbstractRepo implements UserPreferenceRepo {

  public static final String GET_USER_PREFERENCE = "user_id = ?";

  @Override
  public AJEntityUserPreference getUserPreference(String userId) {
    return query(GET_USER_PREFERENCE, userId);
  }

  @Override
  public AJEntityUserPreference updatePreference(AJEntityUserPreference userPreference) {
    Base.open(dataSource());
    userPreference.saveIt();
    Base.commitTransaction();
    Base.close();
    return userPreference;
  }

  private AJEntityUserPreference query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<AJEntityUserPreference> results = AJEntityUserPreference.where(whereClause, params);
    AJEntityUserPreference userPreference = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return userPreference;
  }
}
