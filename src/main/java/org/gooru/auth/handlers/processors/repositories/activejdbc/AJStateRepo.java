package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.StateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJStateRepo extends AJAbstractRepo implements StateRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJStateRepo.class);

  private static final String GET_STATE_BY_NAME = "name = ?";

  private static final String GET_STATE_BY_ID = "id = ?::uuid";

  @Override
  public AJEntityState createState(AJEntityState state) {
    return (AJEntityState) saveOrUpdate(state);
  }

  @Override
  public AJEntityState createState(String name, String countryId, String creatorId) {
    AJEntityState state = new AJEntityState();
    state.setName(name);
    state.setCode(UUID.randomUUID().toString());
    state.setCreatorId(UUID.fromString(creatorId));
    if (countryId != null) {
      state.setCountryId(UUID.fromString(countryId));
    }
    return createState(state);
  }

  @Override
  public AJEntityState getStateById(String id) {
    return query(GET_STATE_BY_ID, id);
  }

  @Override
  public AJEntityState getStateByName(String name) {
    return query(GET_STATE_BY_NAME, name);
  }

  private AJEntityState query(String whereClause, Object... params) {
    AJEntityState state = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityState> results = AJEntityState.where(whereClause, params);
      state = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connection to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }

    return state;
  }
}
