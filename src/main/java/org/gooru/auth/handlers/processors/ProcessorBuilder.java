package org.gooru.auth.handlers.processors;

import io.vertx.core.eventbus.Message;

public class ProcessorBuilder {
  private Message<Object> message;

  private Class<?> handlerClass;
  
  public ProcessorBuilder(Class<?> handlerClass, Message<Object> message) {
    this.message = message;
    this.handlerClass = handlerClass;
  }

  public Processor build() {
    return new MessageProcessor(CommandFactory.getInstance(handlerClass), message);
  }
}
