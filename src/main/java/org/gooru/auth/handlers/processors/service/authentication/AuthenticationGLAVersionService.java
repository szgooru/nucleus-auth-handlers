package org.gooru.auth.handlers.processors.service.authentication;

import org.gooru.auth.handlers.processors.service.MessageResponse;

public interface AuthenticationGLAVersionService {
  static AuthenticationGLAVersionService instance() {
    return new AuthenticationGLAVersionServiceImpl();
  }

  MessageResponse createAnonymousAccessToken(String clientKey, String requestDomain);

  MessageResponse createBasicAuthAccessToken(String clientKey, String requestDomain, String username, String password);


}
