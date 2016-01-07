package org.gooru.auth.handlers.processors.service.state;

import io.vertx.core.json.JsonObject;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.School;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;

public interface StateService {

  static StateService instance() {
    return new StateServiceImpl();
  }

  School createState(JsonObject stateJson);

  List<State> getStates(String query);
}
