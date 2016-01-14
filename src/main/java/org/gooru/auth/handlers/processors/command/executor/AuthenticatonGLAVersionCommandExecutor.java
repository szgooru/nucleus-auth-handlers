package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
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
  public JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.CREATE_ACCESS_TOKEN:
      String password = null;
      String username = null;
      if (body != null) {
        password = body.getString(ParameterConstants.PARAM_USER_PASSWORD);
        username = body.getString(ParameterConstants.PARAM_USER_USERNAME);
      }
      String clientKey = headers.get(MessageConstants.MSG_HEADER_API_KEY);
      if (clientKey == null) {
        clientKey = params.getString(ParameterConstants.PARAM_API_KEY);
      }
      final String requestDomain = headers.get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
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
