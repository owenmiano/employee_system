package ke.co.skyworld.handlers.company;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;
import com.google.gson.JsonArray;

import java.sql.Connection;
import java.util.Deque;

public class GetCompany implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String companyIdString = pathMatch.getParameters().get("companyId");
// Extracting the columns parameter from the query string
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = null;
            if (columnsDeque != null && !columnsDeque.isEmpty()) {
                String columnsString = columnsDeque.getFirst();
                columns = columnsString.split(",");
            } else {
                // If no columns parameter provided, select all columns
                columns = new String[]{"*"};
            }

            // Ensure classIdString is not null or empty before parsing
            if (companyIdString == null || companyIdString.isEmpty()) {
                exchange.setStatusCode(400);
                exchange.getResponseSender().send("Company ID must be provided.");
                return;
            }

            int companyId = Integer.parseInt(companyIdString);
            final String[] finalColumns = columns; // Final copy of columns array

            JsonArray jsonArrayResult = GenericQueries.select(connection, "company", finalColumns, "company_id = ?", companyId);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            if (jsonArrayResult.size() == 0) {
                exchange.setStatusCode(404); // Not Found
                exchange.getResponseSender().send("Company not found.");
            } else {
                exchange.getResponseSender().send(jsonArrayResult.toString());
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
