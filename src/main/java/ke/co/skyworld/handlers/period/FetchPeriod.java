package ke.co.skyworld.handlers.period;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FetchPeriod implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String periodIdString = pathMatch.getParameters().get("periodId");

        if (periodIdString == null || periodIdString.isEmpty()) {
            exchange.setStatusCode(400);
            exchange.getResponseSender().send("Period ID must be provided.");
            return;
        }

        int periodId = Integer.parseInt(periodIdString);
        String[] columns = {"period"};
        String whereClause = "period_id = ?";
        Map<String, String> periodInfo = new HashMap<>();

        try {
            JsonArray result = GenericQueries.select(connection, "period", columns, whereClause, periodId);
            if (result.size() > 0) {
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject periodObject = firstElement.getAsJsonObject();
                    periodInfo.put("period", periodObject.get("period").getAsString());

                }
            }
            else {
                exchange.setStatusCode(404);
                exchange.getResponseSender().send("No results found for the specified period id ");
            }
            // Convert periodInfo to JSON
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(periodInfo);

            // Set response content type and send JSON response
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(jsonResponse);
        } catch (Exception e) {
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("An error occurred fetching the active period: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
