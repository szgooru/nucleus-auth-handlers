package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.transformers.ResponseTransformerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProcessor implements Processor {

  private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

  private Message<Object> message;

  private CommandExecutor handler;

  public MessageProcessor(CommandExecutor handler, Message<Object> message) {
    this.message = message;
    this.handler = handler;
  }

  @Override
  public JsonObject process() {
    try {
      if (message == null || !(message.body() instanceof JsonObject)) {
        LOG.error("Invalid message received, either null or body of message is not JsonObject ");
        throw new InvalidRequestException();
      }
      JsonObject data = (JsonObject) message.body();
      MultiMap headers = message.headers();
      String command = headers.get(MessageConstants.MSG_HEADER_OP);
      JsonObject body = data.getJsonObject(MessageConstants.MSG_HTTP_BODY);
      JsonObject params = data.getJsonObject(MessageConstants.MSG_HTTP_PARAM);
      JsonObject usercontext = data.getJsonObject(MessageConstants.MSG_USER_CONTEXT_HOLDER);
      JsonObject result = handler.exec(command, usercontext, headers, params, body);
      return new ResponseTransformerBuilder().build(result).transform();
    } catch (InvalidRequestException e) {
      LOG.warn("Caught Invalid Request exception while processing", e);
      return new ResponseTransformerBuilder().build(e).transform();
    } catch (Throwable throwable) {
      LOG.warn("Caught unexpected exception here", throwable);
      return new ResponseTransformerBuilder().build(throwable).transform();
    }

  }

}
