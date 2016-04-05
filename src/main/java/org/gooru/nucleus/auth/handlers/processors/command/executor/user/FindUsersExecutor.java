package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;

public final class FindUsersExecutor extends Executor {

  private UserRepo userRepo;

  public FindUsersExecutor() {
    this.userRepo = UserRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String ids = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_IDS);
    ServerValidatorUtility.rejectIfNullOrEmpty(ids, MessageCodeConstants.AU0043, 400, ParameterConstants.PARAM_USER_IDS);
    final String[] userIds = ids.split(",");
    ServerValidatorUtility.reject(userIds.length > 30, MessageCodeConstants.AU0044, 400);
    Stream.of(userIds).forEach(userId -> {
      try {
        UUID.fromString(userId);
      } catch (Exception e) {
        ServerValidatorUtility.reject(true, MessageCodeConstants.AU0045, 400);
      }
    });
    return findUsers(ids);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private MessageResponse findUsers(String ids) {
    ids = ids.replaceAll(",", "','");
    List<Map> results = getUserRepo().findUsers("'" + ids + "'");
    JsonArray users = new JsonArray();
    if (results != null) {
      results.forEach(user -> users.add(AJResponseJsonTransformer.transform((Map<String, Object>) user)));
    }
    return new MessageResponse.Builder().setResponseBody(new JsonObject().put(ParameterConstants.PARAM_USERS, users)).setContentTypeJson()
        .setStatusOkay().successful().build();
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

}
