package ke.co.skyworld.handlers.earnings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.util.Deque;


public class GetEarning implements HttpHandler {


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the Department ID from the path parameters
            Deque<String> earningType = exchange.getQueryParameters().get("name");
            // Check if earning type parameter is missing
            if (earningType == null || earningType.isEmpty()) {
                exchange.setStatusCode(400); // Bad Request
                exchange.getResponseSender().send("Earning type parameter 'name' is missing.");
                return;
            }
            String whereClause = "name = ?";
            JsonArray jsonArrayResult = GenericQueries.select(connection, "earning_types",whereClause,earningType );
            if (jsonArrayResult.isEmpty()) {
                exchange.setStatusCode(404); // Not Found
                exchange.getResponseSender().send("Earning type not found.");
            } else {
                JsonArray idArray = new JsonArray();

                // Extracting only the IDs from jsonArrayResult
                for (JsonElement element : jsonArrayResult) {
                    JsonObject obj = element.getAsJsonObject();
                    idArray.add(obj.get("earning_types_id"));
                }

                // Setting the response headers and sending the IDs as a JSON array
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(idArray.toString());
            }
        }catch (Exception e){
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Error: "+e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }

    }
}
