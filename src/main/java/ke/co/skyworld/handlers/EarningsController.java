//package org.co.skyworld.handlers;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import ke.co.skyworld.queryBuilder.GenericQueries;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//public class EarningsController {
//
//    public static Integer findEarningType(Connection connection, String earningType) {
//        try {
//            // Assuming 'type_description' is the column name for the earning type description
//            String whereClause = "name = ?";
//            ArrayList<Object> values = new ArrayList<>();
//            values.add(earningType); // Add the provided earning type description to the values
//
//            // Execute the query
//            JsonArray jsonArrayResult = GenericQueries.select(connection, "earning_types", whereClause, values.toArray());
//            if (jsonArrayResult.size() > 0) {
//                JsonElement firstElement = jsonArrayResult.get(0);
//                if (firstElement != null && firstElement.isJsonObject()) {
//                    JsonObject earningTypeObject = firstElement.getAsJsonObject();
//                    // Assuming 'earning_types_id' is the correct column name for the ID
//                    return earningTypeObject.get("earning_types_id").getAsInt();
//
//                }
//            }
//            else {
//                System.out.println("No results found for the specified earning type description.");
//            }
//        } catch (Exception e) {
//            System.out.println("An error occurred: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return null; // Return null if the ID is not found or an error occurs
//    }
//
//
//
//
//    //insert
//    public static void createEarningsType(Connection connection, HashMap<String, Object> earningsTypeData) {
//        if (earningsTypeData == null) {
//            System.out.println("Earnings Type data is missing or incomplete.");
//            return;
//        }
//        String earningsType = earningsTypeData.get("name") != null ? earningsTypeData.get("name").toString() : "";
//
//        if (earningsType.trim().isEmpty()) {
//            System.out.println("Earning type cannot be empty.");
//            return;
//        }
//
//
//        boolean isInserted = GenericQueries.insertData(connection, "earning_types", earningsTypeData); // Replace "class" with your actual table name
//
//
//        if (isInserted) {
//            System.out.println("Earning type added successfully");
//        } else {
//            System.out.println("Failed to add earning type");
//        }
//    }
//
//    public static void updateEarningTypes(Connection connection, HashMap<String, Object> earningsTypeData,  int earningTypeID) {
//        try {
//            if (earningsTypeData == null || earningsTypeData.isEmpty()) {
//                System.out.println("Earnings type data is missing or empty.");
//                return;
//            }
//            String whereClause = "earning_types_id = ?";
//
//            JsonObject result = GenericQueries.update(connection, "earning_types", earningsTypeData, whereClause,new Object[]{earningTypeID});
//
//            if (result.get("success").getAsBoolean()) {
//                System.out.println("Earnings type info updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
//            } else {
//                System.out.println("No rows were updated.");
//            }
//        } catch (SQLException e) {
//            System.out.println("An error occurred: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//}
