package org.gooru.auth.handlers.authentication.processors.repositories;

import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.AJUserRepo;

public interface UserRepo {

  static UserRepo getInstance() { 
    return new AJUserRepo();
  }
}
