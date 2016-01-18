package org.gooru.auth.handlers.processors.repositories.activejdbc;

import javax.sql.DataSource;

import org.gooru.auth.handlers.infra.DataSourceRegistry;

public abstract class AJAbstractRepo {
  
  protected DataSource dataSource() {
    return DataSourceRegistry.getInstance().getDefaultDataSource();
  }

}
