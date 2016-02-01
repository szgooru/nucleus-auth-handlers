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

public class UpdateUserEmailExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;

  private static final int EXPIRE_IN_SECONDS = 86400;

  public UpdateUserEmailExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setRedisClient(RedisClient.instance());
    setUserRepo(UserRepo.instance());
  }

  interface Update {
    MessageResponse userEmail(String userId, String newEmailId);
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String newEmailId = null;
    if (messageContext.requestBody() != null) {
      newEmailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    }
    return update.userEmail(messageContext.user().getUserId(), newEmailId);
  }

  private final Update update = (String userId, String emailId) -> {
    rejectIfNull(emailId, MessageCodeConstants.AU0014, HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_EMAIL_ID);
    AJEntityUserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    reject(userIdentityEmail != null, MessageCodeConstants.AU0023, HttpConstants.HttpStatus.BAD_REQUEST.getCode(), emailId,
            ParameterConstants.EMAIL_ADDRESS);
    final String token = InternalHelper.generateToken(InternalHelper.EMAIL_CONFIRM_TOKEN);
    JsonObject tokenData = new JsonObject();
    tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, emailId);
    tokenData.put(ParameterConstants.PARAM_USER_ID, userId);
    getRedisClient().set(token, tokenData.toString(), EXPIRE_IN_SECONDS);
    AJEntityUser user = getUserRepo().getUser(userId);
    AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_EMAIL.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC, AJResponseJsonTransformer.transform(user.toJson(false)));
    eventBuilder.putPayLoadObject(ParameterConstants.PARAM_USER_NEW_EMAIL_ID, emailId).putPayLoadObject(ParameterConstants.PARAM_TOKEN, token);
    return new MessageResponse.Builder().setContentTypeJson().setEventData(eventBuilder.build()).setStatusOkay().successful().build();
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
