package org.gooru.auth.handlers.processors;

import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class MessageProcessor implements Processor {

  private static final Logger LOG = LoggerFactory.getLogger(Processor.class);
  private final static String REQUEST_BODY = "request.body";
  private final static String REQUEST_PARAMS = "request.params";

  private Message<Object> message;
  
  private Command handler;

  public MessageProcessor(Command handler, Message<Object> message) {
    this.message = message;
    this.handler = handler;
  }

  @Override
  public JsonObject process() {
    JsonObject result = null;
    try {
      if (message == null || !(message.body() instanceof JsonObject)) {
        LOG.error("Invalid message received, either null or body of message is not JsonObject ");
        throw new InvalidRequestException();
      }
      JsonObject data = (JsonObject) message.body();
      MultiMap headers = message.headers();
      String command = headers.get(MessageConstants.MSG_HEADER_OP);
      result = handler.exec(command, headers, params(data), requestBody(data));
    } catch (InvalidRequestException e) {

    }
    return result;
  }

  private JsonObject params(JsonObject message) {
    return message.getJsonObject(REQUEST_PARAMS);
  }

  private JsonObject requestBody(JsonObject message) {
    return message.getJsonObject(REQUEST_BODY);
  }

}
