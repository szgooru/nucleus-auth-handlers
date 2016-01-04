package org.gooru.auth.handlers.authentication.processors.repositories;

import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.AJAuthClientRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.AuthClient;

public interface AuthClientRepo {

  static AuthClientRepo getInstance() {
    return new AJAuthClientRepo();
  }

  AuthClient getAuthClient(String clientId, String clientKey);

  AuthClient getAuthClient(String clientKey);
}
