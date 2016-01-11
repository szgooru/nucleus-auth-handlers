package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(UserCommandExecutor.class);

  private UserService userService;

  public UserCommandExecutor() {
    setUserService(UserService.instance());
  }

  @Override
  public JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.CREATE_USER:
      result =
              getUserService().createUser(body, userContext.getString(ParameterConstants.PARAM_CLIENT_ID),
                      userContext.getJsonObject(ParameterConstants.PARAM_CDN_URLS),
                      userContext.getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY));
      break;
    case CommandConstants.UPDATE_USER:
      String updateuserId = params.getString(MessageConstants.MSG_USER_ID);
      if (updateuserId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        updateuserId = userContext.getString(ParameterConstants.PARAM_USER_ID);
      }
      result = getUserService().updateUser(updateuserId, body);
      break;
    case CommandConstants.GET_USER:
      String userId = params.getString(MessageConstants.MSG_USER_ID);
      if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        userId = userContext.getString(ParameterConstants.PARAM_USER_ID);
      }
      result = getUserService().getUser(userId);
      break;
    case CommandConstants.GET_USER_FIND:
      String username = params.getString(ParameterConstants.PARAM_USER_USERNAME);
      String emailId = params.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
      
      result = getUserService().getUser(userId);
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
