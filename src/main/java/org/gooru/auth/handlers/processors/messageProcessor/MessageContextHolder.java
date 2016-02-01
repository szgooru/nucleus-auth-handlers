package org.gooru.auth.handlers.processors.messageProcessor;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessageConstants;

public class MessageContextHolder implements MessageContext {

  private final Message<Object> message;

  private final JsonObject data;

  public MessageContextHolder(Message<Object> message) {
    this.message = message;
    this.data = (JsonObject) message.body();
  }

  @Override
  public JsonObject requestBody() {
    return data.getJsonObject(MessageConstants.MSG_HTTP_BODY);
  }

  @Override
  public JsonObject requestParams() {
    return data.getJsonObject(MessageConstants.MSG_HTTP_PARAM);
  }

  @Override
  public MultiMap headers() {
    return message.headers();
  }

  @Override
  public String command() {
    return headers().get(MessageConstants.MSG_HEADER_OP);
  }

  @Override
  public UserContext user() {
    UserContext userContext = null;
    JsonObject userContextAsJson = data.getJsonObject(MessageConstants.MSG_USER_CONTEXT_HOLDER);
    if (userContextAsJson != null) {
      userContext = new UserContext(userContextAsJson.getMap());
    }
    return userContext;
  }

}
