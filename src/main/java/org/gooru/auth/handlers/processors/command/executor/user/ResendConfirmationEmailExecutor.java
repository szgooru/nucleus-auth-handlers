package org.gooru.auth.handlers.processors.command.executor.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.utils.InternalHelper;

public class ResendConfirmationEmailExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;

  private static final int EXPIRE_IN_SECONDS = 86400;

  public ResendConfirmationEmailExecutor() {
    setRedisClient(RedisClient.instance());
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserRepo(UserRepo.instance());
  }

  interface Resend {
    MessageResponse confirmationEmail(String userId);
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    return resend.confirmationEmail(messageContext.user().getUserId());
  }

  private Resend resend = (String userId) -> {
    final AJEntityUser user = getUserRepo().getUser(userId);
    rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(user.getEmailId());
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    final String token = InternalHelper.generateToken(InternalHelper.EMAIL_CONFIRM_TOKEN);
    JsonObject tokenData = new JsonObject();
    tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
    tokenData.put(ParameterConstants.PARAM_USER_ID, userId);
    getRedisClient().set(token, tokenData.toString(), EXPIRE_IN_SECONDS);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.RESEND_CONFIRM_EMAIL.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC, AJResponseJsonTransformer.transform(user.toJson(false)));
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    eventBuilder.putPayLoadObject(ParameterConstants.PARAM_USER_EMAIL_ID, user.getEmailId()).putPayLoadObject(ParameterConstants.PARAM_TOKEN, token);
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusOkay().successful().build();
  };

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }
}
