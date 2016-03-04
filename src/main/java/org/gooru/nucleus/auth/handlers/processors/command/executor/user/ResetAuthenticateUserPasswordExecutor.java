package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

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
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

public final class ResetAuthenticateUserPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private final ConfigRegistry configRegistry = ConfigRegistry.instance();

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
    sendPasswordChangedEmailNotify(accessToken, token, userIdentity.getEmailId());
    return new MessageResponse.Builder().setResponseBody(null).setContentTypeJson().setStatusNoOutput().successful().build();
  }

  private void sendPasswordChangedEmailNotify(String accessToken, String resetPasswordToken, String toAddress) {
    JsonObject data = new JsonObject();
    data.put(ParameterConstants.MAIL_TEMPLATE_NAME, MailTemplateConstants.PASSWORD_CHANGED);
    JsonArray toAddressJson = new JsonArray();
    toAddressJson.add(toAddress);
    JsonObject context = new JsonObject();
    context.put(ParameterConstants.MAIL_TOKEN, resetPasswordToken);
    data.put(ParameterConstants.MAIL_TEMPLATE_CONTEXT, context);
    data.put(ParameterConstants.MAIL_TO_ADDRESSES, toAddressJson);
    Map<String, String> headers = new HashMap<>();
    headers.put(HelperConstants.HEADER_AUTHORIZATION, (HelperConstants.HEADER_TOKEN + accessToken));
    InternalHelper.executeHTTPClientPost(configRegistry.getMailRestApiUrl(), data.toString(), headers);
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

}
