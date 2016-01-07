package org.gooru.auth.handlers.processors.service.country;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.Country;

public interface CountryService {
  static CountryService instance() { 
   return new CountryServiceImpl();
 }
 
  Country createCountry(String  name, String code);
  
  JsonObject getCountries(String query);
}
