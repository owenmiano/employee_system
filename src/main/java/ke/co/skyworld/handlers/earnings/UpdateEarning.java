package ke.co.skyworld.handlers.earnings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdateEarning implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String earningTypeIdString = pathMatch.getParameters().get("earningTypeId");

            if (earningTypeIdString != null && !earningTypeIdString.trim().isEmpty()) {

                int earningTypeId = Integer.parseInt(earningTypeIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject earningsTypeData = gson.fromJson(requestBody, JsonObject.class);

                    if (!earningsTypeData.has("name") || earningsTypeData.get("name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Earning type is missing.";
                        System.out.println(errorMessage);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String whereClause = "earning_types_id = ?";

                    String result = GenericQueries.update(connection, "earning_types", earningsTypeData, whereClause, earningTypeId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);

                });

            } else {
                exchange.setStatusCode(400);
                String errorMessage = "Earning ID is required";
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
