package org.gooru.nucleus.auth.handlers.processors.command.executor.authentication;

import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class DeleteAccessTokenExecutor implements DBExecutor {

    private RedisClient redisClient;
    private MessageContext messageContext;
    private String token;

    public DeleteAccessTokenExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
        this.redisClient = RedisClient.instance();
    }

    @Override
    public void checkSanity() {
        token = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);
    }

    @Override
    public void validateRequest() {
    }

    @Override
    public MessageResponse executeRequest() {
        this.redisClient.del(token);
        return new MessageResponse.Builder().setContentTypeJson().setStatusNoOutput().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }
}
