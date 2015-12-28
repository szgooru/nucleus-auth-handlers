package org.gooru.auth.handlers.message;

import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.MessagebusEndpoints;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

public class MessageHandler implements Message {

  @Override
  public void send(String message, MultiMap headers, JsonObject params,  JsonObject body, Handler<AsyncResult<JsonObject>> reply) {
    String command = headers.get(MessageConstants.MSG_HEADER_OP);
    switch (message) {
    case MessagebusEndpoints.MBEP_AUTHENTICATION:
      AuthenticatonCommandHandler.getInstance().send(command, headers, params,  body, reply);
      break;
    }

  }
}
