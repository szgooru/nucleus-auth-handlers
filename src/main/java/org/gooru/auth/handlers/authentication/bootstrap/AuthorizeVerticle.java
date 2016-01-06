package org.gooru.auth.handlers.authentication.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.constants.MessageConstants;
import org.gooru.auth.handlers.authentication.constants.MessagebusEndpoints;
import org.gooru.auth.handlers.authentication.processors.AuthorizeCommandExecutor;
import org.gooru.auth.handlers.authentication.processors.ProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizeVerticle extends AbstractVerticle {
  static final Logger LOG = LoggerFactory.getLogger(AuthorizeVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {
    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_AUTHORIZE, message -> {
      LOG.debug("Received message: " + message.body());
        vertx.executeBlocking(future -> {
          JsonObject result = new ProcessorBuilder(AuthorizeCommandExecutor.class, message).build().process();
          future.complete(result);
        }, res -> {
          LOG.debug("Worker thread done. Taking processing forward.");
          JsonObject result = (JsonObject) res.result();
          LOG.debug("Got result object, will reply to message");
          DeliveryOptions options = new DeliveryOptions().addHeader(MessageConstants.MSG_OP_STATUS, result.getString(MessageConstants.MSG_OP_STATUS));
          message.reply(result.getJsonObject(MessageConstants.RESP_CONTAINER_MBUS), options);
          LOG.debug("Sent reply to message. Will process event data now.");
          JsonObject eventData = result.getJsonObject(MessageConstants.RESP_CONTAINER_EVENT);
          if (eventData != null) {
            eb.publish(MessagebusEndpoints.MBEP_EVENT, eventData);
          }
        });

    }).completionHandler(result -> {
      if (result.succeeded()) {
        LOG.info("Authorize end point ready to listen");
      } else {
        LOG.error("Error registering the authorize handler. Halting the authorize machinery");
        Runtime.getRuntime().halt(1);
      }
    });
  }


}
