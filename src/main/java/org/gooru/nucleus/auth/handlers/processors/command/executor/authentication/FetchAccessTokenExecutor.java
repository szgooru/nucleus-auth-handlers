package org.gooru.nucleus.auth.handlers.processors.command.executor.authentication;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;

public final class FetchAccessTokenExecutor extends Executor {

  private RedisClient redisClient;
  private UserRepo userRepo;

  public FetchAccessTokenExecutor() {
    this.redisClient = RedisClient.instance();
    this.userRepo = UserRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String token = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);
    return fetchAccessToken(token);
  }

  private MessageResponse fetchAccessToken(String token) {
    JsonObject accessToken = getRedisClient().getJsonObject(token);
    ServerValidatorUtility.reject(accessToken == null, MessageCodeConstants.AU0040, 400);
    if (accessToken.containsKey(MessageConstants.MSG_KEY_PREFS)) {
      accessToken.remove(MessageConstants.MSG_KEY_PREFS);
    }
    accessToken.remove(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
    final String userId = accessToken.getString(ParameterConstants.PARAM_USER_ID);
    if (!userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      AJEntityUser user = getUserRepo().getUser(userId);
      if (user.getFirstname() != null) {
        accessToken.put(ParameterConstants.PARAM_USER_FIRSTNAME, user.getFirstname());
      }
      if (user.getLastname() != null) {
        accessToken.put(ParameterConstants.PARAM_USER_LASTNAME, user.getLastname());
      }
      if (user.getUserCategory() != null) {
        accessToken.put(ParameterConstants.PARAM_USER_CATEGORY, user.getUserCategory());
      }
    }

    return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusOkay().successful().build();
  };

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }
}
