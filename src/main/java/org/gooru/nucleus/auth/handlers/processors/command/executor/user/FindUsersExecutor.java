package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;

class FindUsersExecutor implements DBExecutor {

    private MessageContext messageContext;
    private String ids;

    public FindUsersExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        ids = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_IDS);
        ServerValidatorUtility.rejectIfNullOrEmpty(ids, MessageCodeConstants.AU0043, 400,
            ParameterConstants.PARAM_USER_IDS);
        final String[] userIds = ids.split(",");
        ServerValidatorUtility.reject(userIds.length > 50, MessageCodeConstants.AU0044, 400);
        Stream.of(userIds).forEach(userId -> {
            try {
                UUID.fromString(userId);
            } catch (Exception e) {
                ServerValidatorUtility.reject(true, MessageCodeConstants.AU0045, 400);
            }
        });
    }

    @Override
    public void validateRequest() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public MessageResponse executeRequest() {
        ids = ids.replaceAll(",", "','");
        List<Map> results = Base.findAll(AJEntityUser.FIND_USERS + "'" + ids + "')");
        JsonArray users = new JsonArray();
        if (results != null) {
            results.forEach(user -> users.add(JsonFormatterBuilder.buildSimpleJsonFormatter(false, null).mapToJson(
                (Map<String, Object>) user)));
        }
        return new MessageResponse.Builder()
            .setResponseBody(new JsonObject().put(ParameterConstants.PARAM_USERS, users)).setContentTypeJson()
            .setStatusOkay().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
