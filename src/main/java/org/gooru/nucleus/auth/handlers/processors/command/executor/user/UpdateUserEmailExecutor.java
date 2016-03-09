package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonObject;

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
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

public final class UpdateUserEmailExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;


  public UpdateUserEmailExecutor() {
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.userRepo = UserRepo.instance();
    this.redisClient = RedisClient.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String newEmailId = null;
    if (messageContext.requestBody() != null) {
      newEmailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    }
    return updateUserEmail(messageContext.user().getUserId(), newEmailId);
  }

  private MessageResponse updateUserEmail(final String userId, final String emailId) {
    rejectIfNull(emailId, MessageCodeConstants.AU0014, HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_EMAIL_ID);
    AJEntityUserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    reject(userIdentityEmail != null, MessageCodeConstants.AU0023, HttpConstants.HttpStatus.BAD_REQUEST.getCode(), emailId,
        ParameterConstants.EMAIL_ADDRESS);
    AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    final String token = InternalHelper.generateEmailConfirmToken(userId);
    JsonObject tokenData = new JsonObject();
    tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, emailId);
    tokenData.put(ParameterConstants.PARAM_USER_ID, userId);
    getRedisClient().set(token, tokenData.toString(), HelperConstants.EXPIRE_IN_SECONDS);
    MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
    mailNotifyBuilder.setTemplateName(MailTemplateConstants.EMAIL_ADDRESS_CHANGE_REQUEST).addToAddress(emailId)
        .putContext(ParameterConstants.MAIL_TOKEN, token).putContext(ParameterConstants.OLD_EMAIL_ID, userIdentity.getEmailId())
        .putContext(ParameterConstants.NEW_EMAIL_ID, emailId);
    return new MessageResponse.Builder().setContentTypeJson().setResponseBody(null).addMailNotify(mailNotifyBuilder.build()).setStatusOkay()
        .successful().build();
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }
}
