package org.gooru.auth.handlers.authentication.infra;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.bootstrap.shutdown.Finalizer;
import org.gooru.auth.handlers.authentication.bootstrap.startup.Initializer;
import org.gooru.auth.handlers.authentication.constants.ConfigConstants;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class RedisClient implements Initializer, Finalizer {

  private JedisPool pool = null;

  private static RedisClient redisClient = null;
  

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    JsonObject redisConfig = config.getJsonObject(ConfigConstants.REDIS);
    pool = new JedisPool(redisConfig.getString(ConfigConstants.HOST), redisConfig.getInteger(ConfigConstants.PORT));
  }

  public static RedisClient getInstance() {
    if (redisClient == null) {
      synchronized (RedisClient.class) {
        redisClient = new RedisClient();
      }
    }
    return redisClient;
  }
  
  public Jedis getJedis() { 
    return pool.getResource();  
  }
  
  @Override
  public void finalizeComponent() {
    if (pool != null) {
      pool.destroy();
    }
  }
}
