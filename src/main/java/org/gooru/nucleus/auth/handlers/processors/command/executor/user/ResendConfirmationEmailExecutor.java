package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonObject;

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
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

public final class ResendConfirmationEmailExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;

  private static final int EXPIRE_IN_SECONDS = 86400;

  public ResendConfirmationEmailExecutor() {
    this.redisClient = RedisClient.instance();
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.userRepo = UserRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    return resendConfirmationEmail(messageContext.user().getUserId());
  }

  private MessageResponse resendConfirmationEmail(String userId) {
    final AJEntityUser user = getUserRepo().getUser(userId);
    rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(user.getEmailId());
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED), MessageCodeConstants.AU0009,
        HttpConstants.HttpStatus.FORBIDDEN.getCode());
    final String token = InternalHelper.generateEmailConfirmToken(userId);
    JsonObject tokenData = new JsonObject();
    tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
    tokenData.put(ParameterConstants.PARAM_USER_ID, userId);
    getRedisClient().set(token, tokenData.toString(), EXPIRE_IN_SECONDS);
    MailNotifyBuilder mailConfirmNotifyBuilder = new MailNotifyBuilder();
    mailConfirmNotifyBuilder.setTemplateName(MailTemplateConstants.USER_REGISTARTION_CONFIRMATION).addToAddress(userIdentity.getEmailId())
        .putContext(ParameterConstants.MAIL_TOKEN, token);
    return new MessageResponse.Builder().addMailNotify(mailConfirmNotifyBuilder.build()).setContentTypeJson().setStatusOkay().successful().build();
  }

  private UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  private UserRepo getUserRepo() {
    return userRepo;
  }

  private RedisClient getRedisClient() {
    return redisClient;
  }

}
