package org.gooru.auth.handlers.processors.command.executor.authentication;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;

public final class FetchAccessTokenExecutor extends Executor {

  private RedisClient redisClient;

  interface Fetch {
    MessageResponse accessToken(String token);
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String token = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);
    return fetch.accessToken(token);
  }

  Fetch fetch = (String token) -> {
    JsonObject accessToken = getRedisClient().getJsonObject(token);
    reject(accessToken == null, MessageCodeConstants.AU0040, 400);
    if (accessToken.containsKey(MessageConstants.MSG_KEY_PREFS)) {
      accessToken.remove(MessageConstants.MSG_KEY_PREFS);
    }
    accessToken.remove(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
    return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusOkay().successful().build();
  };

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }
}
