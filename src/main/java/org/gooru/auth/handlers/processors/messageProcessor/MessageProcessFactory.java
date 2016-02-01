package org.gooru.auth.handlers.processors.messageProcessor;

import java.util.HashMap;
import java.util.Map;

public class MessageProcessFactory {

  private static final Map<Class<?>, MessageProcessorHandler> instances = new HashMap<>();

  public static MessageProcessorHandler getInstance(Class<?> classz) {
    MessageProcessorHandler cmd = instances.get(classz);
    if (cmd == null) {
      synchronized (MessageProcessFactory.class) {
        if (classz.equals(AuthenticationMessageProcessor.class)) {
          cmd = new AuthenticationMessageProcessor();
        } else if (classz.equals(AuthorizeMessageProcessor.class)) {
          cmd = new AuthorizeMessageProcessor();
        } else if (classz.equals(UserMessageProcessor.class)) {
          cmd = new UserMessageProcessor();
        } else if (classz.equals(UserPrefsMessageProcessor.class)) {
          cmd = new UserPrefsMessageProcessor();
        } else if (classz.equals(AuthenticationGLAVersionMessageProcessor.class)) {
          cmd = new AuthenticationGLAVersionMessageProcessor();
        }
      }
      instances.put(classz, cmd);
    }

    return cmd;

  }
}
