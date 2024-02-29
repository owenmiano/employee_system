package ke.co.skyworld.handlers.earnings;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;

public class GetEarnings implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try{
// Extracting the columns parameter from the query string
            String[] columns = {
                    "et.name AS Earning",
                    "c.name AS Company"
            };


            // Specify the table and any joins
            String table = "earning_types et JOIN company c ON et.company_id = c.company_id";
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                    if (jsonArrayResult.isEmpty()) {
                        exchange.setStatusCode(404); // Not Found
                        exchange.getResponseSender().send("Earning types not found.");
                    }
                    exchange1.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(jsonArrayResult.toString());

                } catch (SQLException e) {
                    String errorMessage = "Error occurred: " + e.getMessage();
                    exchange.getResponseSender().send(errorMessage);
                }
            });
        }
        finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
