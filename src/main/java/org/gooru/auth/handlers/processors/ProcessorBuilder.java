package org.gooru.auth.handlers.processors;

import io.vertx.core.eventbus.Message;

import org.gooru.auth.handlers.processors.messageProcessor.MessageProcessFactory;
import org.gooru.auth.handlers.processors.messageProcessor.MessageProcessor;
import org.gooru.auth.handlers.processors.messageProcessor.Processor;
import org.gooru.auth.handlers.processors.messageProcessor.ProcessorHandlerType;

public class ProcessorBuilder {
  private final Message<Object> message;

  private final ProcessorHandlerType handlerType;

  public ProcessorBuilder(ProcessorHandlerType handlerType, Message<Object> message) {
    this.message = message;
    this.handlerType = handlerType;
  }

  public Processor build() {
    return new MessageProcessor(MessageProcessFactory.getInstance(handlerType), message);
  }
}
