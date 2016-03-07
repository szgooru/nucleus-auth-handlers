package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;

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

public final class ResetUnAuthenticateUserPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  public ResetUnAuthenticateUserPasswordExecutor() {
    this.redisClient = RedisClient.instance();
    this.userIdentityRepo = UserIdentityRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String accessToken = messageContext.accessToken();
    final String token = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_TOKEN);
    final String newPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
    return resetUnAuthenticateUserPassword(token, newPassword, accessToken);
  }

  private MessageResponse resetUnAuthenticateUserPassword(String token, String password, String accessToken) {
    String emailId = getRedisClient().get(token);
    rejectIfNull(emailId, MessageCodeConstants.AU0028, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    rejectIfNull(password, MessageCodeConstants.AU0042, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    userIdentity.setPassword(InternalHelper.encryptPassword(password));
    getUserIdentityRepo().createOrUpdate(userIdentity);
    getRedisClient().del(token);
    MailNotifyBuilder  mailNotifyBuilder = new MailNotifyBuilder();
    mailNotifyBuilder.setTemplateName(MailTemplateConstants.PASSWORD_CHANGED).setAuthAccessToken(accessToken).addToAddress(userIdentity.getEmailId()).putContext(ParameterConstants.MAIL_TOKEN, token).putContext(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    return new MessageResponse.Builder().addMailNotify(mailNotifyBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

}
