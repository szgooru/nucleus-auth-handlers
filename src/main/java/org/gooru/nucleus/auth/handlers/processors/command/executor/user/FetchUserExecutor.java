package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import java.util.List;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;

class FetchUserExecutor implements DBExecutor {

    private final MessageContext messageContext;
    private String userId;
    private Map<String, Object> user;

    public FetchUserExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
        if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
            userId = messageContext.user().getString(ParameterConstants.PARAM_USER_ID);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void validateRequest() {
        List<Map> results = Base.findAll(AJEntityUser.FIND_USER, userId);
        user = results.size() > 0 ? results.get(0) : null;
        ServerValidatorUtility.rejectIfNull(user, MessageCodeConstants.AU0026,
            HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    }

    @Override
    public MessageResponse executeRequest() {
        return new MessageResponse.Builder()
            .setResponseBody(JsonFormatterBuilder.buildSimpleJsonFormatter(false, null).mapToJson(user))
            .setContentTypeJson().setStatusOkay().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
