package org.gooru.auth.handlers.processors.command.executor.user;

import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;

import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.gooru.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.utils.InternalHelper;

public final class ResetAuthenticateUserPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  public ResetAuthenticateUserPasswordExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String newPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
    final String oldPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_OLD_PASSWORD);
    return resetAuthenticateUserPassword(messageContext.user().getUserId(), oldPassword, newPassword);
  }

  private MessageResponse resetAuthenticateUserPassword(String userId, String oldPassword, String newPassword) {
    rejectIfNull(oldPassword, MessageCodeConstants.AU0041, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    rejectIfNull(newPassword, MessageCodeConstants.AU0042, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    final AJEntityUserIdentity userIdentity =
            getUserIdentityRepo().getUserIdentityByIdAndPassword(userId, InternalHelper.encryptPassword(oldPassword));
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    userIdentity.setPassword(InternalHelper.encryptPassword(newPassword));
    getUserIdentityRepo().createOrUpdate(userIdentity);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_PASSWORD.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

}
