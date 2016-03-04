package org.gooru.nucleus.auth.handlers.processors.event;

import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class EventBuilder extends JsonObject {

  private String eventId;
  private String eventName;
  private JsonObject session;
  private JsonObject user;
  private Long startTime;
  private Long endTime;
  private JsonObject metrics;
  private JsonObject context;
  private JsonObject version;
  private final JsonObject payLoadObject;

  public EventBuilder() {
    this.payLoadObject = new JsonObject();
  }

  public String getEventId() {
    return eventId;
  }

  public EventBuilder setEventId(String eventId) {
    this.eventId = eventId;
    put("eventId", eventId);
    return this;
  }

  public String getEventName() {
    return eventName;
  }

  public EventBuilder setEventName(String eventName) {
    this.eventName = eventName;
    put("eventName", eventName);
    return this;
  }

  public JsonObject getSession() {
    return session;
  }

  public EventBuilder setSession(JsonObject session) {
    this.session = session;
    put("session", session);
    return this;
  }

  public JsonObject getUser() {
    return user;
  }

  public EventBuilder setUser(JsonObject user) {
    this.user = user;
    put("user", user);
    return this;
  }

  public Long getStartTime() {
    return startTime;
  }

  public EventBuilder setStartTime(Long startTime) {
    this.startTime = startTime;
    put("startTime", startTime);
    return this;
  }

  public Long getEndTime() {
    return endTime;
  }

  public EventBuilder setEndTime(Long endTime) {
    this.endTime = endTime;
    put("endTime", endTime);
    return this;
  }

  public JsonObject getMetrics() {
    return metrics;
  }

  public EventBuilder setMetrics(JsonObject metrics) {
    this.metrics = metrics;
    put("metrics", metrics);
    return this;
  }

  public JsonObject getContext() {
    return context;
  }

  public EventBuilder setContext(JsonObject context) {
    this.context = context;
    put("context", context);
    return this;
  }

  public JsonObject getVersion() {
    return version;
  }

  public EventBuilder setVersion(JsonObject version) {
    this.version = version;
    put("version", version);
    return this;
  }

  public EventBuilder putPayLoadObject(String key, JsonObject value) {
    this.payLoadObject.put(key, value);
    return this;
  }

  public EventBuilder putPayLoadObject(String key, String value) {
    this.payLoadObject.put(key, value);
    return this;
  }

  public JsonObject build() {
    intializeDefaultIfNull();
    return this;
  }

  private EventBuilder intializeDefaultIfNull() {
    if (getEventId() == null) {
      setEventId(UUID.randomUUID().toString());
    }
    if (getMetrics() == null) {
      setMetrics(new JsonObject());
    }
    if (getContext() == null) {
      setContext(new JsonObject());
    }
    if (getUser() == null) {
      setUser(new JsonObject());
    }
    if (getVersion() == null) {
      setVersion(new JsonObject().put("logApi", "0.1"));
    }
    if (getSession() == null) {
      setSession(new JsonObject());
    }
    if (getStartTime() == null) {
      setStartTime(System.currentTimeMillis());
    }
    if (getEndTime() == null) {
      setEndTime(System.currentTimeMillis());
    }
    put("payLoadObject", payLoadObject);
    return this;
  }
}
