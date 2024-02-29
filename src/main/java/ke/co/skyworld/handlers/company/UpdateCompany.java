package ke.co.skyworld.handlers.company;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdateCompany implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String companyIdString = pathMatch.getParameters().get("companyId");

            if (companyIdString != null && !companyIdString.trim().isEmpty()) {
                    int companyId = Integer.parseInt(companyIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject companyData = gson.fromJson(requestBody, JsonObject.class);

                        String whereClause = "company_id = ?";

                        String result = GenericQueries.update(connection, "company", companyData, whereClause, companyId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(result);
                    });

            } else {
                // Handle the case where the teacher ID is missing or empty
                String errorMessage = "Company ID is required";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        }catch (Exception e){
            String errorMessage = "Error occurred: " + e.getMessage();
            exchange.getResponseSender().send(errorMessage);
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}