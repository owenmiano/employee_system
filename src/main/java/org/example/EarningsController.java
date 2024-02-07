package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class EarningsController {

    public static void findEarningTypes(Connection connection, HashMap<String, Object> earningsTypeData, String[] columns) {
        try {
            if (earningsTypeData == null || earningsTypeData.isEmpty()) {
                System.out.println("No earnings type data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : earningsTypeData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "earning_types",columns);
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //insert
    public static void createEarningsType(Connection connection, HashMap<String, Object> earningsTypeData) {
        if (earningsTypeData == null) {
            System.out.println("Earnings Type data is missing or incomplete.");
            return;
        }
        String earningsType = earningsTypeData.get("name") != null ? earningsTypeData.get("name").toString() : "";

        if (earningsType.trim().isEmpty()) {
            System.out.println("Earning type cannot be empty.");
            return;
        }


        boolean isInserted = GenericQueries.insertData(connection, "earning_types", earningsTypeData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Earning type added successfully");
        } else {
            System.out.println("Failed to add earning type");
        }
    }

    public static void updateEarningTypes(Connection connection, HashMap<String, Object> earningsTypeData,  int earningTypeID) {
        try {
            if (earningsTypeData == null || earningsTypeData.isEmpty()) {
                System.out.println("Earnings type data is missing or empty.");
                return;
            }
            String whereClause = "earning_types_id = ?";

            JsonObject result = GenericQueries.update(connection, "earning_types", earningsTypeData, whereClause,new Object[]{earningTypeID});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Earnings type info updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
