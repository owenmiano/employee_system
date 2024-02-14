package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PeriodController {

    //insert
    public static void createPeriod(Connection connection, HashMap<String, Object> periodData) {
        if (periodData == null) {
            System.out.println("Period data is missing or incomplete.");
            return;
        }
        String earningsType = periodData.get("period") != null ? periodData.get("period").toString() : "";

        if (earningsType.trim().isEmpty()) {
            System.out.println("Period cannot be empty.");
            return;
        }


        boolean isInserted = GenericQueries.insertData(connection, "period", periodData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Period added successfully");
        } else {
            System.out.println("Failed to add period");
        }
    }

    public static void updatePeriod(Connection connection, HashMap<String, Object> periodData, int periodID) {
        try {
            if (periodData == null || periodData.isEmpty()) {
                System.out.println("Period data is missing or empty.");
                return;
            }
            String whereClause = "period_id = ?";

            JsonObject result = GenericQueries.update(connection, "period", periodData, whereClause, new Object[]{periodID});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Period info updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, String> fetchActivePeriod(Connection connection) {
        String[] columns = {"period", "period_id"};
        String whereClause = "status = ?";
        List<String> values = Collections.singletonList("Current");
        Map<String, String> periodInfo = new HashMap<>();

        try {
            JsonArray result = GenericQueries.select(connection, "period", columns, whereClause, values.toArray());
            if (result.size() > 0) {
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject periodObject = firstElement.getAsJsonObject();
                    String period = periodObject.get("period").getAsString();
                    String periodId = periodObject.get("period_id").getAsString();

                    periodInfo.put("period", period);
                    periodInfo.put("period_id", periodId);
                    return periodInfo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred fetching the active period: " + e.getMessage());
        }

        return null; // Return null if no active period is found or in case of an error
    }

    public static String fetchPeriod(Connection connection,int periodId) {

        try {
            String[] columns = {"period"};
            String whereClause = "period_id = ?";
            ArrayList<Object> values = new ArrayList<>();
            values.add(periodId);
            JsonArray result = GenericQueries.select(connection, "period", columns, whereClause, values.toArray());
            if (result.size() > 0) {
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject periodObject = firstElement.getAsJsonObject();
                    return periodObject.get("period").getAsString();

                }
            }
            else {
                System.out.println("No results found for the specified period id.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred fetching the declared period id: " + e.getMessage());
        }

        return null;
    }
}