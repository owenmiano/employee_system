package ke.co.skyworld.rest.base;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public final class Dispatcher implements HttpHandler {
    private final HttpHandler handler;

    public Dispatcher(HttpHandler handler) {
        this.handler = handler;
    }

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.dispatch(this.handler);
    }
}