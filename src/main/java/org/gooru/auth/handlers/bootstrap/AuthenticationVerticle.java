package org.gooru.auth.handlers.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.auth.handlers.infra.redis.Redis;
import org.gooru.auth.handlers.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationVerticle extends AbstractVerticle {
  static final Logger LOG = LoggerFactory.getLogger(AuthenticationVerticle.class);
  private final static String REQUEST_BODY = "request.body";

  private final static String REQUEST_PARAMS = "request.params";

  @Override
  public void start() throws Exception {
    EventBus eb = vertx.eventBus();
    new Redis().create(vertx, config().getJsonObject("redis"));
    Message msg = Message.create();
    eb.consumer(MessagebusEndpoints.MBEP_AUTHENTICATION, message -> {
      JsonObject json = (JsonObject) message.body();
      JsonObject params = json.getJsonObject(REQUEST_PARAMS);
      JsonObject body = json.getJsonObject(REQUEST_BODY);
      msg.send(MessagebusEndpoints.MBEP_AUTHENTICATION, message.headers(), params, body, null);
      // Now send back reply
            message.reply("Authentication request received");
          }).completionHandler(result -> {
      if (result.succeeded()) {
        LOG.info("Authentication end point ready to listen");
      } else {
        LOG.error("Error registering the authentication handler. Halting the Authentication machinery");
        Runtime.getRuntime().halt(1);
      }
    });
  }

}
