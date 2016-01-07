package org.gooru.auth.handlers.processors.repositories.activejdbc;

import io.vertx.core.json.JsonArray;

import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.Country;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJCountryRepo extends AJAbstractRepo implements CountryRepo {

  private static final String LIST_COUNTRIES = "creator_id is null and name like  ?";

  @Override
  public Country createCountry(Country country) {
    Base.open(dataSource());
    country.saveIt();
    Base.commitTransaction();
    Base.close();
    return country;
  }

  @Override
  public JsonArray getCountries(String name, long offset, long limit) {
    return queryList(LIST_COUNTRIES, offset, limit, beginsWithPattern(name));
  }

  private JsonArray queryList(String sql, long offset, long limit, Object... params) {
    JsonArray result = null;
    Base.open(dataSource());
    LazyList<Country> results = Country.where(sql, params).offset(offset).limit(limit);
    result = new JsonArray(results.toJson(false, "country_id", "name", "code"));
    Base.close();
    return result;
  }
}
