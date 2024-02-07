package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DeductionsController {
    public static void findDeductionTypes(Connection connection, HashMap<String, Object> deductionTypeData, String[] columns) {
        try {
            if (deductionTypeData == null || deductionTypeData.isEmpty()) {
                System.out.println("No deduction type data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : deductionTypeData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "deduction_types",columns);
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //insert
    public static void createDeductionTypes(Connection connection, HashMap<String, Object> deductionTypeData) {
        if (deductionTypeData == null) {
            System.out.println("Deductions Type data is missing or incomplete.");
            return;
        }
        String earningsType = deductionTypeData.get("name") != null ? deductionTypeData.get("name").toString() : "";

        if (earningsType.trim().isEmpty()) {
            System.out.println("Deduction type cannot be empty.");
            return;
        }


        boolean isInserted = GenericQueries.insertData(connection, "deduction_types", deductionTypeData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Deduction type added successfully");
        } else {
            System.out.println("Failed to add deduction type");
        }
    }

    public static void updateDeductionTypes(Connection connection, HashMap<String, Object> deductionTypeData,  int earningTypeID) {
        try {
            if (deductionTypeData == null || deductionTypeData.isEmpty()) {
                System.out.println("Deduction type data is missing or empty.");
                return;
            }
            String whereClause = "deduction_types_id = ?";

            JsonObject result = GenericQueries.update(connection, "deduction_types", deductionTypeData, whereClause,new Object[]{earningTypeID});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Deduction type info updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
