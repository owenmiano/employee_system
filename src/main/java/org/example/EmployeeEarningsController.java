package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class EmployeeEarningsController {
    public static void findEmployeeEarnings(Connection connection, int employeeEarningsID) {
        try {
            // Define the columns you want to fetch
            String[] columns = {
                    "ee.employee_earnings_id",
                    "et.type_name as earning_type",
                    "e.employee_name",
                    "ee.amount",
                    "p.period_start",
                    "p.period_end",
                    "ee.date_created",
                    "ee.date_modified"
            };

            // Adjust the joins to include tables related to earnings
            String[][] joins = {
                    {"INNER", "earning_types et", "ee.earning_types_id = et.earning_types_id"},
                    {"INNER", "employee e", "ee.employee_id = e.employee_id"},
                    {"INNER", "period p", "ee.period_id = p.period_id"}
            };

            // Where clause to filter by employeeEarningsID
            String whereClause = "ee.employee_earnings_id = ?";
            Object[] values = new Object[]{employeeEarningsID};

            // Execute the updated select method with multiple joins and a parameterized where clause
            JsonArray jsonArrayResult = GenericQueries.select(connection, "employee_earnings ee", joins, columns, whereClause, values);

            // Convert the result to JSON String and print
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }



    //insert
    public static void createEmployeeEarnings(Connection connection, HashMap<String, Object> employeeEarningsData) {
        if (employeeEarningsData == null) {
            System.out.println("Employee earnings Type data is missing or incomplete.");
            return;
        }

        boolean isInserted = GenericQueries.insertData(connection, "employee_earnings", employeeEarningsData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Employee earnings added successfully");
        } else {
            System.out.println("Failed to add employee earnings");
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
