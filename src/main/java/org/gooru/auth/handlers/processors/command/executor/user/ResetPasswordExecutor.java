package org.gooru.auth.handlers.processors.command.executor.user;

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
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.utils.InternalHelper;

public final class ResetPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  private static final int EXPIRE_IN_SECONDS = 86400;

  interface Reset {
    MessageResponse password(String emailId);
  }

  public ResetPasswordExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String emailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    return reset.password(emailId);
  }

  Reset reset = (emailId) -> {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final String token = InternalHelper.generateToken(InternalHelper.RESET_PASSWORD_TOKEN);
    getRedisClient().set(token, userIdentity.getEmailId(), EXPIRE_IN_SECONDS);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.RESET_USER_PASSWORD.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    eventBuilder.putPayLoadObject(ParameterConstants.PARAM_TOKEN, token);
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusOkay().successful().build();
  };

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
}
