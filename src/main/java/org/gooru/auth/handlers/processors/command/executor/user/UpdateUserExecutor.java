package org.gooru.auth.handlers.processors.command.executor.user;

import static org.gooru.auth.handlers.utils.ServerValidatorUtility.addValidator;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectError;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.gooru.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.auth.handlers.processors.repositories.StateRepo;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;

public final class UpdateUserExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private CountryRepo countryRepo;

  private StateRepo stateRepo;

  private SchoolRepo schoolRepo;

  private SchoolDistrictRepo schoolDistrictRepo;

  public UpdateUserExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserRepo(UserRepo.instance());
    setCountryRepo(CountryRepo.instance());
    setStateRepo(StateRepo.instance());
    setSchoolRepo(SchoolRepo.instance());
    setSchoolDistrictRepo(SchoolDistrictRepo.instance());

  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
    if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
      userId = messageContext.user().getString(ParameterConstants.PARAM_USER_ID);
    }
    UserDTO userDTO = new UserDTO(messageContext.requestBody());

    return updateUser(userId, userDTO);
  }

  private MessageResponse updateUser(String userId, UserDTO userDTO) {
    ActionResponseDTO<AJEntityUser> userValidator = updateUserValidator(userId, userDTO);
    rejectError(userValidator.getErrors(), HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    AJEntityUser user = userValidator.getModel();
    user = getUserRepo().update(user);
    EventBuilder eventBuilder = userValidator.getEventBuilder().setEventName(Event.UPDATE_USER.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
            AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS));
    if (userDTO.getUsername() != null) {
      final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
      userIdentity.setUsername(userDTO.getUsername());
      getUserIdentityRepo().createOrUpdate(userIdentity);
      eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    }
    return new MessageResponse.Builder().setContentTypeJson().setEventData(eventBuilder.build()).setStatusNoOutput().successful().build();
  }

  private ActionResponseDTO<AJEntityUser> updateUserValidator(final String userId, final UserDTO userDTO) {
    final AJEntityUser user = getUserRepo().getUser(userId);
    rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    final Errors errors = new Errors();
    final String username = userDTO.getUsername();
    final EventBuilder eventBuilder = new EventBuilder();

    if (userDTO.getFirstname() != null) {
      addValidator(errors, !(userDTO.getFirstname().matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME, MessageCodeConstants.AU0021, ParameterConstants.PARAM_USER_FIRSTNAME);
      user.setFirstname(userDTO.getFirstname());
    }
    if (userDTO.getLastname() != null) {
      addValidator(errors, !(userDTO.getLastname().matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME, MessageCodeConstants.AU0021, ParameterConstants.PARAM_USER_LASTNAME);
      user.setLastname(userDTO.getLastname());
    }
    if (userDTO.getGender() != null) {
      addValidator(errors, (HelperConstants.USER_GENDER.get(userDTO.getGender()) == null), ParameterConstants.PARAM_USER_GENDER,
              MessageCodeConstants.AU0024);
      user.setGender(userDTO.getGender());
    }
    if (userDTO.getUserCategory() != null) {
      addValidator(errors, (HelperConstants.USER_CATEGORY.get(userDTO.getUserCategory()) == null), ParameterConstants.PARAM_USER_CATEGORY,
              MessageCodeConstants.AU0025);
      user.setUserCategory(userDTO.getUserCategory());
    }
    if (username != null) {
      addValidator(errors, !(username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0017);
      addValidator(errors, ((username.length() < 4 || username.length() > 20)), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0018,
              ParameterConstants.PARAM_USER_USERNAME, "4", "20");
      AJEntityUserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
      addValidator(errors, !(userIdentityUsername == null), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0023, username,
              ParameterConstants.PARAM_USER_USERNAME);
    }

    if (userDTO.getCountryId() != null) {
      AJEntityCountry country = getCountryRepo().getCountry(userDTO.getCountryId());
      addValidator(errors, (country == null), ParameterConstants.PARAM_USER_COUNTRY_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_COUNTRY);
      user.setCountryId(userDTO.getCountryId());
    } else if (userDTO.getCountry() != null) {
      AJEntityCountry country = getCountryRepo().getCountryByName(userDTO.getCountry());
      if (country == null) {
        country = getCountryRepo().createCountry(userDTO.getCountry(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.COUNTRY, AJResponseJsonTransformer.transform(country.toJson(false)));
      }
      user.setCountryId(country.getId());
    }

    if (userDTO.getStateId() != null) {
      AJEntityState state = getStateRepo().getStateById(userDTO.getStateId());
      addValidator(errors, (state == null), ParameterConstants.PARAM_USER_STATE_ID, MessageCodeConstants.AU0027, ParameterConstants.PARAM_USER_STATE);
      user.setStateId(userDTO.getStateId());
    } else if (userDTO.getState() != null) {
      AJEntityState state = getStateRepo().getStateByName(userDTO.getState());
      if (state == null) {
        state = getStateRepo().createState(userDTO.getState(), user.getCountryId(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.STATE, AJResponseJsonTransformer.transform(state.toJson(false)));
      }
      user.setStateId(state.getId());
    }

    if (userDTO.getSchoolDistrictId() != null) {
      AJEntitySchoolDistrict schoolDistrict = getSchoolDistrictRepo().getSchoolDistrictById(userDTO.getSchoolDistrictId());
      addValidator(errors, (schoolDistrict == null), ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
      user.setSchoolDistrictId(userDTO.getSchoolDistrictId());
    } else if (userDTO.getSchoolDistrict() != null) {
      AJEntitySchoolDistrict schoolDistrict = getSchoolDistrictRepo().getSchoolDistrictByName(userDTO.getSchoolDistrict());
      if (schoolDistrict == null) {
        schoolDistrict = getSchoolDistrictRepo().createSchoolDistrict(userDTO.getSchoolDistrict(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.SCHOOL_DISTRICT, AJResponseJsonTransformer.transform(schoolDistrict.toJson(false)));
      }
      user.setSchoolDistrictId(schoolDistrict.getId());
    }

    if (userDTO.getSchoolId() != null) {
      AJEntitySchool school = getSchoolRepo().getSchoolById(userDTO.getSchoolId());
      addValidator(errors, (school == null), ParameterConstants.PARAM_USER_SCHOOL_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_SCHOOL);
      user.setSchoolId(userDTO.getSchoolId());
    } else if (userDTO.getSchool() != null) {
      AJEntitySchool school = getSchoolRepo().getSchoolByName(userDTO.getSchool());
      if (school == null) {
        school = getSchoolRepo().createSchool(userDTO.getSchool(), user.getSchoolDistrictId(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.SCHOOL, AJResponseJsonTransformer.transform(school.toJson(false)));
      }
      user.setSchoolId(school.getId());
    }

    if (userDTO.getGrade() != null) {
      user.setGrade(userDTO.getGrade());
    }

    if (userDTO.getCourse() != null) {
      user.setCourse(userDTO.getCourse());
    }
    if (userDTO.getAboutMe() != null) {
      user.setAboutMe(userDTO.getAboutMe());
    }
    if (userDTO.getThumbnailPath() != null) {
      user.setThumbnailPath(userDTO.getThumbnailPath());
    }
    return new ActionResponseDTO<>(user, eventBuilder, errors);

  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  public CountryRepo getCountryRepo() {
    return countryRepo;
  }

  public void setCountryRepo(CountryRepo countryRepo) {
    this.countryRepo = countryRepo;
  }

  public StateRepo getStateRepo() {
    return stateRepo;
  }

  public void setStateRepo(StateRepo stateRepo) {
    this.stateRepo = stateRepo;
  }

  public SchoolRepo getSchoolRepo() {
    return schoolRepo;
  }

  public void setSchoolRepo(SchoolRepo schoolRepo) {
    this.schoolRepo = schoolRepo;
  }

  public SchoolDistrictRepo getSchoolDistrictRepo() {
    return schoolDistrictRepo;
  }

  public void setSchoolDistrictRepo(SchoolDistrictRepo schoolDistrictRepo) {
    this.schoolDistrictRepo = schoolDistrictRepo;
  }

}
