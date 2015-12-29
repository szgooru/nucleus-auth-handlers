package org.gooru.auth.handlers.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.bootstrap.shutdown.Finalizer;
import org.gooru.auth.handlers.bootstrap.shutdown.Finalizers;
import org.gooru.auth.handlers.bootstrap.startup.Initializer;
import org.gooru.auth.handlers.bootstrap.startup.Initializers;
import org.gooru.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.auth.handlers.processors.ProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationVerticle extends AbstractVerticle {
  static final Logger LOG = LoggerFactory.getLogger(AuthenticationVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {

    vertx.executeBlocking(blockingFuture -> {
      startApplication();
    }, future -> {
      if (future.succeeded()) {
        voidFuture.complete();
      } else {
        voidFuture.fail("Not able to initialize the Authentication machinery properly");
      }
    });

    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_AUTHENTICATION, message -> {

      LOG.debug("Received message: " + message.body());

      vertx.executeBlocking(future -> {
        JsonObject result = new ProcessorBuilder(message).build().process(MessagebusEndpoints.MBEP_AUTHENTICATION);
        future.complete(result);
      }, res -> {
        JsonObject result = (JsonObject) res.result();
        message.reply(result);
      });

    }).completionHandler(result -> {
      if (result.succeeded()) {
        LOG.info("Authentication end point ready to listen");
      } else {
        LOG.error("Error registering the authentication handler. Halting the Authentication machinery");
        Runtime.getRuntime().halt(1);
      }
    });

  }

  @Override
  public void stop() throws Exception {
    shutDownApplication();
    super.stop();
  }

  private void startApplication() {
    Initializers initializers = new Initializers();
    try {
      for (Initializer initializer : initializers) {
        initializer.initializeComponent(vertx, config());
      }
    } catch (IllegalStateException ie) {
      LOG.error("Error initializing application", ie);
      Runtime.getRuntime().halt(1);
    }
  }

  private void shutDownApplication() {
    Finalizers finalizers = new Finalizers();
    for (Finalizer finalizer : finalizers) {
      finalizer.finalizeComponent();
    }
  }
}
