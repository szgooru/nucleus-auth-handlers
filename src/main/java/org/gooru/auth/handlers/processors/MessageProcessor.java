package org.gooru.auth.handlers.processors;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.processors.command.executor.CommandExecutor;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProcessor implements Processor {

  private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

  private Message<Object> message;

  private CommandExecutor handler;

  public MessageProcessor(CommandExecutor handler, Message<Object> message) {
    this.message = message;
    this.handler = handler;
  }

  @Override
  public MessageResponse process() {
    MessageResponse result = null;
    try {
      if (message == null || !(message.body() instanceof JsonObject)) {
        LOG.error("Invalid message received, either null or body of message is not JsonObject ");
        throw new InvalidRequestException();
      }
      MessageContext messageContext = new MessageContextHolder(message); 
      result = handler.exec(messageContext);
      return result;
    }  catch (Throwable throwable) {
      LOG.warn("Caught unexpected exception here", throwable);
       return new MessageResponse.Builder().setThrowable(throwable).build();
    }

  }

}
