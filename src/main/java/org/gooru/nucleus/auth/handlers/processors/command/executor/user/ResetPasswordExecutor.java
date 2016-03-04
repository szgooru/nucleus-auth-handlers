package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

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
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;

public final class ResetPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  private final ConfigRegistry configRegistry = ConfigRegistry.instance();

  public ResetPasswordExecutor() {
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.redisClient = RedisClient.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String emailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    final String accessToken = messageContext.accessToken();
    return resetPassword(emailId, accessToken);
  }

  private MessageResponse resetPassword(final String emailId, final String accessToken) {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    ServerValidatorUtility.rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
        ParameterConstants.PARAM_USER);
    final String token = InternalHelper.generatePasswordResetToken(userIdentity.getUserId());
    getRedisClient().set(token, userIdentity.getEmailId(), HelperConstants.EXPIRE_IN_SECONDS);
    sendResetPasswordEmailNotify(accessToken, token, emailId);
    return new MessageResponse.Builder().setResponseBody(null).setContentTypeJson().setStatusOkay().successful().build();
  }

  private void sendResetPasswordEmailNotify(String accessToken, String resetPasswordToken, String toAddress) {
    JsonObject data = new JsonObject();
    data.put(ParameterConstants.MAIL_TEMPLATE_NAME, MailTemplateConstants.PASSWORD_CHANGE_REQUEST);
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
