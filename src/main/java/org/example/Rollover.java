package org.example;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rollover {
    public static void rollover(Connection connection) {
        updateNewEmployeesToActive(connection);
        updateLeavingEmployeesToTerminated(connection);
        closeCurrentPeriodAndOpenNew(connection);
    }

    private static String updateNewEmployeesToActive(Connection connection) {
        Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
        String period = activePeriodInfo.get("period");
        String[] columns = {"employment_start_date"};
        String whereClause = "employment_status = ?";
        List<String> values = Collections.singletonList("new");
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
            System.out.println("An error occurred fetching the active period: " + e.getMessage());
        }
        return null;
    }

    private static void updateLeavingEmployeesToTerminated(Connection connection) {
        String sql = "UPDATE employees SET employment_status = 'terminated' WHERE employment_status = 'leaving' AND start_date <= CURRENT_DATE - INTERVAL '1' MONTH";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int affectedRows = stmt.executeUpdate();
            System.out.println("Updated " + affectedRows + " 'leaving' employees to 'terminated'.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void closeCurrentPeriodAndOpenNew(Connection connection) {
        // Close current period
        String closeSql = "UPDATE periods SET status = 'closed' WHERE status = 'current'";
        try (PreparedStatement closeStmt = connection.prepareStatement(closeSql)) {
            int closedRows = closeStmt.executeUpdate();
            System.out.println("Closed current period. Rows affected: " + closedRows);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Open new period one month later
        YearMonth nextPeriodMonth = YearMonth.now().plusMonths(1);
        LocalDate nextPeriodStart = nextPeriodMonth.atDay(1);
        LocalDate nextPeriodEnd = nextPeriodMonth.atEndOfMonth();

        String openSql = "INSERT INTO periods (status, start_date, end_date) VALUES ('current', ?, ?)";
        try (PreparedStatement openStmt = connection.prepareStatement(openSql)) {
            openStmt.setDate(1, java.sql.Date.valueOf(nextPeriodStart));
            openStmt.setDate(2, java.sql.Date.valueOf(nextPeriodEnd));
            int openedRows = openStmt.executeUpdate();
            System.out.println("Opened new period. Rows affected: " + openedRows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
