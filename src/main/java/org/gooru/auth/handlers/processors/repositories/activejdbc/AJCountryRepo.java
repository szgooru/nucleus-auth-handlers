package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJCountryRepo extends AJAbstractRepo implements CountryRepo {

  private static final String GET_COUNTRY_BY_NAME = "name = ?";

  private static final String GET_COUNTRY_BY_ID = "id = ?";

  @Override
  public AJEntityCountry getCountry(Long id) {
    return query(GET_COUNTRY_BY_ID, id);
  }

  @Override
  public AJEntityCountry getCountryByName(String name) {
    return query(GET_COUNTRY_BY_NAME, name);
  }

  @Override
  public AJEntityCountry createCountry(AJEntityCountry country) {
    Base.open(dataSource());
    country.saveIt();
    Base.commitTransaction();
    Base.close();
    return country;
  }

  @Override
  public AJEntityCountry createCountry(String name, String creatorId) {
    AJEntityCountry country = new AJEntityCountry();
    country.setName(name);
    country.setCode(UUID.randomUUID().toString());
    country.setCreatorId(creatorId);
    return createCountry(country);
  }

  private AJEntityCountry query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<AJEntityCountry> results = AJEntityCountry.where(whereClause, params);
    AJEntityCountry country = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return country;
  }
}
