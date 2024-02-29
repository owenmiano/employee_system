package ke.co.skyworld.handlers.department;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdateDepartment implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String departmentIdString = pathMatch.getParameters().get("departmentId");

            if (departmentIdString != null && !departmentIdString.trim().isEmpty()) {

                int departmentId = Integer.parseInt(departmentIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject departmentData = gson.fromJson(requestBody, JsonObject.class);

                    if (!departmentData.has("name") || departmentData.get("name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Department name is missing.";
                        System.out.println(errorMessage);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String whereClause = "department_id = ?";

                    String result = GenericQueries.update(connection, "department", departmentData, whereClause, departmentId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);

                });

            } else {
                exchange.setStatusCode(400);
                String errorMessage = "Department ID is required";
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
