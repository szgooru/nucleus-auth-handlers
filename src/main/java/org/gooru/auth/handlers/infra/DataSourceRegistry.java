package org.gooru.auth.handlers.infra;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gooru.auth.handlers.bootstrap.shutdown.Finalizer;
import org.gooru.auth.handlers.bootstrap.startup.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public class DataSourceRegistry implements Initializer, Finalizer {

  private static final String DEFAULT_DATA_SOURCE = "defaultDataSource";
  private static final String DEFAULT_DATA_SOURCE_TYPE = "auth.ds.type";
  private static final String DS_HIKARI = "hikari";
  private static final Logger LOG = LoggerFactory.getLogger(DataSourceRegistry.class);
  // All the elements in this array are supposed to be present in config file
  // as keys as we are going to initialize them with the value associated with
  // that key
  private List<String> datasources = Arrays.asList(DEFAULT_DATA_SOURCE);
  private Map<String, DataSource> registry = new HashMap<>();
  boolean initialized = false;
  
  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    // Skip if we are already initialized
    LOG.debug("Initialization called upon.");
    if (!initialized) {
      LOG.debug("May have to do initialization");
      // We need to do initialization, however, we are running it via verticle instance which is going to run in 
      // multiple threads hence we need to be safe for this operation
      synchronized (Holder.INSTANCE) {
        LOG.debug("Will initialize after double checking");
        if (!initialized) {
          LOG.debug("Initializing now");
          for (String datasource : datasources) {
            JsonObject dbConfig = config.getJsonObject(datasource);
            if (dbConfig != null) {        
              DataSource ds = initializeDataSource(dbConfig);
              registry.put(datasource, ds);
            }
          }
          initialized = true;
        }
      }
    }
  }
  
  public DataSource getDefaultDataSource() {
    return registry.get(DEFAULT_DATA_SOURCE);
  }
  
  public DataSource getDataSourceByName(String name) {
    if (name != null) {
      return registry.get(name);
    }
    return null;
  }

  private DataSource initializeDataSource(JsonObject dbConfig)  {
    // The default DS provider is hikari, so if set explicitly or not set use it, else error out
    String dsType = dbConfig.getString(DEFAULT_DATA_SOURCE_TYPE);
    DataSource dataSource = null; 
    if (dsType != null && !dsType.equals(DS_HIKARI)) {
      // No support
      throw new IllegalStateException("Unsupported data store type");
    } else { 
      try {
        dataSource = new HikariCPDataSourceProvider().getDataSource(dbConfig);
      } catch (SQLException e) {
        throw new IllegalStateException("Failed to create data source");
      }
    }
    return dataSource;
  }

  @Override
  public void finalizeComponent() {
    for (String datasource : datasources) {
      DataSource ds = registry.get(datasource);
      if (ds != null) {        
        if (ds instanceof HikariDataSource) {
          ((HikariDataSource) ds).close();
        }
      }
    }     
  }
  
  public static DataSourceRegistry getInstance() {
    return Holder.INSTANCE;
  }

  private DataSourceRegistry() {
    // TODO Auto-generated constructor stub
  }
  
  private static class Holder {
    private static DataSourceRegistry INSTANCE = new DataSourceRegistry();
  }

}
