package org.example;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rollover {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");

    public static void rollover(Connection connection) {
        try {
            // Disable auto-commit to manually manage transactions
            connection.setAutoCommit(false);

            updateActivePeriod(connection);
            String updateMessage = updateEmploymentStatus(connection);
            System.out.println(updateMessage);
            updatePayments(connection);

            // If all operations are successful, commit the transaction
            connection.commit();
        } catch (Exception e) {
            // If there is any exception, attempt to rollback changes
            try {
                connection.rollback();
                System.out.println("Rollover failed, changes have been rolled back.");
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.out.println("An error occurred during the rollover process: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Re-enable auto-commit mode
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Failed to re-enable auto-commit mode: " + e.getMessage());
            }
        }
    }
    public static void updateActivePeriod(Connection connection) {
        try {
        Map<String, String> periodInfo = PeriodController.fetchActivePeriod(connection);
        if (periodInfo != null && !periodInfo.isEmpty()) {
        int periodId = Integer.parseInt(periodInfo.get("period_id"));
        String period = periodInfo.get("period");
        YearMonth activePeriodDate = YearMonth.parse(period, formatter);
        YearMonth nextPeriodDate = activePeriodDate.plusMonths(1);
        String nextPeriod = nextPeriodDate.format(formatter);

            // Prepare data to update: setting status to "closed"
            HashMap<String, Object> updateData = new HashMap<>();
            updateData.put("status", "Closed");

            // Define the WHERE clause to identify the correct period to update
            String whereClause = "period_id = ?";

             //Execute the update
            JsonObject result = GenericQueries.update(connection, "period", updateData, whereClause, new Object[]{periodId});

            if (result != null && result.has("success") && result.get("success").getAsBoolean()) {
                System.out.println("Current period has been closed successfully");
                updateData.put("period", nextPeriod);
                updateData.put("status", "Current");
                boolean isInserted = GenericQueries.insertData(connection, "period", updateData);

                if (isInserted) {
                    System.out.println("Next Period added successfully");
                } else {
                    System.out.println("Failed to add period");
                }
            }
            else {
                System.out.println("Failed to update period status.");
            }
        }
        else {
            System.out.println("No active period found or failed to fetch period information.");
        }
        } catch (SQLException e) {
            System.out.println("An error occurred during the update process: " + e.getMessage());
            e.printStackTrace();
        }

    }


    public static String updateEmploymentStatus(Connection connection) {
        Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
        String period = activePeriodInfo.get("period");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth activePeriodDate = YearMonth.parse(period, formatter);

        String[] columns = {"employee_id", "employment_start_date", "employment_termination_date", "employment_status"};
        try {
            JsonArray result = GenericQueries.select(connection, "employee", columns);
            if (result.size() > 0) {
                for (JsonElement element : result) {
                    if (element != null && element.isJsonObject()) {
                        JsonObject employeeObject = element.getAsJsonObject();
                        int employeeId = employeeObject.get("employee_id").getAsInt();
                        String currentEmploymentStatus = employeeObject.get("employment_status").getAsString();

                        // Extract the start and termination dates
                        String startDateStr = employeeObject.get("employment_start_date").getAsString();
                        YearMonth employeeStartDate = YearMonth.parse(startDateStr, formatter);

                        JsonElement terminationDateElement = employeeObject.get("employment_termination_date");
                        YearMonth employeeTerminationDate = null;
                        if (terminationDateElement != null && !terminationDateElement.isJsonNull()) {
                            String terminationDateStr = terminationDateElement.getAsString();
                            employeeTerminationDate = YearMonth.parse(terminationDateStr, formatter);
                        }

                        // Update logic based on conditions
                        HashMap<String, Object> updateData = new HashMap<>();
                        String whereClause = "employee_id = ?";

                        if (employeeTerminationDate != null) {
                            if (!"terminated".equalsIgnoreCase(currentEmploymentStatus)) {
                                if (employeeTerminationDate.equals(activePeriodDate)) {
                                    updateData.put("employment_status", "leaving");
                                } else if (activePeriodDate.isAfter(employeeTerminationDate)) {
                                    updateData.put("employment_status", "terminated");
                                }
                            }
                        }
                        else if (employeeTerminationDate == null && employeeStartDate.isBefore(activePeriodDate)) {
                            updateData.put("employment_status", "active");
                        }
                        if (!updateData.isEmpty()) {
                            GenericQueries.update(connection, "employee", updateData, whereClause, new Object[]{employeeId});
                        }
                    }
                }
                return "Employment statuses updated successfully.";
            } else {
                return "No employee records found.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred updating employment statuses: " + e.getMessage();
        }
    }

    public static void updatePayments(Connection connection) {
            String[] columns = {"employee_id"};
            try {
                JsonArray result = GenericQueries.select(connection, "employee", columns); // Assuming a modified select method
                if (result.size() > 0) {
                    for (JsonElement element : result) {
                        if (element != null && element.isJsonObject()) {
                            JsonObject employeeObject = element.getAsJsonObject();
                            int employeeId = employeeObject.get("employee_id").getAsInt();
                            EmployeeEarningsController.updateEarnings(connection,employeeId);
                        }
                    }
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
