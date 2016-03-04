package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.AJUserPreferenceRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;

public interface UserPreferenceRepo {

  static UserPreferenceRepo instance() {
    return new AJUserPreferenceRepo();
  }

  AJEntityUserPreference getUserPreference(String userId);
  
  AJEntityUserPreference createPreference(AJEntityUserPreference userPreference);
  
  AJEntityUserPreference updatePreference(AJEntityUserPreference userPreference);
}
