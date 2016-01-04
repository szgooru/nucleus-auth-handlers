package org.gooru.auth.handlers.authentication.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.constants.CommandConstants;
import org.gooru.auth.handlers.authentication.constants.MessageConstants;
import org.gooru.auth.handlers.authentication.constants.ParameterConstants;
import org.gooru.auth.handlers.authentication.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.authentication.processors.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticatonCommandExecutor implements CommandExecutor  {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticatonCommandExecutor.class);

  private AuthenticationService authenticationService;

  public AuthenticatonCommandExecutor() {
    setAuthenticationService(AuthenticationService.getInstance());
  }

  @Override
  public JsonObject exec(String command, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.CREATE_ACCESS_TOKEN:
      String basicAuthCredentials = headers.get(MessageConstants.MSG_HEADER_BASIC_AUTH);
      String clientId = body.getString(ParameterConstants.PARAM_CLIENT_ID);
      String clientKey = body.getString(ParameterConstants.PARAM_CLIENT_KEY);
      String grantType = body.getString(ParameterConstants.PARAM_GRANT_TYPE);
      String requestDomain = headers.get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
      if (basicAuthCredentials == null) {
        result = getAuthenticationService().createAnonymousAccessToken(clientId, clientKey, grantType, requestDomain);
      } else {
        result = getAuthenticationService().createBasicAuthAccessToken(clientId, clientKey, grantType, requestDomain, basicAuthCredentials);
      }
      break;
    case CommandConstants.GET_ACCESS_TOKEN:
      result = getAuthenticationService().getAccessToken(headers.get(MessageConstants.MSG_HEADER_TOKEN));
      break;
    case CommandConstants.DELETE_ACCESS_TOKEN:
      getAuthenticationService().deleteAccessToken(headers.get(MessageConstants.MSG_HEADER_TOKEN));
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
