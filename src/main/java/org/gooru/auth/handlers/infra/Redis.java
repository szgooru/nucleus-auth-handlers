package org.gooru.auth.handlers.infra;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import org.gooru.auth.handlers.bootstrap.startup.Initializer;

public final class Redis implements Initializer {

  public static RedisClient client;

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    if (client == null) {
      client = RedisClient.create(vertx, new RedisOptions(config));
    }
  }

  public static RedisClient client() {
    return client;
  }

  public static  Redis  getInstance() {
    return new Redis();
  }

}
