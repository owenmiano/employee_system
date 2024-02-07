package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class CompanyController {

    public static void findCompany(Connection connection, HashMap<String, Object> companyData, String[] columns) {
        try {
            if (companyData == null || companyData.isEmpty()) {
                System.out.println("No company data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : companyData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "company",columns);
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //insert
    public static void createCompany(Connection connection, HashMap<String, Object> companyData) {
        if (companyData == null) {
            System.out.println("Company data is missing or incomplete.");
            return;
        }
        String companyName = companyData.get("name") != null ? companyData.get("name").toString() : "";

        if (companyName.trim().isEmpty()) {
            System.out.println("Company name cannot be empty.");
            return;
        }


        boolean isInserted = GenericQueries.insertData(connection, "company", companyData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Company added successfully");
        } else {
            System.out.println("Failed to add company");
        }
    }

    public static void updateCompany(Connection connection, HashMap<String, Object> companyData,  int companyID) {
        try {
            if (companyData == null || companyData.isEmpty()) {
                System.out.println("Company data is missing or empty.");
                return;
            }
            String whereClause = "company_id = ?";

            JsonObject result = GenericQueries.update(connection, "company", companyData, whereClause,new Object[]{companyID});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Company info updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
