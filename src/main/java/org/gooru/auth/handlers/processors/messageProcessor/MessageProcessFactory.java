package org.gooru.auth.handlers.processors.messageProcessor;

import java.util.HashMap;
import java.util.Map;

public class MessageProcessFactory {

  private static Map<Class<?>, MessageProcessorHandler> instances = new HashMap<>();

  public static MessageProcessorHandler getInstance(Class<?> classz) {
    MessageProcessorHandler cmd = instances.get(classz);
    if (cmd == null) {
      synchronized (MessageProcessFactory.class) {
        if (classz.equals(AuthenticatonMessageProcessor.class)) {
          cmd = new AuthenticatonMessageProcessor();
        } else if (classz.equals(AuthorizeMessageProcessor.class)) {
          cmd = new AuthorizeMessageProcessor();
        } else if (classz.equals(UserMessageProcessor.class)) {
          cmd = new UserMessageProcessor();
        } else if (classz.equals(UserPrefsMessageProcessor.class)) {
          cmd = new UserPrefsMessageProcessor();
        } else if (classz.equals(AuthenticatonGLAVersionMessageProcessor.class)) {
          cmd = new AuthenticatonGLAVersionMessageProcessor();
        }
      }
      instances.put(classz, cmd);
    }

    return cmd;

  }
}
