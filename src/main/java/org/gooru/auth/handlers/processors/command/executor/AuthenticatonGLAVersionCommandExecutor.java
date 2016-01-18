package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.MessageContext;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.gooru.auth.handlers.processors.service.authentication.AuthenticationGLAVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticatonGLAVersionCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticatonGLAVersionCommandExecutor.class);

  private AuthenticationGLAVersionService authenticationGLAVersionService;

  public AuthenticatonGLAVersionCommandExecutor() {
    setAuthenticationGLAVersionService(AuthenticationGLAVersionService.instance());
  }

  @Override
  public MessageResponse exec(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.CREATE_ACCESS_TOKEN:
      String password = null;
      String username = null;
      if (messageContext.requestBody() != null) {
        password = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_PASSWORD);
        username = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_USERNAME);
      }
      String clientKey = messageContext.headers().get(MessageConstants.MSG_HEADER_API_KEY);
      if (clientKey == null) {
        clientKey = messageContext.requestParams().getString(ParameterConstants.PARAM_API_KEY);
      }
      final String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
      if (username != null && password != null) {
        result = getAuthenticationGLAVersionService().createBasicAuthAccessToken(clientKey, requestDomain, username, password);
      } else {
        result = getAuthenticationGLAVersionService().createAnonymousAccessToken(clientKey, requestDomain);
      }
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

  public AuthenticationGLAVersionService getAuthenticationGLAVersionService() {
    return authenticationGLAVersionService;
  }

  public void setAuthenticationGLAVersionService(AuthenticationGLAVersionService authenticationGLAVersionService) {
    this.authenticationGLAVersionService = authenticationGLAVersionService;
  }
}
