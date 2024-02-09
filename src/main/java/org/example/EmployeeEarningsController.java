package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    public static void createEmployeeEarnings(Connection connection, HashMap<String, Object> employeeEarningsData) {
        if (employeeEarningsData == null) {
            System.out.println("Employee eanings data is missing or incomplete.");
            return;
        }


        Integer amountInt = (Integer) employeeEarningsData.get("amount");
        float amount = amountInt.floatValue();
        int periodId = (Integer) employeeEarningsData.get("period_id");
//        int earningTypeId = (Integer) employeeEarningsData.get("earning_types_id");
        Integer employeeId = (Integer) employeeEarningsData.get("employee_id");
        if (employeeId == null) {
            System.out.println("Employee ID is missing.");
            return;
        }

        // Check if the employee's employment_status is 'terminated'
        if (isEmployeeTerminated(connection, employeeId)) {
            System.out.println("Cannot add salary for a terminated employee.");
            return;
        }
        // Check if the employee has been working for at least 3 months
        if (hasWorkedForThreeMonths(connection, employeeId)) {
            // Calculate and add allowances based on the provided basic salary
            calculateAndAddAllowances(connection, employeeId, amount, periodId);
        }

        boolean isInserted = GenericQueries.insertData(connection, "employee_earnings", employeeEarningsData);
        if (isInserted) {
            System.out.println("Employee earnings added successfully");
        } else {
            System.out.println("Failed to add employee salary");
        }
    }
    private static boolean isEmployeeTerminated(Connection connection, int employeeId) {
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

//    public static float fetchLastSalaryForEmployee(Connection connection, int employeeId, int earningTypeId) {
//
//
//
//    }


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



    private static void calculateAndAddAllowances(Connection connection, int employeeId, double basicSalary, int periodId) {
        final double HOUSE_ALLOWANCE_RATE = 0.03;
        final double TRANSPORT_ALLOWANCE_RATE = 0.015;
        final double MORTGAGE_ALLOWANCE_RATE = 0.02;

        // Calculate allowances
        double houseAllowance = basicSalary * HOUSE_ALLOWANCE_RATE;
        double transportAllowance = basicSalary * TRANSPORT_ALLOWANCE_RATE;
        double mortgageAllowance = basicSalary * MORTGAGE_ALLOWANCE_RATE;

        // Insert allowances
        insertAllowance(connection, employeeId, 2, houseAllowance, periodId); // House allowance
        insertAllowance(connection, employeeId, 3, transportAllowance, periodId); // Transport allowance
        insertAllowance(connection, employeeId, 4, mortgageAllowance, periodId); // Mortgage allowance
    }

    private static void insertAllowance(Connection connection, int employeeId, int earningTypeId, double amount, int periodId) {
        HashMap<String, Object> allowanceData = new HashMap<>();
        allowanceData.put("earning_types_id", earningTypeId);
        allowanceData.put("employee_id", employeeId);
        allowanceData.put("amount", amount);
        allowanceData.put("period_id", periodId);
        boolean isInserted = GenericQueries.insertData(connection, "employee_earnings", allowanceData);

        if (isInserted) {
            System.out.println("Allowance inserted successfully for employee ID " + employeeId + ", earning type ID " + earningTypeId);
        } else {
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
