package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJSchoolRepo extends AJAbstractRepo implements SchoolRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJSchoolRepo.class);

  private static final String GET_SCHOOL_BY_NAME = "name = ?";

  private static final String GET_SCHOOL_BY_ID = "id = ?::uuid";

  @Override
  public AJEntitySchool createSchool(AJEntitySchool school) {
    return (AJEntitySchool) saveOrUpdate(school);
  }

  @Override
  public AJEntitySchool createSchool(String name, String schoolDistrictId, String creatorId) {
    AJEntitySchool school = new AJEntitySchool();
    school.setName(name);
    school.setCode(UUID.randomUUID().toString());
    school.setCreatorId(creatorId);
    if (schoolDistrictId != null) {
      school.setSchoolDistrictId(schoolDistrictId);
    }
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
    AJEntitySchool school = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntitySchool> results = AJEntitySchool.where(whereClause, params);
      school = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connetion to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return school;
  }

}
