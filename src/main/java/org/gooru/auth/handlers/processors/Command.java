package org.gooru.auth.handlers.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

public interface Command {
  JsonObject exec(String command, MultiMap headers, JsonObject params, JsonObject body);
  
}
