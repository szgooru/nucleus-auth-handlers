package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJAuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AuthClient;

public interface AuthClientRepo {

  static AuthClientRepo getInstance() {
    return new AJAuthClientRepo();
  }

  AuthClient getAuthClient(String clientId, String clientKey);

  AuthClient getAuthClient(String clientKey);
}
