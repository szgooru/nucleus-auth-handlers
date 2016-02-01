package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gooru.auth.handlers.infra.DataSourceRegistry;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AJAbstractRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJAbstractRepo.class);

  protected DataSource dataSource() {
    return DataSourceRegistry.getInstance().getDefaultDataSource();
  }

  public <T extends Model> Model saveOrUpdate(Model model) {
    try {
      Base.open(dataSource());
      model.saveIt();
      Base.commitTransaction();
    } catch (Throwable e) {
      LOG.error("Exception while marking connection to be write", e);
      Base.rollbackTransaction();
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return model;
  }

  public <T extends Model> Model update(Model model) {
    try {
      Base.open(dataSource());
      model.toUpdate();
      Base.commitTransaction();
    } catch (Throwable e) {
      LOG.error("Exception while marking connection to be write", e);
      Base.rollbackTransaction();
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return model;
  }

  @SuppressWarnings("rawtypes")
  public Map find(final String sql, final Object... params) {
    Map result = null;
    try {
      Base.open(dataSource());
      List<Map> results = Base.findAll(sql, params);
      result = results.size() > 0 ? results.get(0) : null;
    } catch (Throwable e) {
      LOG.error("Exception while marking connection to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return result;
  }
}
