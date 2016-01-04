package org.gooru.auth.handlers.authentication.processors.transformers;

import io.vertx.core.json.JsonObject;

public interface ResponseTransformer {
  JsonObject transform();
}
