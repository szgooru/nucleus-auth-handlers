package org.gooru.auth.handlers.authentication.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

public interface CommandExecutor {
  JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body);
  
  
}
