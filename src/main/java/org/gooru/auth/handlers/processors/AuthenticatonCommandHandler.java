package org.gooru.auth.handlers.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.service.AuthenticationService;
import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticatonCommandHandler extends CommandHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticatonCommandHandler.class);
  
  private AuthenticationService authenticationService;

  public AuthenticatonCommandHandler() {
    this.setAuthenticationService(AuthenticationService.create());
  }

  @Override
  public JsonObject exec(String command, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.CREATE_ACCESS_TOKEN:
      result = getAuthenticationService().createAnonymousAccessToken(body.getString("client_id"), body.getString("client_key"));
      break;
    case CommandConstants.GET_ACCESS_TOKEN:
      break;
    case CommandConstants.DELETE_ACCESS_TOKEN:
      String token = headers.get(MessageConstants.MSG_HEADER_TOKEN);
      getAuthenticationService().deleteAccessToken(token);
      break;
    case CommandConstants.AUTHORIZE:
      getAuthenticationService().authorize();
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
