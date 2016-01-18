package org.gooru.auth.handlers.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

public interface MessageContext {

  JsonObject requestBody();
  
  JsonObject requestParams();
  
  MultiMap headers();
  
  String command();
  
  UserContext user();
  
}
