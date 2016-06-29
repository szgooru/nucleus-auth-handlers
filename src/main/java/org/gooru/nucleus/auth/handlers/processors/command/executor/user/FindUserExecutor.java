package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import java.util.List;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;

class FindUserExecutor implements DBExecutor {

    private final MessageContext messageContext;
    private String username;
    private String email;

    public FindUserExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        username = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_USERNAME);
        email = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_EMAIL);
        if (username == null && email == null) {
            throw new BadRequestException("Invalid param type passed");
        }
    }

    @Override
    public void validateRequest() {

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MessageResponse executeRequest() {
        Map<String, Object> user = null;
        if (email != null) {
            List<Map> results = Base.findAll(AJEntityUser.FIND_USER_USING_EMAIL, email);
            user = results.size() > 0 ? results.get(0) : null;
        } else if (username != null) {
            List<Map> results = Base.findAll(AJEntityUser.FIND_USER_USING_USERNAME, username.toLowerCase());
            user = results.size() > 0 ? results.get(0) : null;
        }
        ServerValidatorUtility.reject(user == null, MessageCodeConstants.AU0051, 404);
        return new MessageResponse.Builder().setResponseBody(AJResponseJsonTransformer.transform(user))
            .setContentTypeJson().setStatusOkay().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
