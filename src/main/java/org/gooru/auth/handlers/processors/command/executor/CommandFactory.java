package org.gooru.auth.handlers.processors.command.executor;

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
        } else if (classz.equals(AuthorizeCommandExecutor.class)) {
          cmd = new AuthorizeCommandExecutor();
        } else if (classz.equals(UserCommandExecutor.class)) {
          cmd = new UserCommandExecutor();
        } else if (classz.equals(UserPrefsCommandExecutor.class)) {
          cmd = new UserPrefsCommandExecutor();
        } 
      }
      instances.put(classz, cmd);
    }

    return cmd;

  }

}
