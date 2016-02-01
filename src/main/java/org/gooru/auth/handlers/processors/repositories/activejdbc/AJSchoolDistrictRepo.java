package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJSchoolDistrictRepo extends AJAbstractRepo implements SchoolDistrictRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJSchoolDistrictRepo.class);

  private static final String GET_SCHOOL_DISTRICT_BY_NAME = "name = ?";

  private static final String GET_SCHOOL_DISTRICT_BY_ID = "id = ?::uuid";

  @Override
  public AJEntitySchoolDistrict createSchoolDistrict(AJEntitySchoolDistrict schoolDistrict) {
    return (AJEntitySchoolDistrict) saveOrUpdate(schoolDistrict);
  }

  @Override
  public AJEntitySchoolDistrict createSchoolDistrict(String name, String creatorId) {
    AJEntitySchoolDistrict schoolDistrict = new AJEntitySchoolDistrict();
    schoolDistrict.setName(name);
    schoolDistrict.setCode(UUID.randomUUID().toString());
    schoolDistrict.setCreatorId(creatorId);
    return createSchoolDistrict(schoolDistrict);
  }

  @Override
  public AJEntitySchoolDistrict getSchoolDistrictById(String id) {
    return query(GET_SCHOOL_DISTRICT_BY_ID, id);
  }

  @Override
  public AJEntitySchoolDistrict getSchoolDistrictByName(String name) {
    return query(GET_SCHOOL_DISTRICT_BY_NAME, name);
  }

  private AJEntitySchoolDistrict query(String whereClause, Object... params) {
    AJEntitySchoolDistrict schoolDistrict = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntitySchoolDistrict> results = AJEntitySchoolDistrict.where(whereClause, params);
      schoolDistrict = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connection to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }

    return schoolDistrict;
  }
}
