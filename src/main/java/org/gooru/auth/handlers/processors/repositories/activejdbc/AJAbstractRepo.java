package org.gooru.auth.handlers.processors.repositories.activejdbc;

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

  public <T extends Model> Model save(Model model) {
    try {
      Base.open(dataSource());
      model.toInsert();
      model.insert();
      Base.commitTransaction();
    } catch (Throwable e) {
      LOG.error("Exception while marking connetion to be write", e);
      Base.rollbackTransaction();
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return model;
  }

  public <T extends Model> Model saveOrUpdate(Model model) {
    try {
      Base.open(dataSource());
      model.saveIt();
      Base.commitTransaction();
    } catch (Throwable e) {
      LOG.error("Exception while marking connetion to be write", e);
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
      LOG.error("Exception while marking connetion to be write", e);
      Base.rollbackTransaction();
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return model;
  }
}
