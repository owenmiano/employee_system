package ke.co.skyworld.handlers.period;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdatePeriod implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String periodIdString = pathMatch.getParameters().get("periodId");

            if (periodIdString != null && !periodIdString.trim().isEmpty()) {

                int periodId = Integer.parseInt(periodIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
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

                    String whereClause = "period_id = ?";

                    String result = GenericQueries.update(connection, "period", periodData, whereClause, periodId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);

                });

            } else {
                // Handle the case where the class ID is missing or empty
                exchange.setStatusCode(400);
                String errorMessage = "Period ID is required";
                exchange.getResponseSender().send(errorMessage);
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
