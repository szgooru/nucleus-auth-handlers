package org.gooru.auth.handlers.processors.service.user;

import org.gooru.auth.handlers.processors.data.transform.model.UserPrefsDTO;
import org.gooru.auth.handlers.processors.service.MessageResponse;

public interface UserPrefsService {
  static UserPrefsService instance() { 
    return new UserPrefsServiceImpl();
  }

  MessageResponse updateUserPreference(String userId, UserPrefsDTO userPrefsDTO);

  MessageResponse getUserPreference(String userId);
}
