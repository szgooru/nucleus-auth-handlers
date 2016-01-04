package org.gooru.auth.handlers.authentication.processors;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process();
}
