package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.processors.MessageContext;
import org.gooru.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.gooru.auth.handlers.processors.service.authentication.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticatonCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticatonCommandExecutor.class);

  private AuthenticationService authenticationService;

  public AuthenticatonCommandExecutor() {
    setAuthenticationService(AuthenticationService.instance());
  }

  @Override
  public MessageResponse exec(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.CREATE_ACCESS_TOKEN:
      String basicAuthCredentials = messageContext.headers().get(MessageConstants.MSG_HEADER_BASIC_AUTH);
      String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
      AuthClientDTO authClientDTO = new AuthClientDTO(messageContext.requestBody().getMap());
      if (basicAuthCredentials == null) {
        result = getAuthenticationService().createAnonymousAccessToken(authClientDTO, requestDomain);
      } else {
        result = getAuthenticationService().createBasicAuthAccessToken(authClientDTO, requestDomain, basicAuthCredentials);
      }
      break;
    case CommandConstants.DELETE_ACCESS_TOKEN:
      result = getAuthenticationService().deleteAccessToken(messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN));
      break;
    case CommandConstants.GET_ACCESS_TOKEN:
      result = getAuthenticationService().getAccessToken(messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN));
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  public void setAuthenticationService(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }
}
