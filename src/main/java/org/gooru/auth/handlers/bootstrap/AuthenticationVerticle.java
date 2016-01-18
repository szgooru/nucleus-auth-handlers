package org.gooru.auth.handlers.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.auth.handlers.processors.ProcessorBuilder;
import org.gooru.auth.handlers.processors.command.executor.AuthenticatonCommandExecutor;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationVerticle extends AbstractVerticle {
  static final Logger LOG = LoggerFactory.getLogger(AuthenticationVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {
    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_AUTHENTICATION, message -> {
      LOG.debug("Received message: " + message.body());
      vertx.executeBlocking(future -> {
        MessageResponse result = new ProcessorBuilder(AuthenticatonCommandExecutor.class, message).build().process();
        future.complete(result);
      }, res -> {
        MessageResponse result = (MessageResponse) res.result();
        message.reply(result.reply(), result.deliveryOptions());

        JsonObject eventData = result.event();
        if (eventData != null) {
          eb.publish(MessagebusEndpoints.MBEP_EVENT, eventData);
        }

      });;

    }).completionHandler(result -> {
      if (result.succeeded()) {
        LOG.info("authentication end point ready to listen");
      } else {
        LOG.error("Error registering the authentication handler. Halting the authentication machinery");
        Runtime.getRuntime().halt(1);
      }
    });
  }


}
