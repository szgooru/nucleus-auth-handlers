package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;

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

public final class ResetAuthenticateUserPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  public ResetAuthenticateUserPasswordExecutor() {
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.redisClient = RedisClient.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String newPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
    final String oldPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_OLD_PASSWORD);
    final String accessToken = messageContext.accessToken();
    return resetAuthenticateUserPassword(messageContext.user().getUserId(), oldPassword, newPassword, accessToken);
  }

  private MessageResponse resetAuthenticateUserPassword(String userId, String oldPassword, String newPassword, String accessToken) {
    rejectIfNull(oldPassword, MessageCodeConstants.AU0041, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    rejectIfNull(newPassword, MessageCodeConstants.AU0042, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    final AJEntityUserIdentity userIdentity =
        getUserIdentityRepo().getUserIdentityByIdAndPassword(userId, InternalHelper.encryptPassword(oldPassword));
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    userIdentity.setPassword(InternalHelper.encryptPassword(newPassword));
    getUserIdentityRepo().createOrUpdate(userIdentity);
    final String token = InternalHelper.generatePasswordResetToken(userIdentity.getUserId());
    getRedisClient().set(token, userIdentity.getEmailId(), HelperConstants.EXPIRE_IN_SECONDS);
    MailNotifyBuilder  mailNotifyBuilder = new MailNotifyBuilder();
    mailNotifyBuilder.setTemplateName(MailTemplateConstants.PASSWORD_CHANGED).setAuthAccessToken(accessToken).addToAddress(userIdentity.getEmailId()).putContext(ParameterConstants.MAIL_TOKEN, token).putContext(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    return new MessageResponse.Builder().setResponseBody(null).addMailNotify(mailNotifyBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
  }


  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

}
