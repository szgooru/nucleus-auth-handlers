package org.gooru.auth.handlers.infra.redis;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public final class Redis {

  public static RedisClient client;

  public void create(Vertx vertx, JsonObject config) {
    if (client == null) {
      client = RedisClient.create(vertx, new RedisOptions(config));
    }
  }

  public static RedisClient client() {
    return client;
  }
}
