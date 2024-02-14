package org.example;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Reports {
    //Display all the newly created employees grouped by the different departments.
    public static void displayNewEmployees(Connection connection) {
        try {
            String[] columns = {
                    "e.employee_name",
                    "e.employee_number",
                    "e.employment_start_date",
                    "e.employee_position",
                    "e.gender",
                    "c.name AS company_name",
                    "d.name AS department_name"
            };

            String[][] joins = {
                    {"INNER", "company c", "e.company_id = c.company_id"},
                    {"INNER", "department d", "e.department_id = d.department_id"}
            };

            String whereClause = "e.employment_status = ?";
            String groupByClause = "d.name,e.employee_number";
            List<String> values = Collections.singletonList("new");

            JsonArray jsonArrayResult = GenericQueries.select(connection, "employee e", joins, columns, whereClause,groupByClause, values.toArray());

            // Process the results to group by department
            Map<String, JsonArray> groupedByDepartment = new HashMap<>();
            for (JsonElement element : jsonArrayResult) {
                JsonObject employee = element.getAsJsonObject();
                String departmentName = employee.get("department_name").getAsString();

                groupedByDepartment.computeIfAbsent(departmentName, k -> new JsonArray()).add(employee);
            }

            // Convert the grouped map to a JsonArray for output
            JsonArray outputArray = new JsonArray();
            for (Map.Entry<String, JsonArray> entry : groupedByDepartment.entrySet()) {
                JsonObject departmentObject = new JsonObject();
                departmentObject.addProperty("department_name", entry.getKey());
                departmentObject.add("employees", entry.getValue());
                outputArray.add(departmentObject);
            }

            // Convert the result to JSON String and print
            String jsonResult = outputArray.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Display the total number of active employees in a department.
    public static void displayActiveEmployeeCountsByDepartment(Connection connection, int departmentId) {
        try {
            // Define the SQL columns and aggregation function
            String[] columns = {
                    "d.name AS department_name",
                    "COUNT(e.employee_id) AS active_employees"
            };

            // Define the joins
            String[][] joins = {
                    {"INNER", "company c", "e.company_id = c.company_id"},
                    {"INNER", "department d", "e.department_id = d.department_id"}
            };

            // Adjust the where clause to filter by department ID
            String whereClause = "e.employment_status = ? AND d.department_id = ?";
            List<Object> values = new ArrayList<>();
            values.add("active");
            values.add(departmentId);

            // Execute the select method with aggregation
            JsonArray jsonArrayResult = GenericQueries.select(connection, "employee e", joins, columns, whereClause, values.toArray());

            // Process the result to print or return
            if (jsonArrayResult.size() > 0) {
                String jsonResult = jsonArrayResult.toString();
                System.out.println(jsonResult);
            } else {
                System.out.println("No active employees found for the specified department.");
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

     //Generate a report on the total earnings of an employee and total deductions as well as their net pay.
    public static void generateEmployeeEarningsAndDeductionsReport(Connection connection, int employeeId,int periodId) {
        try {
            // Fetch total earnings for the employee
            float totalEarnings = EmployeeEarningsController.getTotalEmployeeEarningsById(connection, employeeId,periodId);

            // Fetch total deductions for the employee
            float totalDeductions = EmployeeDeductionsController.getTotalDeductionsForEmployee(connection, employeeId,periodId);

            // Calculate net pay
            float netPay = totalEarnings - totalDeductions;

            Map<String, String> employeeInfo = EmployeeController.findEmployee(connection, employeeId);
             String employeeName=employeeInfo.get("employee_name");
             String employeeNumber=employeeInfo.get("employee_number");

            String periodName = PeriodController.fetchPeriod(connection, periodId);

            // Display or return the report data
            JsonObject reportObject = new JsonObject();
            reportObject.addProperty("employee_name", employeeName);
            reportObject.addProperty("employee_number", employeeNumber);
            reportObject.addProperty("period", periodName);
            reportObject.addProperty("total_earnings", totalEarnings);
            reportObject.addProperty("total_deductions", totalDeductions);
            reportObject.addProperty("net_pay", netPay);

            // Add the object to a JSON array
            JsonArray reportArray = new JsonArray();
            reportArray.add(reportObject);

            // Convert the array to a JSON string and print
            System.out.println(reportArray.toString());
        } catch (Exception e) {
            System.out.println("An error occurred while generating the report: " + e.getMessage());
            e.printStackTrace();
        }
    }

//Generate a report on the total allowances and net salaries of each employee in a department.
public static void generateEmployeeAllowancesAndNetSalariesReport(Connection connection, int departmentId, int periodId) {
    try {
        Integer basicSalaryId = EarningsController.findEarningType(connection, "Basic Salary");
        String[] columns = {"e.employee_id", "e.employee_name","e.employee_number","e.employee_position", "e.employment_status","e.employment_start_date","d.name AS department_name"};

        String[][] joins = {
                {"INNER", "department d", "e.department_id = d.department_id"}
        };

        String whereClause = "e.department_id = ?";
        String groupByClause = "d.name,e.employee_number";
        Object[] params = new Object[]{departmentId};

        // Adjust the select method call to include joins
        JsonArray employeesResult = GenericQueries.select(connection, "employee e", joins, columns, whereClause,groupByClause, params);

        Map<String, JsonArray> groupedByDepartment = new HashMap<>();

        for (JsonElement employeeElement : employeesResult) {
            if (employeeElement != null && employeeElement.isJsonObject()) {
                JsonObject employeeObject = employeeElement.getAsJsonObject();
                int employeeId = employeeObject.get("employee_id").getAsInt();
                String employeeName = employeeObject.get("employee_name").getAsString();
                String employeeNumber = employeeObject.get("employee_number").getAsString();
                String employmentStartDate = employeeObject.get("employment_start_date").getAsString();
                String employmentStatus = employeeObject.get("employment_status").getAsString();
                String employeePosition = employeeObject.get("employee_position").getAsString();
                // Fetch the department name from the employeeObject
                String departmentName = employeeObject.get("department_name").getAsString();

                float totalAllowances = getTotalAllowances(connection, employeeId, periodId,basicSalaryId);
                float salary = getNetSalary(connection, employeeId, periodId,basicSalaryId);
                String periodName = PeriodController.fetchPeriod(connection, periodId);

                JsonObject reportObject = new JsonObject();
                reportObject.addProperty("employee_id", employeeId);
                reportObject.addProperty("employee_name", employeeName);
                reportObject.addProperty("employee_number", employeeNumber);
                reportObject.addProperty("employee_start_date", employmentStartDate);
                reportObject.addProperty("employee_position", employeePosition);
                reportObject.addProperty("employee_status", employmentStatus);
                reportObject.addProperty("period", periodName);
                reportObject.addProperty("total_allowances", totalAllowances);
                reportObject.addProperty("net_salary", salary);

                // Group the report object by department
                groupedByDepartment.computeIfAbsent(departmentName, k -> new JsonArray()).add(reportObject);
            }
        }

        // Convert the grouped map to a JsonArray for output
        JsonArray outputArray = new JsonArray();
        for (Map.Entry<String, JsonArray> entry : groupedByDepartment.entrySet()) {
            JsonObject departmentObject = new JsonObject();
            departmentObject.addProperty("department_name", entry.getKey());
            departmentObject.add("employees", entry.getValue());
            outputArray.add(departmentObject);
        }

        // Print the grouped report
        System.out.println(outputArray.toString());
    } catch (Exception e) {
        System.out.println("An error occurred while generating the report: " + e.getMessage());
        e.printStackTrace();
    }
}

    public static float getTotalAllowances(Connection connection, int employeeId, int periodId, int basicSalaryEarningTypeId) {
        float totalAllowances = 0.0f;
        String[] columns = {"amount"};
        String whereClause = " employee_id = ? AND period_id = ? AND earning_types_id != ?";
        Object[] params = new Object[]{employeeId, periodId, basicSalaryEarningTypeId};

        try {
            JsonArray earningsRecords = GenericQueries.select(connection, "employee_earnings", columns, whereClause, params);

            for (JsonElement element : earningsRecords) {
                if (element != null && element.isJsonObject()) {
                    JsonObject earningsObject = element.getAsJsonObject();
                    if (earningsObject.has("amount")) {
                        float amount = earningsObject.get("amount").getAsFloat();
                        totalAllowances += amount;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching employee total allowances: " + e.getMessage());
        }

        return totalAllowances;
    }


    public static float getNetSalary(Connection connection, int employeeId, int periodId, int basicSalaryEarningTypeId) {
        float netSalary = 0.0f;
        String[] columns = {"amount"};
        String whereClause = " employee_id = ? AND period_id = ? AND earning_types_id = ?";
        Object[] params = new Object[]{employeeId, periodId, basicSalaryEarningTypeId};
        try {
            JsonArray earningsRecords = GenericQueries.select(connection, "employee_earnings", columns, whereClause, params);
            for (JsonElement element : earningsRecords) {
                if (element != null && element.isJsonObject()) {
                    JsonObject earningsObject = element.getAsJsonObject();
                    if (earningsObject.has("amount")) {
                        netSalary = earningsObject.get("amount").getAsFloat();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching employee net salary: " + e.getMessage());
        }
        return netSalary; // Return the found net salary or 0.0f if none was found or an error occurred
    }


}
