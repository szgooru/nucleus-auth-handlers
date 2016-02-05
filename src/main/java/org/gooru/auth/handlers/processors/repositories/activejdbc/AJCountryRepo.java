package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJCountryRepo extends AJAbstractRepo implements CountryRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJCountryRepo.class);

  private static final String GET_COUNTRY_BY_NAME = "name = ?";

  private static final String GET_COUNTRY_BY_ID = "id = ?::uuid";

  @Override
  public AJEntityCountry getCountry(String id) {
    return query(GET_COUNTRY_BY_ID, id);
  }

  @Override
  public AJEntityCountry getCountryByName(String name) {
    return query(GET_COUNTRY_BY_NAME, name);
  }

  @Override
  public AJEntityCountry createCountry(AJEntityCountry country) {
    return (AJEntityCountry) saveOrUpdate(country);
  }

  @Override
  public AJEntityCountry createCountry(String name, String creatorId) {
    AJEntityCountry country = new AJEntityCountry();
    country.setName(name);
    country.setCode(UUID.randomUUID().toString());
    country.setCreatorId(UUID.fromString(creatorId));
    return createCountry(country);
  }

  private AJEntityCountry query(String whereClause, Object... params) {
    AJEntityCountry country = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityCountry> results = AJEntityCountry.where(whereClause, params);
      country = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connection to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return country;
  }
}
