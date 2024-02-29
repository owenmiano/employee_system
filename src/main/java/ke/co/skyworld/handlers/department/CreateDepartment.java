package ke.co.skyworld.handlers.department;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class CreateDepartment implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject departmentData = gson.fromJson(requestBody, JsonObject.class);

                    if (!departmentData.has("name") || departmentData.get("name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Department name is missing.";
                        exchange.setStatusCode(404);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String insertionResult = GenericQueries.insertData(connection, "department", departmentData);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(insertionResult);
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
