package org.gooru.auth.handlers.authentication.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.constants.CommandConstants;
import org.gooru.auth.handlers.authentication.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.authentication.processors.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(UserCommandExecutor.class);

  private UserService userService;

  public UserCommandExecutor() {
    setUserService(UserService.getInstance());
  }

  @Override
  public JsonObject exec(String command, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.CREATE_USER:
      break;
    case CommandConstants.UPDATE_USER:
      break;
    case CommandConstants.GET_USER:
      break;
    case CommandConstants.UPDATE_USER_PREFERENCE:
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

  public UserService getUserService() {
    return userService;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
