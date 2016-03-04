package org.gooru.nucleus.auth.handlers.processors.command.executor.authentication;

import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class DeleteAccessTokenExecutor extends Executor {

  private RedisClient redisClient;

  public DeleteAccessTokenExecutor() {
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String token = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);
    return deleteAccessToken(token);
  }

  private MessageResponse deleteAccessToken(String token) {
    getRedisClient().del(token);
    return new MessageResponse.Builder().setContentTypeJson().setStatusNoOutput().successful().build();
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }
}
