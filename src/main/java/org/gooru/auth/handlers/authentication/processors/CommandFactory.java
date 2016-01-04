package org.gooru.auth.handlers.authentication.processors;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {

  private static Map<Class<?>, CommandExecutor> instances = new HashMap<>();

  public static CommandExecutor getInstance(Class<?> classz) {
    CommandExecutor cmd = instances.get(classz);
    if (cmd == null) {
      synchronized (CommandFactory.class) {
        if (classz.equals(AuthenticatonCommandExecutor.class)) {
          cmd = new AuthenticatonCommandExecutor();
        }
      }
      instances.put(classz, cmd);
    }
    return cmd;

  }

}
