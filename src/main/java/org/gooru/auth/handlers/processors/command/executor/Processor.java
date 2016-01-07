package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process();
}
