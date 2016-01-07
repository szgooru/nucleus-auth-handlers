package org.gooru.auth.handlers.processors.service.school;

import io.vertx.core.json.JsonObject;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.School;

public interface SchoolService {

  static SchoolService instance() {
    return new SchoolServiceImpl();
  }
  
  School createSchool(JsonObject school);

  List<School> getSchools(String query);
  
  
}
