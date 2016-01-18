package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.MessageContext;
import org.gooru.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.MessageResponse;
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
  public MessageResponse exec(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.CREATE_USER:
      UserDTO userDTO = new UserDTO(messageContext.requestBody().getMap());
      result = getUserService().createUserAccount(userDTO, messageContext.user());
      break;
    case CommandConstants.UPDATE_USER:
      String updateuserId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
      if (updateuserId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        updateuserId = messageContext.user().getString(ParameterConstants.PARAM_USER_ID);
      }
      UserDTO updateUserDTO = new UserDTO(messageContext.requestBody().getMap());
      result = getUserService().updateUser(updateuserId, updateUserDTO);
      break;
    case CommandConstants.GET_USER:
      String userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
      if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
        userId = messageContext.user().getString(ParameterConstants.PARAM_USER_ID);
      }
      result = getUserService().getUser(userId);
      break;
    case CommandConstants.GET_USER_FIND:
      final String username = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_USERNAME);
      final String email = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_EMAIL);
      result = getUserService().findUser(username, email);
      break;
    case CommandConstants.RESET_PASSWORD:
      final String emailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
      result = getUserService().resetPassword(emailId);
      break;
    case CommandConstants.UPDATE_PASSWORD:
      final String userUpdatePasswordId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
      final String resetPasswordToken = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_TOKEN);
      final String newPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
      if (messageContext.user().getUserId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
        result = getUserService().resetUnAuthenticateUserPassword(resetPasswordToken, newPassword);
      } else {
        final String oldPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_OLD_PASSWORD);
        result = getUserService().resetAuthenticateUserPassword(userUpdatePasswordId, oldPassword, newPassword);
      }
      break;
    case CommandConstants.RESET_EMAIL_ADDRESS:
      result = getUserService().updateUserEmail(messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID));
      break;
    case CommandConstants.RESEND_CONFIRMATION_EMAIL:
      result = getUserService().resendConfirmationEmail(messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID));
      break;
    case CommandConstants.CONFIRMATION_EMAIL:
      String token = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_TOKEN);
      result = getUserService().confirmUserEmail(messageContext.user().getUserId(), token);
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
