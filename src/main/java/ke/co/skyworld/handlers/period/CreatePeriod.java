package ke.co.skyworld.handlers.period;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class CreatePeriod implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject periodData = gson.fromJson(requestBody, JsonObject.class);

                    if (!periodData.has("period") || periodData.get("period").getAsString().trim().isEmpty()) {
                        String errorMessage = "Period is missing.";
                        exchange.setStatusCode(400);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }
                    if (!periodData.has("status") || periodData.get("status").getAsString().trim().isEmpty()) {
                        String errorMessage = "Period status is missing.";
                        exchange.setStatusCode(400);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String insertionResult = GenericQueries.insertData(connection, "period", periodData);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(insertionResult);
                } catch (Exception e) {
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send("Error: "+e.getMessage());
                }
            });
        } finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
