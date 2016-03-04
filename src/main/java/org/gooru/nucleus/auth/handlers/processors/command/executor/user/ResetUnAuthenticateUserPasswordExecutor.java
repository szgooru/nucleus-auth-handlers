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
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.*;

public final class ResetUnAuthenticateUserPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  private final ConfigRegistry configRegistry = ConfigRegistry.instance();

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
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_PASSWORD.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    sendPasswordChangedEmailNotify(accessToken, token, userIdentity.getEmailId());
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
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
