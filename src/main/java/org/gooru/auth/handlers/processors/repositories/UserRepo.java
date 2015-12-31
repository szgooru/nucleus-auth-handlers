package org.gooru.auth.handlers.processors.repositories;

import io.vertx.core.json.JsonObject;

public interface UserRepo  {
  JsonObject getUserByName();
}
