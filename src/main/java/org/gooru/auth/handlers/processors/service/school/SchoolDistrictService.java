package org.gooru.auth.handlers.processors.service.school;

import io.vertx.core.json.JsonObject;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.SchoolDistrict;

public interface SchoolDistrictService {

  static SchoolDistrictService instance() {
    return new SchoolDistrictServiceImpl();
  }

  SchoolDistrict createSchoolDistrict(JsonObject schoolDistrictJson);

  List<SchoolDistrict> getSchoolDistricts(String query);
}
