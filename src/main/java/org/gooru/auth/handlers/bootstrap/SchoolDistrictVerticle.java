package org.gooru.auth.handlers.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.auth.handlers.processors.command.executor.ProcessorBuilder;
import org.gooru.auth.handlers.processors.command.executor.SchoolDistrictCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchoolDistrictVerticle extends AbstractVerticle {
  static final Logger LOG = LoggerFactory.getLogger(SchoolDistrictVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {
    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_SCHOOL_DISTRICT, message -> {
      LOG.debug("Received message: " + message.body());
        vertx.executeBlocking(future -> {
          JsonObject result = new ProcessorBuilder(SchoolDistrictCommandExecutor.class, message).build().process();
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
        LOG.info("School district end point ready to listen");
      } else {
        LOG.error("Error registering the school district handler. Halting the school district machinery");
        Runtime.getRuntime().halt(1);
      }
    });
  }


}
