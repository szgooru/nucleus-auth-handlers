package org.gooru.auth.handlers.processors.command.executor.authentication;

import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;

public final class DeleteAccessTokenExecutor extends Executor {

  private RedisClient redisClient;

  public DeleteAccessTokenExecutor() {
    setRedisClient(RedisClient.instance());
  }

  interface Delete {
    MessageResponse accessToken(String token);;
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String token = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);
    return delete.accessToken(token);
  }

  Delete delete = (String token) -> {
    getRedisClient().del(token);
    return new MessageResponse.Builder().setContentTypeJson().setStatusNoOutput().successful().build();
  };

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }
}
