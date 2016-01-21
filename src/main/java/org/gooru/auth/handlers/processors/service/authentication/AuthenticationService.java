package org.gooru.auth.handlers.processors.service.authentication;

import org.gooru.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.auth.handlers.processors.service.MessageResponse;

public interface AuthenticationService {
  static AuthenticationService instance() {
    return new AuthenticationServiceImpl();
  }

  MessageResponse createAnonymousAccessToken(AuthClientDTO authClientDTO, String requestDomain);

  MessageResponse createBasicAuthAccessToken(AuthClientDTO authClientDTO, String requestDomain, String basicAuthCredentials);

  MessageResponse deleteAccessToken(String token);
  
  MessageResponse getAccessToken(String token);

}
