package com.github.mmatczuk.ablb.web;

import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mmatczuk.ablb.dispather.DispatcherService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * Async routing http handler implementation.
 *
 * @author mmatczuk
 */
@Component
public class RouteHttpHandler implements HttpHandler {
	private final DispatcherService mDispatcherService;

	@Autowired
	RouteHttpHandler(DispatcherService dispatcherService) {
		mDispatcherService = dispatcherService;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		final String userId = getUserId(exchange);
		if (userId == null) {
			exchange.setResponseCode(StatusCodes.BAD_REQUEST);
			exchange.getResponseSender().close();
		} else {
			final String group = mDispatcherService.groupName(userId);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.getResponseSender().send(group);
		}
	}

	private String getUserId(HttpServerExchange exchange) {
		final Deque<String> values = exchange.getQueryParameters().get("id");
		return values != null ?
				values.getFirst() : null;
	}
}
