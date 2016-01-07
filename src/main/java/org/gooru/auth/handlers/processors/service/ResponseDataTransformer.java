package org.gooru.auth.handlers.processors.service;

import io.vertx.core.json.JsonObject;

import java.util.Map;

public class ResponseDataTransformer {

  public static JsonObject JsonObject(Map<String, Object> data) {
    return transform(data, null);
  }

  public static JsonObject transform(Map<String, Object> data, String[] excludes) {
    JsonObject result = null;
    if (data != null && excludes != null) {
      for (String exclude : excludes) {
        data.remove(exclude);
      }
    }
    if (data != null) {
      result = new JsonObject(data);
    }
    return result;
  }

}
