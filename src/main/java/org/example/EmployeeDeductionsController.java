package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class EmployeeDeductionsController {
    public static float getEmployeeEarnings(Connection connection, int employeeId, int periodId) {
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

        // Calculate deductions only after successfully fetching earnings
        calculateDeductions(connection, employeeId, totalEarnings, periodId);
        return totalEarnings;
    }

    private static void calculateDeductions(Connection connection, int employeeId, float totalEarnings, int periodId) {
        float nhifDeduction = 500.0f;
        float nssfDeduction = 700.0f;
        boolean allDeductionsInserted = true;

        allDeductionsInserted &= insertDeduction(connection, employeeId, "NHIF", nhifDeduction, periodId);
        allDeductionsInserted &= insertDeduction(connection, employeeId, "NSSF", nssfDeduction, periodId);

        if (totalEarnings >= 25000) {
            float PAYE = 0.14f * totalEarnings;
            allDeductionsInserted &= insertDeduction(connection, employeeId, "PAYE", PAYE, periodId);
        }

        if (allDeductionsInserted) {
            System.out.println("Employee deductions added successfully");
        } else {
            System.out.println("One or more deductions failed for employee ID: " + employeeId);
        }
    }

    private static boolean insertDeduction(Connection connection, int employeeId, String deductionType, float amount, int periodId) {
        Integer deductionTypeId = DeductionsController.findDeductionType(connection, deductionType);
        if (deductionTypeId != null) {
            HashMap<String, Object> deductionData = new HashMap<>();
            deductionData.put("deduction_types_id", deductionTypeId);
            deductionData.put("employee_id", employeeId);
            deductionData.put("amount", amount);
            deductionData.put("period_id", periodId);

            return GenericQueries.insertData(connection, "employee_deductions", deductionData);
        } else {
            System.out.println(deductionType + " type not found for employee ID: " + employeeId);
            return false;
        }
    }


}
