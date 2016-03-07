package org.gooru.nucleus.auth.handlers.bootstrap;

import java.util.stream.Stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.processors.ProcessorBuilder;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.ProcessorHandlerType;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(UserVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {
    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_USER, message -> {
      LOG.debug("Received message: " + message.body());
      vertx.executeBlocking(future -> {
        MessageResponse result = new ProcessorBuilder(ProcessorHandlerType.USER, message).build().process();
        future.complete(result);
      }, res -> {
        MessageResponse result = (MessageResponse) res.result();
        message.reply(result.reply(), result.deliveryOptions());

        JsonObject eventData = result.event();
        if (eventData != null) {
          eb.publish(MessagebusEndpoints.MBEP_EVENT, eventData);
        }
        if (result.mailNotify() != null && result.mailNotify().size() > 0) { 
          JsonArray mailNotifies = result.mailNotify();
          Stream<JsonObject> stream = mailNotifies.stream().map(mailNotify -> (JsonObject) mailNotify);
          stream.forEach((JsonObject mailNotify) -> {             
            InternalHelper.executeHTTPClientPost(ConfigRegistry.instance().getMailRestApiUrl(), mailNotify.toString(), mailNotify.getString(HelperConstants.HEADER_AUTHORIZATION));
          });
        }
      });

    }).completionHandler(result -> {
      if (result.succeeded()) {
        LOG.info("User end point ready to listen");
      } else {
        LOG.error("Error registering the user handler. Halting the user machinery");
        Runtime.getRuntime().halt(1);
      }
    });
  }


}
