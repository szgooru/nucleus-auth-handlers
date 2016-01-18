package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJUserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;

public interface UserIdentityRepo {
  static UserIdentityRepo instance() {
    return new AJUserIdentityRepo();
  }
  AJEntityUserIdentity getUserIdentityByUsernameAndPassword(final String username, final String password);
  
  AJEntityUserIdentity getUserIdentityByEmailIdAndPassword(final String emailId, final String password);
  
  AJEntityUserIdentity getUserIdentityByEmailId(final String emailId);
  
  AJEntityUserIdentity getUserIdentityByReferenceId(final String referenceId);
  
  AJEntityUserIdentity getUserIdentityByUsername(final String username);
  
  AJEntityUserIdentity createOrUpdate(final AJEntityUserIdentity userIdentity);
  
  AJEntityUserIdentity getUserIdentityByIdAndPassword(final String userId, final String password);
  
  AJEntityUserIdentity getUserIdentityById(final String userId);
    
}
