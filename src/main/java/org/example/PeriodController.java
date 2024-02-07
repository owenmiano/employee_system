package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class PeriodController {
    public static void findPeriod(Connection connection, HashMap<String, Object> periodData, String[] columns) {
        try {
            if (periodData == null || periodData.isEmpty()) {
                System.out.println("No period data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : periodData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "period",columns);
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


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

    public static void updatePeriod(Connection connection, HashMap<String, Object> periodData,  int periodID) {
        try {
            if (periodData == null || periodData.isEmpty()) {
                System.out.println("Period data is missing or empty.");
                return;
            }
            String whereClause = "period_id = ?";

            JsonObject result = GenericQueries.update(connection, "period", periodData, whereClause,new Object[]{periodID});

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
}
