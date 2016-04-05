package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;

public final class ResetPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  public ResetPasswordExecutor() {
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.redisClient = RedisClient.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String emailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    return resetPassword(emailId);
  }

  private MessageResponse resetPassword(final String emailId) {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    ServerValidatorUtility.rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
        ParameterConstants.PARAM_USER);
    final String token = InternalHelper.generatePasswordResetToken(userIdentity.getUserId());
    getRedisClient().set(token, userIdentity.getEmailId(), HelperConstants.EXPIRE_IN_SECONDS);
    MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
    mailNotifyBuilder.setTemplateName(MailTemplateConstants.PASSWORD_CHANGE_REQUEST).addToAddress(emailId)
        .putContext(ParameterConstants.MAIL_TOKEN, token).putContext(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    return new MessageResponse.Builder().setResponseBody(null).addMailNotify(mailNotifyBuilder.build()).setContentTypeJson().setStatusOkay()
        .successful().build();
  }

  private UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  private RedisClient getRedisClient() {
    return redisClient;
  }
}
