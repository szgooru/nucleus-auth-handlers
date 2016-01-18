package org.gooru.auth.handlers.processors.service;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;

final public class AJResponseJsonTransformer {

  public static JsonObject transform(String ajResult, String[] jsonFields, boolean removeNullFields) {
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }

    for (String fieldName : jsonFields) {
      String valueToXform = result.getString(fieldName);
      if (valueToXform != null && !valueToXform.isEmpty()) {
        if (valueToXform.startsWith("{")) {
          JsonObject xformedValue = new JsonObject(valueToXform);
          result.put(fieldName, xformedValue);
        } else if (valueToXform.startsWith("[")) {
          JsonArray xformedValue = new JsonArray(valueToXform);
          result.put(fieldName, xformedValue);
        }
      }
    }
    if (removeNullFields) {
      Iterator<String> fieldNamesIterator = result.fieldNames().iterator();
      while (fieldNamesIterator.hasNext()) {
        String fieldName = fieldNamesIterator.next();
        if (result.getValue(fieldName) == null) {
          fieldNamesIterator.remove();
        }
      }

    }
    return result;
  }
}
