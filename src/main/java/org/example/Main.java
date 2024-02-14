package org.example;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class Main {
    public static void main(String[] args) {
        File configFile = getConfigFile();
        try {
            DatabaseConnectionManager dbManager = new DatabaseConnectionManager(configFile);

            try (Connection connection = dbManager.getConnection()) {
                System.out.println("Database Connected successfully");
                dbManager.createTables(connection);
                Rollover.rollover(connection);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize the database: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
    private static File getConfigFile() {
        Path folderPath = Paths.get("config", "config.xml");
        if (!Files.exists(folderPath)) {
            throw new IllegalArgumentException("Configuration file not found at " + folderPath);
        }
        return folderPath.toFile();
    }

//add company method
    private static void addCompany(Connection connection) {
        HashMap<String, Object> companyData = new HashMap<>();
        companyData.put("name", "Sky World LTD");
        companyData.put("reg_no", "BN-KYCZMHJU");
        companyData.put("industry", "Inspiring Innovation");
        companyData.put("mission", "Attract and retain quality, high-paying customers");
        companyData.put("phone", "0726713580");
        companyData.put("email", "info@skyworld.co.ke");
        companyData.put("website", "https://skyworld.co.ke/");
        companyData.put("branch", "Nairobi");
        companyData.put("postal_address", "Western Heights, 7th floor, Suite 11, Karuna Road, Westlands, Nairobi.");
        companyData.put("company_inception", "2015-01-15");


        CompanyController.createCompany(connection, companyData);

    }
//    //    select company method
    private static void selectCompany(Connection connection) {
        HashMap<String, Object> companyData = new HashMap<>();
        companyData.put("reg_no", "BN-KYCZMHJU");

        String[] columns = {
                "name", "reg_no","industry","phone","email","website",

        };

        CompanyController.findCompany(connection, companyData,columns);

    }
//    modify company method
    private static void modifyCompany(Connection connection) {
        HashMap<String, Object> companyData = new HashMap<>();
        companyData.put("phone", "0726713580");

        int companyID = 1;
        CompanyController.updateCompany(connection, companyData,companyID);

    }

    //add department method
    private static void addDepartment(Connection connection) {
        HashMap<String, Object> departmentData = new HashMap<>();
        departmentData.put("company_id", 1);
        departmentData.put("name", "Sales and Marketing");

        DepartmentController.createDepartment(connection, departmentData);

    }
    //     select department method
    private static void selectDepartment(Connection connection) {
        HashMap<String, Object> departmentData = new HashMap<>();
        departmentData.put("name", "Procurement");

        String[] columns = {"name"};

        DepartmentController.findDepartment(connection, departmentData,columns);

    }
    //    modify department method
    private static void modifyDepartment(Connection connection) {
        HashMap<String, Object> departmentData = new HashMap<>();
        departmentData.put("name", "Procurement");

        int departmentID = 4;
        DepartmentController.updateDepartment(connection, departmentData,departmentID);

    }

    //add earning types method
    private static void addEarningsType(Connection connection) {
        HashMap<String, Object> earningsTypeData = new HashMap<>();
        earningsTypeData.put("company_id", 1);
        earningsTypeData.put("name", "Mortgage Allowance");

        EarningsController.createEarningsType(connection, earningsTypeData);

    }
    //     select earning type method
    private static void selectEarningsType(Connection connection) {
        String earningType="Basic Salary";
        EarningsController.findEarningType(connection, earningType);

    }
    //    modify earning type method
    private static void modifyEarningsType(Connection connection) {
        HashMap<String, Object> earningsTypeData = new HashMap<>();
        earningsTypeData.put("name", "Basic Salary");

        int earningTypeID = 1;
        EarningsController.updateEarningTypes(connection, earningsTypeData,earningTypeID);

    }

    //add deduction types method
    private static void addDeductionTypes(Connection connection) {
        HashMap<String, Object> deductionTypeData = new HashMap<>();
        deductionTypeData.put("company_id", 1);
        deductionTypeData.put("name", "PAYE");

        DeductionsController.createDeductionTypes(connection, deductionTypeData);

    }
    //     select deduction type method
    private static void selectDeductionType(Connection connection) {
        String deductionType="PAYE";
        DeductionsController.findDeductionType(connection, deductionType);
    }
    //    modify deduction type method
    private static void modifyDeductionType(Connection connection) {
        HashMap<String, Object> deductionTypeData = new HashMap<>();
        deductionTypeData.put("name", "Basic Salary");

        int deductionTypeID = 1;
        DeductionsController.updateDeductionTypes(connection, deductionTypeData,deductionTypeID);

    }


    //add company method
    private static void addEmployee(Connection connection) {
        HashMap<String, Object> employeeData = new HashMap<>();
        employeeData.put("company_id", 1);
        employeeData.put("employee_name", "Matilda Agwambo");
        employeeData.put("employee_number", "SKY1315");
        employeeData.put("id_number", "30034507");
        employeeData.put("nssf_no", "010104040404041515");
        employeeData.put("nhif_no", "2220390124");
        employeeData.put("kra_pin", "A0190710G");
        employeeData.put("phone", "0723452233");
        employeeData.put("email", "matilda90@gmail.com");
        employeeData.put("date_of_birth", "1989-09-30");
        employeeData.put("employment_start_date", "09-2023");
        employeeData.put("employment_termination_date", null);
        employeeData.put("employee_position", "Marketing Manager");
        employeeData.put("department_id", 2);
        employeeData.put("gender", "Female");
        employeeData.put("username", "matilda");
        employeeData.put("password", "Matilda!2024$");

        EmployeeController.createEmployee(connection, employeeData);

    }
    // select company method
    private static void selectEmployee(Connection connection) {
        int employeeID = 2;
        EmployeeController.findEmployee(connection, employeeID);

    }
    //    modify employee method
    private static void modifyEmployee(Connection connection) {
        HashMap<String, Object> employeeData = new HashMap<>();
        employeeData.put("employment_termination_date", "08-2023");

        int employeeID = 3;
        EmployeeController.updateEmployee(connection, employeeData,employeeID);

    }

    //add period method
    private static void addPeriod(Connection connection) {
        HashMap<String, Object> periodData = new HashMap<>();
        periodData.put("period", "08-2023");
        periodData.put("status", "Current");

        PeriodController.createPeriod(connection, periodData);

    }
    //     select period method
    private static void selectPeriod(Connection connection) {
            int periodId=1;

        PeriodController.fetchPeriod(connection, periodId);

    }
    //    modify period method
    private static void modifyPeriod(Connection connection) {
        HashMap<String, Object> periodData = new HashMap<>();
        periodData.put("period", "2024-01");

        int periodID = 1;
        PeriodController.updatePeriod(connection, periodData,periodID);

    }

    //add Employee earnings  method
    private static void addEmployeeEarnings(Connection connection) {

        int employeeId = 5;
      EmployeeEarningsController.createEmployeeEarnings(connection,employeeId);

    }


    //     select employee earnings  method
    private static void selectEmployeeEarnings(Connection connection) {

        int employeeEarningsID=1;
        EmployeeEarningsController.findEmployeeEarnings(connection, employeeEarningsID);

    }
    //    modify earning type method
    private static void modifyEmployeeEarnings(Connection connection) {
        HashMap<String, Object> earningsTypeData = new HashMap<>();
        earningsTypeData.put("name", "Basic Salary");

        int earningTypeID = 1;
        EmployeeEarningsController.updateEarningTypes(connection, earningsTypeData,earningTypeID);

    }

    // Deductions calculation method
//    private static void addEmployeeReductions(Connection connection) {
//        int employeeId=4;
//            Map<String, String> periodInfo = PeriodController.fetchActivePeriod(connection);
//        int periodId = Integer.parseInt(periodInfo.get("period_id"));
//
//        EmployeeDeductionsController.getEmployeeEarnings(connection,employeeId,periodId);
//    }

    //Reports section
    //Display the total number of active employees in a department
    private static void countActiveEmployees(Connection connection) {
        int departmentId=4;


        Reports.displayActiveEmployeeCountsByDepartment(connection,departmentId);
    }

    //Generate a report on the total earnings of an employee and total deductions as well as their net pay.
    private static void displayemployeeEarningsDeductions(Connection connection) {
        int employeeId=1;
        int periodId=1;

        Reports.generateEmployeeEarningsAndDeductionsReport(connection,employeeId,periodId);
    }

    //Generate a report on the total allowances and net salaries of each employee in a department.
    private static void displayEmployeeTotalAllowancesAndNetSalaries(Connection connection) {
        int departmentId=4;
        int periodId=1;
        Reports.generateEmployeeAllowancesAndNetSalariesReport(connection,departmentId,periodId);
    }
}