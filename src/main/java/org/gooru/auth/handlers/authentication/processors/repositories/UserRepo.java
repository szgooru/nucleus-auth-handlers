package org.gooru.auth.handlers.authentication.processors.repositories;

import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.AJUserRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.User;

public interface UserRepo {

  static UserRepo getInstance() { 
    return new AJUserRepo();
  }
  
  User getUser(String userId);
  
  User saveOrUpdate(User user);
}
