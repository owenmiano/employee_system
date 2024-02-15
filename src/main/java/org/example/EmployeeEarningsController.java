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
    public static void createEmployeeEarnings(Connection connection, int employeeId,float amount) {
        try {
            HashMap<String, Object> employeeEarningsData = new HashMap<>();
            Integer earningTypeId = EarningsController.findEarningType(connection, "Basic Salary");
            if (earningTypeId == null) {
                System.out.println("Basic Salary earning type not found.");
                return;
            }

            Map<String, String> periodInfo = PeriodController.fetchActivePeriod(connection);
            if (periodInfo == null || periodInfo.isEmpty()) {
                System.out.println("Active period not found.");
                return;
            }
            int periodId = Integer.parseInt(periodInfo.get("period_id"));

            employeeEarningsData.put("earning_types_id", earningTypeId);
            employeeEarningsData.put("employee_id", employeeId);
            employeeEarningsData.put("amount", amount);
            employeeEarningsData.put("period_id", periodId);

            if (EmployeeEarningsController.isEmployeeTerminated(connection, employeeId)) {
                return;
            }

            Integer lastPaidPeriodId = fetchLastPaidPeriodId(connection, employeeId, earningTypeId);
            if (lastPaidPeriodId != null) {
                // If here, it means the employee already has an earnings record
                String periodName = PeriodController.fetchPeriod(connection, periodId);
                Map<String, String> employeeInfo = EmployeeController.findEmployee(connection, employeeId);
                if (employeeInfo != null && !employeeInfo.isEmpty()) {
                    String employeeName = employeeInfo.get("employee_name");

                    System.out.println("Earnings record already exists for employee: " + employeeName + " in this period: " + periodName);
                  return;
                }
            }

            boolean isInserted = GenericQueries.insertData(connection, "employee_earnings", employeeEarningsData);
            if (!isInserted) {
                System.out.println("Failed to insert employee earnings.");
                return;
            }

            if (EmployeeEarningsController.hasWorkedForThreeMonths(connection, employeeId)) {
                // Calculate and add allowances based on the provided basic salary

                calculateAndAddAllowances(connection, employeeId, amount, periodId);
            }

            if (isInserted) {
                System.out.println("Employee earnings added successfully");
                EmployeeDeductionsController.calculateDeductions(connection, employeeId, periodId);
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updateEarnings(Connection connection, int employeeId) {
        try {
            HashMap<String, Object> employeeEarningsData = new HashMap<>();
            Integer earningTypeId = EarningsController.findEarningType(connection, "Basic Salary");
            if (earningTypeId == null) {
                System.out.println("Basic Salary earning type not found.");
                return;
            }

            Map<String, String> periodInfo = PeriodController.fetchActivePeriod(connection);
            if (periodInfo == null || periodInfo.isEmpty()) {
                System.out.println("Active period not found.");
                return;
            }
            int periodId = Integer.parseInt(periodInfo.get("period_id"));

            employeeEarningsData.put("earning_types_id", earningTypeId);
            employeeEarningsData.put("employee_id", employeeId);
            employeeEarningsData.put("period_id", periodId);

            if (EmployeeEarningsController.isEmployeeTerminated(connection, employeeId)) {
                return;
            }

            Integer lastPaidPeriodId = fetchLastPaidPeriodId(connection, employeeId, earningTypeId);
            if (lastPaidPeriodId != null) {
                float lastSalary = EmployeeEarningsController.fetchLastSalaryForEmployee(connection, employeeId, earningTypeId, lastPaidPeriodId);

                    float newSalary = lastSalary * 1.02f; // Apply the 2% salary increase
                    employeeEarningsData.put("amount", newSalary);

            }else {

                        Map<String, String> employeeInfo = EmployeeController.findEmployee(connection, employeeId);
                        if (employeeInfo != null && !employeeInfo.isEmpty()) {
                            String employeeName = employeeInfo.get("employee_name");
                            String employeeNumber = employeeInfo.get("employee_number");
                            System.out.println("Please initialize payment for employee: " + employeeName + " | Employee Number: " + employeeNumber);

                        }

            }

            boolean isUpdated = GenericQueries.insertData(connection, "employee_earnings", employeeEarningsData);
            if (!isUpdated) {
                System.out.println("Failed to Update employee earnings.");
                return;
            }

            if (EmployeeEarningsController.hasWorkedForThreeMonths(connection, employeeId)) {
                // Calculate and add allowances based on the provided basic salary
                Number amountNumber = (Number) employeeEarningsData.get("amount");
                float amount = amountNumber.floatValue();
                calculateAndAddAllowances(connection, employeeId, amount, periodId);
            }

            if (isUpdated) {
                System.out.println("Employee earnings updated successfully");
                EmployeeDeductionsController.calculateDeductions(connection, employeeId, periodId);
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
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

        return activePeriodDate.isAfter(terminationDate);
    }
    public static Integer fetchLastPaidPeriodId(Connection connection, int employeeId, int earningTypeId) {
        try {
            String whereClause = "employee_id = ? AND earning_types_id = ?";
            Object[] params = new Object[]{employeeId, earningTypeId};
            String orderByClause = "period_id DESC";
            String[] columns = {"period_id"};
            int limitClause = 1;

            JsonArray result = GenericQueries.select(connection, "employee_earnings",columns,whereClause,orderByClause,limitClause,params);
            if (result.size() > 0) {
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject record = firstElement.getAsJsonObject();
                    return record.get("period_id").getAsInt();
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred while fetching the last paid period ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Return null if no record is found or in case of an error
    }

    public static float fetchLastSalaryForEmployee(Connection connection, int employeeId, int earningTypeId, int periodId) {
        String[] columns = {"amount"};
        String whereClause = "employee_id = ? AND earning_types_id = ? AND period_id = ?";
        Object[] params = new Object[]{employeeId, earningTypeId, periodId};

        try {
            JsonArray result = GenericQueries.select(connection, "employee_earnings", columns, whereClause, params);
            if (result.size() > 0) {
                JsonElement firstElement = result.get(0);
                if (firstElement != null && firstElement.isJsonObject()) {
                    JsonObject earningsObject = firstElement.getAsJsonObject();
                    return earningsObject.get("amount").getAsFloat(); // Return the amount if found
                }
            } else {
                System.out.println("No earnings record found for the specified parameters.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred fetching the last salary: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // Default return for no record found or in case of an error
    }




    public static boolean hasWorkedForThreeMonths(Connection connection, int employeeId) {
        String startDateStr = EmployeeController.fetchEmployeeStartDate(connection, employeeId);
        Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
        String period = activePeriodInfo.get("period");

        if (startDateStr == null || period == null) {
            System.out.println("Start date or active period is missing.");
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth startDate = YearMonth.parse(startDateStr, formatter);
        YearMonth activePeriodDate = YearMonth.parse(period, formatter);

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

    public static float getTotalEmployeeEarningsById(Connection connection, int employeeId, int periodId) {
        float totalEarnings = 0.0f;
        String whereClause = "period_id = ? AND employee_id = ?";
        Object[] params = new Object[]{periodId, employeeId};

        try {
            JsonArray earningsRecords = GenericQueries.select(connection, "employee_earnings", new String[]{"amount"}, whereClause, params);
            for (JsonElement element : earningsRecords) {
                if (element != null && element.isJsonObject()) {
                    JsonObject earningsObject = element.getAsJsonObject();
                    if (earningsObject.has("amount")) {
                        totalEarnings += earningsObject.get("amount").getAsFloat();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching employee earnings: " + e.getMessage());
        }

        return totalEarnings;
    }
}
