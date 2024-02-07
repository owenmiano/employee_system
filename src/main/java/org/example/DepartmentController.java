package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DepartmentController {

    public static void findDepartment(Connection connection, HashMap<String, Object> departmentData, String[] columns) {
        try {
            if (departmentData == null || departmentData.isEmpty()) {
                System.out.println("No department data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : departmentData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "department",columns);
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //insert
    public static void createDepartment(Connection connection, HashMap<String, Object> departmentData) {
        if (departmentData == null) {
            System.out.println("Department data is missing or incomplete.");
            return;
        }
        String departmentName = departmentData.get("name") != null ? departmentData.get("name").toString() : "";

        if (departmentName.trim().isEmpty()) {
            System.out.println("Department name cannot be empty.");
            return;
        }


        boolean isInserted = GenericQueries.insertData(connection, "department", departmentData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Department added successfully");
        } else {
            System.out.println("Failed to add department");
        }
    }

    //update department
    public static void updateDepartment(Connection connection, HashMap<String, Object> departmentData,  int departmentID) {
        try {
            if (departmentData == null || departmentData.isEmpty()) {
                System.out.println("Department data is missing or empty.");
                return;
            }
            String whereClause = "department_id = ?";

            JsonObject result = GenericQueries.update(connection, "department", departmentData, whereClause,new Object[]{departmentID});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Department info updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
