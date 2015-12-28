package org.gooru.auth.handlers.infra.redis;

import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisOptions;
import io.vertx.rxjava.redis.RedisClient;

public final class Redis {

  public static RedisClient client;

  public void create(io.vertx.core.Vertx vertx, JsonObject config) {
    if (client == null) {
      client = RedisClient.create((io.vertx.rxjava.core.Vertx)vertx, new RedisOptions(config));
    }
  }

  public static RedisClient client() {
    return client;
  }
}
