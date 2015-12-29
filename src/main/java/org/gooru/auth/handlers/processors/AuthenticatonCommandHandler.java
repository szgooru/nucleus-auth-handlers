package org.gooru.auth.handlers.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.RxHelper;

import org.gooru.auth.handlers.authentication.model.AuthClient;
import org.gooru.auth.handlers.authentication.service.AuthenticationService;
import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;

import rx.Observable;

public final class AuthenticatonCommandHandler extends CommandHandler {

  private AuthenticationService authenticationService;

  public AuthenticatonCommandHandler() {
    this.setAuthenticationService(AuthenticationService.create());
  }

  @Override
  public Observable<JsonObject> exec(String command, MultiMap headers, JsonObject params, JsonObject body) {
    Observable<JsonObject> observable = RxHelper.observableFuture();
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
    return null;
  }

  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  public void setAuthenticationService(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }
}
