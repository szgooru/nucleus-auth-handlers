package org.gooru.auth.handlers.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.auth.handlers.processors.ProcessorBuilder;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.messageProcessor.UserPrefsMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPrefsVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(UserPrefsVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {
    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_USER_PREFS, message -> {
      LOG.debug("Received message: " + message.body());
      vertx.executeBlocking(future -> {
        MessageResponse result = new ProcessorBuilder(UserPrefsMessageProcessor.class, message).build().process();
        future.complete(result);
      }, res -> {
        MessageResponse result = (MessageResponse) res.result();
        message.reply(result.reply(), result.deliveryOptions());

        JsonObject eventData = result.event();
        if (eventData != null) {
          eb.publish(MessagebusEndpoints.MBEP_EVENT, eventData);
        }
      });
    }).completionHandler(result -> {
      if (result.succeeded()) {
        LOG.info("User prefs end point ready to listen");
      } else {
        LOG.error("Error registering the user prefs handler. Halting the user prefs machinery");
        Runtime.getRuntime().halt(1);
      }
    });
  }


}
