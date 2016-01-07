package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.School;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;
import org.javalite.activejdbc.Base;

public class AJSchoolRepo extends AJAbstractRepo implements SchoolRepo {

  private static final String LIST_SCHOOL = "name like  ?";
  
  private static final String LIST_SCHOOL_DISTRICT_SCHOOL = "school_district_id = ? and name like  ?";

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
  public List<School> getSchools(String name, long offset, long limit) {
    return queryList(LIST_SCHOOL, offset, limit, beginsWithPattern(name));
  }

  @Override
  public List<School> getSchools(String name, String schoolDistrictId, long offset, long limit) {
    return queryList(LIST_SCHOOL_DISTRICT_SCHOOL, offset, limit, schoolDistrictId, beginsWithPattern(name));
  }

  private List<School> queryList(String whereClause, long offset, long limit, Object... params) {
    Base.open(dataSource());
    List<School> results = State.where(whereClause, params).offset(offset).limit(limit);
    Base.close();
    return results;
  }

}
