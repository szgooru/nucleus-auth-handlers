package org.gooru.auth.handlers.authentication.processors.repositories;

import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.AJUserPreferenceRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserPreference;

public interface UserPreferenceRepo {

  static UserPreferenceRepo getInstance() {
    return new AJUserPreferenceRepo();
  }

  UserPreference getUserPreference(String userId);
}
