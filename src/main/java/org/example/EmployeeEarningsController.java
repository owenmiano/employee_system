package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

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
//
    public static void createEmployeeEarnings(Connection connection, int employeeId) {
        boolean shouldCreateEarnings = true;
        HashMap<String, Object> employeeEarningsData = new HashMap<>();
        Integer earningTypeId = EarningsController.findEarningType(connection, "Basic Salary");
        Map<String, String> periodInfo = PeriodController.fetchActivePeriod(connection);
        int periodId = Integer.parseInt(periodInfo.get("period_id"));
        employeeEarningsData.put("earning_types_id", earningTypeId);
        employeeEarningsData.put("employee_id", employeeId);
        employeeEarningsData.put("period_id", periodId);

        try {
            // Disable auto-commit to manually manage transactions
            connection.setAutoCommit(false);

            if (EmployeeEarningsController.isEmployeeTerminated(connection, employeeId)) {
                System.out.println("Cannot add salary for a terminated employee.");
                shouldCreateEarnings = false;
            }

            // Proceed only if the employee is not terminated
            if (shouldCreateEarnings && EmployeeEarningsController.checkForExistingEarningsRecord(connection, employeeId, earningTypeId)) {
                float lastSalary = EmployeeEarningsController.fetchLastSalaryForEmployee(connection, employeeId, earningTypeId);
                if (lastSalary > 0) {
                    System.out.println("Last salary: " + lastSalary);
                    float newSalary = lastSalary * 1.02f; // Apply the increase
                    System.out.println("Updated salary: " + newSalary);
                    employeeEarningsData.put("amount", newSalary);
                } else {
                    System.out.println("Last salary not greater than 0. Aborting operation.");
                    shouldCreateEarnings = false; // Do not proceed with creating earnings
                }
            } else {
                employeeEarningsData.put("amount", 50000); // Example default amount
            }

            boolean isInserted = false;
            // Only insert data if shouldCreateEarnings is true
            if (shouldCreateEarnings) {
                isInserted = GenericQueries.insertData(connection, "employee_earnings", employeeEarningsData);
                if (!isInserted) {
                    throw new SQLException("Failed to insert employee earnings.");
                }

                if (EmployeeEarningsController.hasWorkedForThreeMonths(connection, employeeId)) {
                    // Calculate and add allowances based on the provided basic salary
                    Number amountNumber = (Number) employeeEarningsData.get("amount");
                    float amount = amountNumber.floatValue();
                    calculateAndAddAllowances(connection, employeeId, amount, periodId);
                }
            }

            // Commit transaction if all operations were successful
            connection.commit();
            if (isInserted) {
                System.out.println("Employee earnings added successfully");
                // Trigger the getEmployeeEarnings method after successful earnings addition
                EmployeeDeductionsController.getEmployeeEarnings(connection, employeeId, periodId);
            }
        } catch (Exception e) {
            System.out.println("An error occurred here: " + e.getMessage());
            try {
                connection.rollback();
                System.out.println("Transaction is rolled back.");
            } catch (SQLException ex) {
                System.out.println("Error during transaction rollback: " + ex.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException e) {
                System.out.println("Error restoring auto-commit mode: " + e.getMessage());
            }
        }
    }

    public static boolean isEmployeeTerminated(Connection connection, int employeeId) {
        Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
        String period = activePeriodInfo.get("period");
        String termination = EmployeeController.fetchEmployeeTermnationDate(connection,employeeId);

        if (termination == null) {
            return false; // Employee is not terminated
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth terminationDate = YearMonth.parse(termination, formatter);
        YearMonth activePeriodDate = YearMonth.parse(period, formatter);

        // Check if the employment start date is greater than the active period
        return !terminationDate.isAfter(activePeriodDate);
    }
    public static boolean checkForExistingEarningsRecord(Connection connection, int employeeId, int earningTypeId) {
        try {
            String whereClause = "earning_types_id = ? AND employee_id = ?";
            Object[] params = new Object[]{earningTypeId, employeeId};
            JsonArray answersReport = GenericQueries.select(connection, "employee_earnings", whereClause, params);

            // Assuming the JsonArray is null or empty when no records are found
            return answersReport != null && answersReport.size() > 0;
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            // Return false if an exception occurs, indicating no record found or an error in the process
            return false;
        }
    }

    public static float fetchLastSalaryForEmployee(Connection connection, int employeeId, int earningTypeId) {
        String[] columns = {"amount"};
        Integer lastActivePeriod = PeriodController.fetchLastPeriodID(connection);
        if (lastActivePeriod == null) {
            System.out.println("Could not retrieve the last active period.");
            return 0; // No period found
        }

        String whereClause = "employee_id = ? AND earning_types_id = ? AND period_id = ?";
        Object[] params = new Object[]{employeeId, earningTypeId, lastActivePeriod};

        try {
            JsonArray result = GenericQueries.select(connection, "employee_earnings", columns, whereClause, params);
            if (result.size() > 0) {
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject earningsObject = firstElement.getAsJsonObject();
                    return earningsObject.get("amount").getAsFloat(); // Return amount if found
                }
            }
            System.out.println("No earnings record found for the specified parameters.");
        } catch (SQLException e) {
            System.out.println("An error occurred fetching the last salary: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // Default return for no record found or error
    }



    public static boolean hasWorkedForThreeMonths(Connection connection, int employeeId) {
        // Assume these methods return dates in String format like "yyyy-MM-dd"
        String startDateStr = EmployeeController.fetchEmployeeStartDate(connection, employeeId);
        Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
        String period = activePeriodInfo.get("period");

        // Adjust based on your actual method

        if (startDateStr == null || period == null) {
            System.out.println("Start date or active period is missing.");
            return false;
        }

        // Assuming startDateStr and activePeriodStr are actually in "yyyy-MM" format based on the initial question
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth startDate = YearMonth.parse(startDateStr, formatter);
        YearMonth activePeriodDate = YearMonth.parse(period, formatter);

        // Calculate the difference between the start date and the active period date
        long monthsBetween = ChronoUnit.MONTHS.between(startDate, activePeriodDate);

        // Check if the difference is at least three months
        return monthsBetween >= 3;
    }


    private static void calculateAndAddAllowances(Connection connection, int employeeId, float basicSalary, int periodId) {
        Map<String, Float> allowanceRates = new HashMap<>();
        allowanceRates.put("Housing Allowance", 0.03f);
        allowanceRates.put("Transport Allowance", 0.015f);
        allowanceRates.put("Mortgage Allowance", 0.02f);

        // Iterate through each allowance type
        for (Map.Entry<String, Float> allowanceEntry : allowanceRates.entrySet()) {
            String allowanceDescription = allowanceEntry.getKey();
            float rate = allowanceEntry.getValue();

            // Calculate allowance amount
            float allowanceAmount = basicSalary * rate;

            // Dynamically get the earning type ID based on description
            Integer earningTypeId = EarningsController.findEarningType(connection, allowanceDescription);

            // Check if the ID was found and insert allowances
            if (earningTypeId != null) {
                insertAllowance(connection, employeeId, earningTypeId, allowanceAmount, periodId);
            } else {
                System.out.println(allowanceDescription + " type not found.");
            }
        }
    }



    private static void insertAllowance(Connection connection, int employeeId, int earningTypeId, double amount, int periodId) {
        HashMap<String, Object> allowanceData = new HashMap<>();
        allowanceData.put("earning_types_id", earningTypeId);
        allowanceData.put("employee_id", employeeId);
        allowanceData.put("amount", amount);
        allowanceData.put("period_id", periodId);
        boolean isInserted = GenericQueries.insertData(connection, "employee_earnings", allowanceData);

        if (!isInserted) {
            System.out.println("Failed to insert allowance for employee ID " + employeeId + ", earning type ID " + earningTypeId);
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
