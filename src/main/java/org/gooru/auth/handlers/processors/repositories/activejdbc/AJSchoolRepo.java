package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJSchoolRepo extends AJAbstractRepo implements SchoolRepo {

  private static final String GET_SCHOOL_BY_NAME = "name = ?";

  private static final String GET_SCHOOL_BY_ID = "id = ?";

  @Override
  public AJEntitySchool createSchool(AJEntitySchool school) {
    return (AJEntitySchool) save(school);
  }

  @Override
  public AJEntitySchool createSchool(String name, String creatorId) {
    AJEntitySchool school = new AJEntitySchool();
    school.setId(UUID.randomUUID().toString());
    school.setName(name);
    school.setCode(UUID.randomUUID().toString());
    school.setCreatorId(creatorId);
    return createSchool(school);
  }

  @Override
  public AJEntitySchool getSchoolById(String id) {
    return query(GET_SCHOOL_BY_ID, id);
  }

  @Override
  public AJEntitySchool getSchoolByName(String name) {
    return query(GET_SCHOOL_BY_NAME, name);
  }

  private AJEntitySchool query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<AJEntitySchool> results = AJEntitySchool.where(whereClause, params);
    AJEntitySchool school = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return school;
  }

}
