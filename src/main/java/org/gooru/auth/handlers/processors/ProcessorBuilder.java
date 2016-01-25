package org.gooru.auth.handlers.processors;

import io.vertx.core.eventbus.Message;

import org.gooru.auth.handlers.processors.messageProcessor.MessageProcessFactory;
import org.gooru.auth.handlers.processors.messageProcessor.MessageProcessor;
import org.gooru.auth.handlers.processors.messageProcessor.Processor;

public class ProcessorBuilder {
  private Message<Object> message;

  private Class<?> handlerClass;
  
  public ProcessorBuilder(Class<?> handlerClass, Message<Object> message) {
    this.message = message;
    this.handlerClass = handlerClass;
  }

  public Processor build() {
    return new MessageProcessor(MessageProcessFactory.getInstance(handlerClass), message);
  }
}
