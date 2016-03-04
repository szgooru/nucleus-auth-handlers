package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

public final class UpdateUserEmailExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;

  private final ConfigRegistry configRegistry = ConfigRegistry.instance();

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
    final String accessToken = messageContext.accessToken();
    return updateUserEmail(messageContext.user().getUserId(), newEmailId, accessToken);
  }

  private MessageResponse updateUserEmail(String userId, String emailId, String accessToken) {
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
    sendEmailAddressChangeRequestNotify(accessToken, token, userIdentity.getEmailId(), emailId);
    return new MessageResponse.Builder().setContentTypeJson().setResponseBody(null).setStatusOkay().successful().build();
  }

  private void sendEmailAddressChangeRequestNotify(String accessToken, String resetEmailToken, String oldEmailId, String newEmailId) {
    JsonObject data = new JsonObject();
    data.put(ParameterConstants.MAIL_TEMPLATE_NAME, MailTemplateConstants.EMAIL_ADDRESS_CHANGE_REQUEST);
    JsonArray toAddressJson = new JsonArray();
    toAddressJson.add(newEmailId);
    JsonObject context = new JsonObject();
    context.put(ParameterConstants.MAIL_TOKEN, resetEmailToken);
    context.put(ParameterConstants.OLD_EMAIL_ID, oldEmailId);
    context.put(ParameterConstants.NEW_EMAIL_ID, newEmailId);
    context.put(ParameterConstants.MAIL_TOKEN, resetEmailToken);
    data.put(ParameterConstants.MAIL_TEMPLATE_CONTEXT, context);
    data.put(ParameterConstants.MAIL_TO_ADDRESSES, toAddressJson);
    Map<String, String> headers = new HashMap<>();
    headers.put(HelperConstants.HEADER_AUTHORIZATION, (HelperConstants.HEADER_TOKEN + resetEmailToken));
    InternalHelper.executeHTTPClientPost(configRegistry.getMailRestApiUrl(), data.toString(), headers);
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
