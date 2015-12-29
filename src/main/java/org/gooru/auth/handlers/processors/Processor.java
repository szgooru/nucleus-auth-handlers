package org.gooru.auth.handlers.processors;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process(String name);
}
