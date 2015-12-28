package org.gooru.auth.handlers.message;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.impl.AuthenticationService;
import org.gooru.auth.handlers.authentication.model.AuthClient;
import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;

public final class AuthenticatonCommandHandler extends CommandHandler {

  private static Command instance = null;

  private AuthenticationService authenticationService;

  public AuthenticatonCommandHandler() {
    this.setAuthenticationService(AuthenticationService.create());
  }

  @Override
  public void send(String command, MultiMap headers,JsonObject params, JsonObject body, Handler<AsyncResult<JsonObject>> reply) {

    switch (command) {
    case CommandConstants.CREATE_ACCESS_TOKEN:
      getAuthenticationService().createAccessToken(new AuthClient(body)).subscribe(result -> System.out.print("aaaj" + result));
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
    }

  }

  public static synchronized Command getInstance() {
    if (instance == null) {
      instance = new AuthenticatonCommandHandler();
    }
    return instance;
  }

  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  public void setAuthenticationService(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }
}
