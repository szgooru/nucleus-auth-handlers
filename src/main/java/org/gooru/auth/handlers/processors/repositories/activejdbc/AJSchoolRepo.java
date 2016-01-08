package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.School;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJSchoolRepo extends AJAbstractRepo implements SchoolRepo {

  private static final String GET_SCHOOL_BY_NAME = "name = ?";

  private static final String GET_SCHOOL_BY_ID = "id = ?";

  @Override
  public School createSchool(School school) {
    Base.open(dataSource());
    school.toInsert();
    school.insert();
    Base.commitTransaction();
    Base.close();
    return school;
  }
  @Override
  public School getSchoolById(String id) {
    return query(GET_SCHOOL_BY_ID, id);
  }

  @Override
  public School getSchoolByName(String name) {
    return query(GET_SCHOOL_BY_NAME, name);
  }
  
  private School query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<School> results = School.where(whereClause, params);
    School school = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return school;
  }


}
