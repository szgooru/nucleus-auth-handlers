package org.gooru.auth.handlers.processors;

import rx.Observable;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

public interface Command {
  Observable<JsonObject> exec(String command, MultiMap headers, JsonObject params, JsonObject body);
  
}
