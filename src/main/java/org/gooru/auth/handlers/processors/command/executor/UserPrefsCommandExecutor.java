package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.MessageContext;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.MessageResponse;
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
  public MessageResponse exec(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.UPDATE_USER_PREFERENCE:
      String userUpdateId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
      if (userUpdateId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        userUpdateId = messageContext.user().getUserId();
      }
      result = getUserPrefsService().updateUserPreference(userUpdateId, messageContext.requestBody());
      break;
    case CommandConstants.GET_USER_PREFERENCE:
      String userId = messageContext.requestBody().getString(MessageConstants.MSG_USER_ID);
      if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        userId = messageContext.user().getUserId();
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
