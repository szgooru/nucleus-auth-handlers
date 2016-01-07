package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJUserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserPreference;

public interface UserPreferenceRepo {

  static UserPreferenceRepo getInstance() {
    return new AJUserPreferenceRepo();
  }

  UserPreference getUserPreference(String userId);
  
  UserPreference updatePreference(UserPreference userPreference);
}
