package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.postgresql.util.PGobject;

final public class AJResponseJsonTransformer {

  public static JsonObject transform(String ajResult, String[] jsonFields, boolean removeNullFields) {
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }
    if (jsonFields != null) {
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

  public static JsonObject transform(String ajResult, boolean removeNullFields) {
    return transform(ajResult, null, removeNullFields);
  }

  public static JsonObject transform(String ajResult) {
    return transform(ajResult, null, true);
  }

  public static JsonObject transform(String ajResult, String[] jsonFields) {
    return transform(ajResult, jsonFields, true);
  }

  public static JsonObject transform(Map<String, Object> ajResult, String[] jsonFields, boolean removeNullFields) {
    JsonObject result = new JsonObject();
    if (ajResult == null) {
      return result;
    }
    Iterator<String> fieldNamesIterator = ajResult.keySet().iterator();
    while (fieldNamesIterator.hasNext()) {
      String fieldName = fieldNamesIterator.next();
      Object value = ajResult.get(fieldName);
      if (removeNullFields) {
        if (value == null) {
          fieldNamesIterator.remove();
        } else {

          result.put(fieldName, getValue(value));
        }
      } else {
        result.put(fieldName, getValue(value));
      }
    }
    return result;
  }

  public static JsonObject transform(Map<String, Object> ajResult, boolean removeNullFields) {
    return transform(ajResult, null, removeNullFields);
  }

  public static JsonObject transform(Map<String, Object> ajResult) {
    return transform(ajResult, null, true);
  }

  private static Object getValue(Object value) {
    if (value instanceof PGobject) {
      String data = ((PGobject) value).getValue();
      if (data.startsWith("{")) {
        value = new JsonObject(data);
      } else if (data.startsWith("[")) {
        value = new JsonArray(data);
      }
    } else if (value instanceof Timestamp) {
      value = ((Timestamp) value).getTime();
    } else if (value instanceof UUID) {
      value = value.toString();
    }
    return value;
  }

}
