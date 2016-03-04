package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.AJAuthClientRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;

public interface AuthClientRepo {

  static AuthClientRepo instance() {
    return new AJAuthClientRepo();
  }

  AJEntityAuthClient getAuthClient(String clientId, String clientKey);

  AJEntityAuthClient getAuthClient(String clientKey);
}
