package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.user.UserPrefsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserPrefsCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(UserPrefsCommandExecutor.class);

  private UserPrefsService userPrefsService;

  public UserPrefsCommandExecutor() {
    setUserPrefsService(UserPrefsService.instance());
  }

  @Override
  public JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.UPDATE_USER_PREFERENCE:
      String userUpdateId = params.getString(MessageConstants.MSG_USER_ID);
      if (userUpdateId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        userUpdateId = userContext.getString(ParameterConstants.PARAM_USER_ID);
      }
      result = getUserPrefsService().updateUserPreference(userUpdateId, body);
      break;
    case CommandConstants.GET_USER_PREFERENCE:
      String userId = params.getString(MessageConstants.MSG_USER_ID);
      if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        userId = userContext.getString(ParameterConstants.PARAM_USER_ID);
      }
      result = getUserPrefsService().getUserPreference(userId);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

  public UserPrefsService getUserPrefsService() {
    return userPrefsService;
  }

  public void setUserPrefsService(UserPrefsService userPrefsService) {
    this.userPrefsService = userPrefsService;
  }
}
