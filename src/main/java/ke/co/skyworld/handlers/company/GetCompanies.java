package ke.co.skyworld.handlers.company;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;
import io.undertow.util.Headers;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;

public class GetCompanies implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try{
// Extracting the columns parameter from the query string
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = null;
            if (columnsDeque != null && !columnsDeque.isEmpty()) {
                String columnsString = columnsDeque.getFirst();
                columns = columnsString.split(",");
            } else {
                columns = new String[]{"*"};
            }
            final String[] finalColumns = columns;
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, "company", finalColumns);
                    if (jsonArrayResult.size() == 0) {
                        // No teacher found with the given ID
                        exchange.setStatusCode(404); // Not Found
                        exchange.getResponseSender().send("Class not found.");
                    } else {
                        // Teacher found, send the result
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    }
                } catch (SQLException e) {
                    String errorMessage = "SQL Error occurred: " + e.getMessage();
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
