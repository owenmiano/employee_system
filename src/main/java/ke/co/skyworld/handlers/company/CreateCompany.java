package ke.co.skyworld.handlers.company;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.util.regex.Pattern;

public class CreateCompany implements HttpHandler {
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject companyData = gson.fromJson(requestBody, JsonObject.class);
                    String emailAddress = companyData.has("email") ? companyData.get("email").getAsString() : "";

                    if (!isValidEmail(emailAddress)) {
                        String errorMessage = "Invalid email address.";
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                        return;
                    }
                    if (!companyData.has("name") || companyData.get("name").getAsString().isEmpty()){
                        String errorMessage = "Company name is missing.";
                        exchange1.setStatusCode(StatusCodes.BAD_REQUEST);
                        exchange1.getResponseSender().send(errorMessage);
                        return;
                    }
                    if (!companyData.has("branch") || companyData.get("branch").getAsString().isEmpty()){
                        String errorMessage = "Company branch is missing.";
                        exchange1.setStatusCode(StatusCodes.BAD_REQUEST);
                        exchange1.getResponseSender().send(errorMessage);
                        return;
                    }
                    if (!companyData.has("postal_address") || companyData.get("postal_address").getAsString().isEmpty()){
                        String errorMessage = "Company postal address is missing.";
                        exchange1.setStatusCode(StatusCodes.BAD_REQUEST);
                        exchange1.getResponseSender().send(errorMessage);
                        return;
                    }
                    if (!companyData.has("company_inception") || companyData.get("company_inception").getAsString().isEmpty()){
                        String errorMessage = "company inception date is missing.";
                        exchange1.setStatusCode(StatusCodes.BAD_REQUEST);
                        exchange1.getResponseSender().send(errorMessage);
                        return;
                    }
                    String insertionResult = GenericQueries.insertData(connection, "company", companyData);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(insertionResult);


        }catch (Exception e) {
            String errorMessage = "Error processing request: " + e.getMessage();
            exchange.getResponseSender().send(errorMessage);
        }
        });
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
