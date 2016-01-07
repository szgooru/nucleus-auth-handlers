package org.gooru.auth.handlers.processors.repositories;

import io.vertx.core.json.JsonArray;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJCountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.Country;

public interface CountryRepo {

  static CountryRepo instance() {
    return new AJCountryRepo();
  }
  
  Country createCountry(Country country);
  
  JsonArray getCountries(String name, long offset, long limit);
}
