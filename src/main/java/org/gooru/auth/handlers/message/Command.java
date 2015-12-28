package org.gooru.auth.handlers.message;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

public interface Command {
  void send(String command, MultiMap headers, JsonObject params, JsonObject body, Handler<AsyncResult<JsonObject>>  reply);
  
}
