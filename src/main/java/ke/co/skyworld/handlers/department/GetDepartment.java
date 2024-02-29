package ke.co.skyworld.handlers.department;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.util.Deque;

public class GetDepartment implements HttpHandler {


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the Department ID from the path parameters
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String departmentIdString = pathMatch.getParameters().get("departmentId");

            String[] columns = {
                    "d.name AS Department",
                    "c.name AS Company"
            };


            // Specify the table and any joins
            String table = "department d JOIN company c ON d.company_id = c.company_id";


            // Ensure classIdString is not null or empty before parsing
            if (departmentIdString == null || departmentIdString.isEmpty()) {
                exchange.setStatusCode(400);
                exchange.getResponseSender().send("Department ID must be provided.");
                return;
            }

            int departmentId = Integer.parseInt(departmentIdString);
            String whereClause = "d.department_id = ?";
            JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns, whereClause, departmentId);
            if (jsonArrayResult.size() == 0) {
                exchange.setStatusCode(404); // Not Found
                exchange.getResponseSender().send("Department not found.");
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(jsonArrayResult.toString());
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