package org.gooru.auth.handlers.processors;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {

  private static Map<Class, Command> instances = new HashMap<>();

  public static synchronized Command getInstance(Class classz) {
    Command cmd = instances.get(classz);
    if (cmd == null) {
      if (classz.equals(AuthenticatonCommandHandler.class)) {
        cmd = new AuthenticatonCommandHandler();
      }
      instances.put(classz, cmd);
    }
    return cmd;

  }

}
