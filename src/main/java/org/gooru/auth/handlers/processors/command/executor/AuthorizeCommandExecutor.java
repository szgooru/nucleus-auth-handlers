package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
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
  public JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.AUTHORIZE:
      String clientId = body.getString(ParameterConstants.PARAM_CLIENT_ID);
      String clientKey = body.getString(ParameterConstants.PARAM_CLIENT_KEY);
      String grantType = body.getString(ParameterConstants.PARAM_GRANT_TYPE);
      String returnUrl = body.getString(ParameterConstants.PARAM_RETURN_URL);
      String requestDomain = headers.get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
      result = getAuthorizeService().authorize(body, clientId, clientKey, grantType, requestDomain, returnUrl);
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
