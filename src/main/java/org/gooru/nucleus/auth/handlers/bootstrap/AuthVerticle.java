package org.gooru.nucleus.auth.handlers.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(AuthVerticle.class);

    @Override
    public void start(Future<Void> voidFuture) throws Exception {
        final EventBus eb = vertx.eventBus();
        eb.consumer(MessagebusEndpoints.MBEP_AUTH, message -> {
            LOG.debug("Received message: " + message.body());
            vertx.executeBlocking(future -> {
                JsonObject result = getAccessToken(message.headers().get(MessageConstants.MSG_HEADER_TOKEN));
                future.complete(result);
            }, res -> {
                if (res.result() != null) {
                    JsonObject result = (JsonObject) res.result();
                    DeliveryOptions options = new DeliveryOptions()
                        .addHeader(MessageConstants.MSG_OP_STATUS, MessageConstants.MSG_OP_STATUS_SUCCESS);
                    message.reply(result, options);
                } else {
                    message.reply(null);
                }
            });

        }).completionHandler(result -> {
            if (result.succeeded()) {
                LOG.info("Auth end point ready to listen");
                voidFuture.complete();
            } else {
                LOG.error("Error registering the auth handler. Halting the Auth machinery");
                voidFuture.fail(result.cause());
            }
        });
    }

    private JsonObject getAccessToken(String token) {
        JsonObject accessToken = RedisClient.instance().getJsonObject(token);
        if (accessToken != null) {
            int expireAtInSeconds = accessToken.getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
            RedisClient.instance().expire(token, expireAtInSeconds);
        }
        return accessToken;
    }
}
