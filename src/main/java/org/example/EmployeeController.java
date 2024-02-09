package org.example;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

public class EmployeeController {
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }

    private static boolean isValidIDNumber(String idNumber) {
        String idNumberRegex = "^\\d{8}$";
        return idNumber != null && idNumber.matches(idNumberRegex);
    }
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }

    public static void findEmployee(Connection connection, HashMap<String, Object> employeeData, String[] originalColumns) {
        try {
            if (employeeData == null || employeeData.isEmpty()) {
                System.out.println("No employee data provided.");
                return;
            }

            // Initialize the list with the extended columns
            ArrayList<String> columnsList = new ArrayList<>();
            for (String column : originalColumns) {
                // Prefix with "employee." to avoid ambiguity
                columnsList.add("e." + column);
            }

            // Correctly add the company and department names
            columnsList.add("c.name company_name");
            columnsList.add("d.name department_name");

            // Convert the ArrayList to an array for use in the query
            String[] columns = columnsList.toArray(new String[0]);

            // Adjust the joins to include the company and department tables
            String[][] joins = {
                    {"INNER", "company c", "e.company_id = c.company_id"},
                    {"INNER", "department d", "e.department_id = d.department_id"}
            };

            // Construct the WHERE clause based on provided employeeData
            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : employeeData.entrySet()) {
                String key = "e." + entry.getKey(); // Qualify key with the employee table name to avoid ambiguity
                whereClauseJoiner.add(key + " = ?");
                values.add(entry.getValue());
            }

            String whereClause = whereClauseJoiner.toString();

            // Assuming GenericQueries.select can handle joins, extended columns, and parameterized where clauses
            JsonArray jsonArrayResult = GenericQueries.select(connection, "employee e", joins, columns, whereClause, values.toArray());

            // Convert the result to JSON and print
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static void createEmployee(Connection connection, HashMap<String, Object> employeeData) {
        try {
            if (employeeData == null) {
                System.out.println("Employee data is missing or incomplete.");
                return;
            }
            if (employeeData.containsKey("password")) {
                String plainPassword = employeeData.get("password").toString();
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                employeeData.put("password", hashedPassword);
            } else {
                System.out.println("Password is missing.");
                return;
            }
            String emailAddress = employeeData.get("email") != null ? employeeData.get("email").toString() : "";
            String idNumber = employeeData.get("id_number") != null ? employeeData.get("id_number").toString() : "";
            String phone = employeeData.get("phone") != null ? employeeData.get("phone").toString() : "";


            if (!isValidEmail(emailAddress)) {
                System.out.println("Invalid email address.");
                return;
            }

            if (!isValidIDNumber(idNumber)) {
                System.out.println("Invalid ID number.");
                return;
            }

            if (!isValidPhoneNumber(phone)) {
                System.out.println("Invalid phone number.");
                return;
            }
            Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
            String period = activePeriodInfo.get("period");
            System.out.println("Current "+period);
            System.out.println("employment start date "+employeeData.get("employment_start_date").toString());
            if (period == null) {
                System.out.println("Failed to fetch the active period or no active period found.");
                return;
            }

            String employmentStartDateStr = employeeData.get("employment_start_date").toString();
            // Assuming employment_start_date in employeeData and periods in database are in format "MM-yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
            YearMonth employmentStartDate = YearMonth.parse(employmentStartDateStr, formatter);
            YearMonth activePeriodDate = YearMonth.parse(period, formatter);

            // Check if the employment start date is greater than the active period
            if (employmentStartDate.isAfter(activePeriodDate) || employmentStartDate.equals(activePeriodDate)) {
                // If the start date is after the active period or exactly the same, set the status to "new"
                employeeData.put("employment_status", "new");
            } else {
                // If the start date is before the active period, then it's considered "active"
                employeeData.put("employment_status", "active");
            }

            boolean isInserted = GenericQueries.insertData(connection, "employee", employeeData);

            if (isInserted) {
                System.out.println("Employee added successfully");
            } else {
                System.out.println("Failed to add employee");
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String fetchEmployeeTermnationDate(Connection connection, int employeeId) {
        String[] columns = {"employment_termination_date"};
        String whereClause = "employee_id = ?";
        List<Integer> values = Collections.singletonList(employeeId);

        try {
            JsonArray result = GenericQueries.select(connection, "employee", columns, whereClause, values.toArray());
            if (result.size() > 0) {
                // Assuming the first element of the JsonArray is the JsonObject we're interested in
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject employeeObject = firstElement.getAsJsonObject();
                    // Corrected to fetch the 'employment_termination_date' field
                    JsonElement terminationDateElement = employeeObject.get("employment_termination_date");
                    if (terminationDateElement != null && !terminationDateElement.isJsonNull()) {
                        return terminationDateElement.getAsString();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred fetching the termination date: " + e.getMessage());
        }

        return null; // Return null if no termination date is found or in case of an error
    }

    public static String fetchEmployeeStartDate(Connection connection, int employeeId) {
        String[] columns = {"employment_start_date"};
        String whereClause = "employee_id = ?";
        List<Integer> values = Collections.singletonList(employeeId);

        try {
            JsonArray result = GenericQueries.select(connection, "employee", columns, whereClause, values.toArray());
            if (result.size() > 0) {
                // Assuming the first element of the JsonArray is the JsonObject we're interested in
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject employeeObject = firstElement.getAsJsonObject();
                    // Corrected to fetch the 'employment_termination_date' field
                    JsonElement startDateElement = employeeObject.get("employment_start_date");
                    if (startDateElement != null && !startDateElement.isJsonNull()) {
                        return startDateElement.getAsString();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred fetching the termination date: " + e.getMessage());
        }

        return null; // Return null if no termination date is found or in case of an error
    }





    public static void updateEmployee(Connection connection, HashMap<String, Object> employeeData, int employeeID) {
        try {
            if (employeeData == null || employeeData.isEmpty()) {
                System.out.println("Employee data is missing or empty.");
                return;
            }
            String whereClause = "employee_id = ?";

            JsonObject result = GenericQueries.update(connection, "employee", employeeData, whereClause,new Object[]{employeeID});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Employee updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
