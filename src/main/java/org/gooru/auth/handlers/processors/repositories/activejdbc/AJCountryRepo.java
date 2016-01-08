package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.Country;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJCountryRepo extends AJAbstractRepo implements CountryRepo {

  private static final String GET_COUNTRY_BY_NAME = "name = ?";

  private static final String GET_COUNTRY_BY_ID = "id = ?";

  @Override
  public Country getCountry(String id) {
    return query(GET_COUNTRY_BY_NAME, id);
  }

  @Override
  public Country getCountryByName(String name) {
    return query(GET_COUNTRY_BY_ID, name);
  }

  @Override
  public Country createCountry(Country country) {
    Base.open(dataSource());
    country.saveIt();
    Base.commitTransaction();
    Base.close();
    return country;
  }

  private Country query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<Country> results = Country.where(whereClause, params);
    Country country = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return country;
  }
}
