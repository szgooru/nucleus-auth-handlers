package org.gooru.nucleus.auth.handlers.processors.command.executor.internal;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class InternalExecutorFactory {

	public static DBExecutor userDetails(MessageContext messageContext) {
		return new UserDetailsExecutor(messageContext);
	}

	public static DBExecutor loginAsUser(MessageContext messageContext) {
		return new LoginAsUserExecutor(messageContext);
	}

}
