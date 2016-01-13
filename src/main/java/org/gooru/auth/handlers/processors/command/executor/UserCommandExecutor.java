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
    final String userContextId = userContext.getString(ParameterConstants.PARAM_USER_ID);
    switch (command) {
    case CommandConstants.CREATE_USER:
      final String clientId = userContext.getString(ParameterConstants.PARAM_CLIENT_ID);
      int accessTokenValidity = userContext.getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
      result = getUserService().createUserAccount(body, clientId, accessTokenValidity);
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
      final String username = params.getString(ParameterConstants.PARAM_USER_USERNAME);
      final String email = params.getString(ParameterConstants.PARAM_USER_EMAIL);
      result = getUserService().findUser(username, email);
      break;
    case CommandConstants.RESET_PASSWORD:
      final String emailId = body.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
      result = getUserService().resetPassword(emailId);
      break;
    case CommandConstants.UPDATE_PASSWORD:
      final String userUpdatePasswordId = params.getString(MessageConstants.MSG_USER_ID);
      final String resetPasswordToken = body.getString(ParameterConstants.PARAM_USER_TOKEN);
      final String newPassword = body.getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
      if (userContextId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
        result = getUserService().resetUnAuthenticateUserPassword(resetPasswordToken, newPassword);
      } else {
        final String oldPassword = body.getString(ParameterConstants.PARAM_USER_OLD_PASSWORD);
        result = getUserService().resetAuthenticateUserPassword(userUpdatePasswordId, oldPassword, newPassword);
      }
      break;
    case CommandConstants.RESET_EMAIL_ADDRESS:
      final String updateEmailId = body.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
      result = getUserService().updateUserEmail(updateEmailId);
      break;
    case CommandConstants.RESEND_CONFIRMATION_EMAIL:
      final String resendEmailId = body.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
      result = getUserService().resendConfirmationEmail(resendEmailId);
      break;
    case CommandConstants.CONFIRMATION_EMAIL:
      final String confirmEmailToken = body.getString(ParameterConstants.PARAM_USER_TOKEN);
      result = getUserService().confirmUserEmail(userContextId, confirmEmailToken);
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
