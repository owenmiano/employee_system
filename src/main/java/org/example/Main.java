package org.example;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        File configFile = getConfigFile();
        try {
            DatabaseConnectionManager dbManager = new DatabaseConnectionManager(configFile);

            try (Connection connection = dbManager.getConnection()) {
                System.out.println("Database Connected successfully");
                dbManager.createTables(connection);

                addEmployeeEarnings(connection);
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
                "company_id", "name", "reg_no","industry","phone","email","website",

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

        int departmentID = 1;
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
        HashMap<String, Object> earningsTypeData = new HashMap<>();
        earningsTypeData.put("name", "");

        String[] columns = {"name"};

        EarningsController.findEarningTypes(connection, earningsTypeData,columns);

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
        deductionTypeData.put("name", "NHIF");

        DeductionsController.createDeductionTypes(connection, deductionTypeData);

    }
    //     select deduction type method
    private static void selectDeductionType(Connection connection) {
        HashMap<String, Object> deductionTypeData = new HashMap<>();
        deductionTypeData.put("name", "");

        String[] columns = {"name"};

        DeductionsController.findDeductionTypes(connection, deductionTypeData,columns);

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
        employeeData.put("employee_name", "Alice Mwangi");
        employeeData.put("employee_number", "SKY1313");
        employeeData.put("id_number", "22456789");
        employeeData.put("nssf_no", "010103030303041413");
        employeeData.put("nhif_no", "2220354246");
        employeeData.put("kra_pin", "A0134510G");
        employeeData.put("phone", "0712345678");
        employeeData.put("email", "alice90@gmail.com");
        employeeData.put("date_of_birth", "1992-05-20");
        employeeData.put("employment_start_date", "04-2023");
        employeeData.put("employment_termination_date", null);
        employeeData.put("employee_position", "HR Manager");
        employeeData.put("department_id", 1);
        employeeData.put("gender", "Female");
        employeeData.put("username", "alicia");
        employeeData.put("password", "Alice!2024$");

        EmployeeController.createEmployee(connection, employeeData);

    }
    //    //    select company method
    private static void selectEmployee(Connection connection) {
        HashMap<String, Object> employeeData = new HashMap<>();
        employeeData.put("employee_number", "SKY1311");

        String[] columns = {
                "employee_name", "employee_number","id_number","nssf_no","nhif_no","kra_pin","phone","date_of_birth","employment_start_date",
                "employment_termination_date","employee_position","gender","username"
        };

        EmployeeController.findEmployee(connection, employeeData,columns);

    }
    //    modify employee method
    private static void modifyEmployee(Connection connection) {
        HashMap<String, Object> employeeData = new HashMap<>();
        employeeData.put("kra_pin", "A0145704G");

        int employeeID = 2;
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
        HashMap<String, Object> periodData = new HashMap<>();
        periodData.put("period", "08-2023");
        periodData.put("status", "Active");

        String[] columns = {"period","status"};

        PeriodController.findPeriod(connection, periodData,columns);

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

        HashMap<String, Object> employeeEarningsData = new HashMap<>();
        employeeEarningsData.put("earning_types_id", 1);
        employeeEarningsData.put("employee_id", 3);
        employeeEarningsData.put("amount", 10000);
        employeeEarningsData.put("period_id", 1);
        EmployeeEarningsController.createEmployeeEarnings(connection, employeeEarningsData);

    }
////    private static void addEmployeeAllowances(Connection connection) {
////        HashMap<String, Object> employeeAllowanceData = new HashMap<>();
////        employeeAllowanceData.put("earning_types_id", 1);
////        employeeAllowanceData.put("employee_id", 1);
////        EmployeeEarningsController.createEmployeeAllowance(connection, employeeAllowanceData);
////
////    }
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


}