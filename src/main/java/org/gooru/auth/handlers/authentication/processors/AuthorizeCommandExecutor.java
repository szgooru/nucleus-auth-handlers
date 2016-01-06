package org.gooru.auth.handlers.authentication.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.constants.CommandConstants;
import org.gooru.auth.handlers.authentication.constants.MessageConstants;
import org.gooru.auth.handlers.authentication.constants.ParameterConstants;
import org.gooru.auth.handlers.authentication.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.authentication.processors.service.AuthenticationService;
import org.gooru.auth.handlers.authentication.processors.service.AuthorizeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthorizeCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizeCommandExecutor.class);

  private AuthorizeService authorizeService;

  public AuthorizeCommandExecutor() {
    setAuthorizeService(AuthorizeService.getInstance());
  }

  @Override
  public JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.CREATE_ACCESS_TOKEN:
      // result = getAuthorizeService().authorize(clientId, clientKey,
      // grantType, returnUrl)
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
