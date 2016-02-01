package org.gooru.auth.handlers.processors.command.executor.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HelperConstants;
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

public class ConfirmUserEmailExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;

  public ConfirmUserEmailExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setRedisClient(RedisClient.instance());
    setUserRepo(UserRepo.instance());
  }

  interface Confirm {
    MessageResponse userEmail(String token);
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String token = null;
    if (messageContext.requestBody() != null) {
      token = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_TOKEN);
    }
    return confirm.userEmail(token);
  }

  private Confirm confirm = (String token) -> {
    final String tokenData = getRedisClient().get(token);
    rejectIfNull(tokenData, MessageCodeConstants.AU0028, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    JsonObject tokenJsonData = new JsonObject(tokenData);
    final String userId = tokenJsonData.getString(ParameterConstants.PARAM_USER_ID);
    final String emailId = tokenJsonData.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_EMAIL_CONFIRM.getName());
    if (!userIdentity.getEmailId().equalsIgnoreCase(emailId)) {
      userIdentity.setEmailId(emailId);
      AJEntityUser user = getUserRepo().getUser(userIdentity.getUserId());
      user.setEmailId(emailId);
      getUserRepo().update(user);
      eventBuilder.put(SchemaConstants.USER_DEMOGRAPHIC, AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS));
    }
    userIdentity.setEmailConfirmStatus(true);
    getUserIdentityRepo().createOrUpdate(userIdentity);
    getRedisClient().del(token);
    eventBuilder.put(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
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

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }
}
