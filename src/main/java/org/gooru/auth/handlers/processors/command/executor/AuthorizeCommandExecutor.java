package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.processors.MessageContext;
import org.gooru.auth.handlers.processors.data.transform.model.AuthorizeDTO;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.gooru.auth.handlers.processors.service.authorize.AuthorizeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthorizeCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizeCommandExecutor.class);

  private AuthorizeService authorizeService;

  public AuthorizeCommandExecutor() {
    setAuthorizeService(AuthorizeService.instance());
  }

  @Override
  public MessageResponse exec(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.AUTHORIZE:
      AuthorizeDTO authorizeDTO = new AuthorizeDTO(messageContext.requestBody().getMap());
      String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
      result = getAuthorizeService().authorize(authorizeDTO, requestDomain);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

  public AuthorizeService getAuthorizeService() {
    return authorizeService;
  }

  public void setAuthorizeService(AuthorizeService authorizeService) {
    this.authorizeService = authorizeService;
  }
}
