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
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.*;

public final class ResetUnAuthenticateUserPasswordExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  public ResetUnAuthenticateUserPasswordExecutor() {

    setUserIdentityRepo(UserIdentityRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String token = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_TOKEN);
    final String newPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
    return resetUnAuthenticateUserPassword(token, newPassword);
  }

  private MessageResponse resetUnAuthenticateUserPassword(String token, String password) {
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
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
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
}
