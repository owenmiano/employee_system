//package org.co.skyworld.handlers;
//
//import java.time.YearMonth;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.regex.Pattern;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import java.sql.Connection;
//import java.sql.SQLException;
//
//import ke.co.skyworld.queryBuilder.GenericQueries;
//import org.mindrot.jbcrypt.BCrypt;
//
//public class EmployeeController {
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
//
//    private static boolean isValidEmail(String email) {
//        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
//        Pattern pattern = Pattern.compile(emailRegex);
//        return email != null && pattern.matcher(email).matches();
//    }
//
//    private static boolean isValidIDNumber(String idNumber) {
//        String idNumberRegex = "^\\d{8}$";
//        return idNumber != null && idNumber.matches(idNumberRegex);
//    }
//    private static boolean isValidPhoneNumber(String phoneNumber) {
//        String phoneNumberRegex = "^\\d{10}$";
//        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
//    }
//
//    public static Map<String, String> findEmployee(Connection connection, int employeeID) {
//        try {
//            String whereClause = "employee_id = ?";
//            JsonArray result = GenericQueries.select(connection, "employee", whereClause, new Object[]{employeeID});
//            if (result.size() > 0) {
//                Map<String, String> employeeInfo = new HashMap<>();
//                JsonElement element = result.get(0); // Assuming you're interested in the first result
//                if (element != null && element.isJsonObject()) {
//                    JsonObject employeeObject = element.getAsJsonObject();
//                    employeeInfo.put("company_id", employeeObject.get("company_id").getAsString());
//                    employeeInfo.put("employee_name", employeeObject.get("employee_name").getAsString());
//                    employeeInfo.put("employee_number", employeeObject.get("employee_number").getAsString());
//                    employeeInfo.put("id_number", employeeObject.get("id_number").getAsString());
//                    employeeInfo.put("nssf_no", employeeObject.get("nssf_no").getAsString());
//                    employeeInfo.put("kra_pin", employeeObject.get("kra_pin").getAsString());
//                    employeeInfo.put("phone", employeeObject.get("phone").getAsString());
//                    employeeInfo.put("email", employeeObject.get("email").getAsString());
//                    employeeInfo.put("date_of_birth", employeeObject.get("date_of_birth").getAsString());
//                    employeeInfo.put("employment_start_date", employeeObject.get("employment_start_date").getAsString());
//                    employeeInfo.put("employment_status", employeeObject.get("employment_status").getAsString());
//                    employeeInfo.put("employment_termination_date", employeeObject.get("employment_termination_date").isJsonNull() ? "N/A" : employeeObject.get("employment_termination_date").getAsString()); // Handle possible null
//                    return employeeInfo;
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("An error occurred: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return null; // Return null if no employee found or in case of an error
//    }
//
//
//    public static void createEmployee(Connection connection, HashMap<String, Object> employeeData) {
//        try {
//            if (employeeData == null) {
//                System.out.println("Employee data is missing or incomplete.");
//                return;
//            }
//            if (employeeData.containsKey("password")) {
//                String plainPassword = employeeData.get("password").toString();
//                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
//                employeeData.put("password", hashedPassword);
//            } else {
//                System.out.println("Password is missing.");
//                return;
//            }
//            String emailAddress = employeeData.get("email") != null ? employeeData.get("email").toString() : "";
//            String idNumber = employeeData.get("id_number") != null ? employeeData.get("id_number").toString() : "";
//            String phone = employeeData.get("phone") != null ? employeeData.get("phone").toString() : "";
//
//
//            if (!isValidEmail(emailAddress)) {
//                System.out.println("Invalid email address.");
//                return;
//            }
//
//            if (!isValidIDNumber(idNumber)) {
//                System.out.println("Invalid ID number.");
//                return;
//            }
//
//            if (!isValidPhoneNumber(phone)) {
//                System.out.println("Invalid phone number.");
//                return;
//            }
//            Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
//            String period = activePeriodInfo.get("period");
//
//            if (period == null) {
//                System.out.println("Failed to fetch the active period or no active period found.");
//                return;
//            }
//
//            String employmentStartDateStr = employeeData.get("employment_start_date").toString();
//            String employmentTerminationDateStr = null;
//            if (employeeData.get("employment_termination_date") != null) {
//                employmentTerminationDateStr = employeeData.get("employment_termination_date").toString();
//            }
//            YearMonth employmentStartDate = YearMonth.parse(employmentStartDateStr, formatter);
//            YearMonth activePeriodDate = YearMonth.parse(period, formatter);
//            YearMonth employmentTerminationDate = null;
//
//            if (employmentTerminationDateStr != null) {
//                employmentTerminationDate = YearMonth.parse(employmentTerminationDateStr, formatter);
//                if (employmentTerminationDate.isBefore(employmentStartDate)) {
//                    // Handle the error: log, throw an exception, or correct the data
//                    System.err.println("Error: Termination date is before employment start date.");
//                    return;
//                }
//            }
//
//// Check if the employee is new or active based on the start date
//            if (employmentStartDate.isAfter(activePeriodDate) || employmentStartDate.equals(activePeriodDate)) {
//                // If the start date is after the active period, mark as "new"
//                employeeData.put("employment_status", "new");
//            } else {
//                // If the start date is before the active period, then it's considered "active"
//                employeeData.put("employment_status", "active");
//            }
//
//            if (employmentTerminationDate != null) {
//                if (employmentTerminationDate.equals(activePeriodDate)) {
//                    employeeData.put("employment_status", "leaving");
//                }
//            }
//
//            boolean isInserted = GenericQueries.insertData(connection, "employee", employeeData);
//
//            if (isInserted) {
//                System.out.println("Employee added successfully");
//            } else {
//                System.out.println("Failed to add employee");
//            }
//
//        } catch (Exception e) {
//            System.out.println("An error occurred: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public static String fetchEmployeeTermnationDate(Connection connection, int employeeId) {
//        String[] columns = {"employment_termination_date"};
//        String whereClause = "employee_id = ?";
//        List<Integer> values = Collections.singletonList(employeeId);
//
//        try {
//            JsonArray result = GenericQueries.select(connection, "employee", columns, whereClause, values.toArray());
//            if (result.size() > 0) {
//                // Assuming the first element of the JsonArray is the JsonObject we're interested in
//                JsonElement firstElement = result.get(0);
//                if (firstElement != null && firstElement.isJsonObject()) {
//                    JsonObject employeeObject = firstElement.getAsJsonObject();
//                    // Corrected to fetch the 'employment_termination_date' field
//                    JsonElement terminationDateElement = employeeObject.get("employment_termination_date");
//                    if (terminationDateElement != null && !terminationDateElement.isJsonNull()) {
//                        return terminationDateElement.getAsString();
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println("An error occurred fetching the termination date: " + e.getMessage());
//        }
//
//        return null;
//    }
//
//    public static String fetchEmployeeStartDate(Connection connection, int employeeId) {
//        String[] columns = {"employment_start_date"};
//        String whereClause = "employee_id = ?";
//        List<Integer> values = Collections.singletonList(employeeId);
//
//        try {
//            JsonArray result = GenericQueries.select(connection, "employee", columns, whereClause, values.toArray());
//            if (result.size() > 0) {
//                // Assuming the first element of the JsonArray is the JsonObject we're interested in
//                JsonElement firstElement = result.get(0);
//                if (firstElement != null && firstElement.isJsonObject()) {
//                    JsonObject employeeObject = firstElement.getAsJsonObject();
//                    // Corrected to fetch the 'employment_termination_date' field
//                    JsonElement startDateElement = employeeObject.get("employment_start_date");
//                    if (startDateElement != null && !startDateElement.isJsonNull()) {
//                        return startDateElement.getAsString();
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println("An error occurred fetching the termination date: " + e.getMessage());
//        }
//
//        return null; // Return null if no termination date is found or in case of an error
//    }
//
//
//
//
//
//    public static void updateEmployee(Connection connection, HashMap<String, Object> employeeData, int employeeID) {
//        try {
//            Map<String, String> employeeInfo = EmployeeController.findEmployee(connection,employeeID);
//            String employmentStartDate = employeeInfo.get("employment_start_date");
//            if (employeeData == null || employeeData.isEmpty()) {
//                System.out.println("Employee data is missing or empty.");
//                return;
//            }
//            if (employeeData.containsKey("employment_termination_date")) {
//                String termination = employeeData.get("employment_termination_date").toString();
//                Map<String, String> activePeriodInfo = PeriodController.fetchActivePeriod(connection);
//                String period = activePeriodInfo.get("period");
//                YearMonth employmentDate = YearMonth.parse(employmentStartDate, formatter);
//                YearMonth employmentTerminationDate = YearMonth.parse(termination, formatter);
//                YearMonth activePeriodDate = YearMonth.parse(period, formatter);
//
//                if (employmentTerminationDate.isBefore(employmentDate)) {
//                    // Handle the error: log, throw an exception, or correct the data
//                    System.err.println("Error: Termination date is before employment start date.");
//                    return;
//                }
//
//                if (employmentTerminationDate.equals(activePeriodDate)) {
//                    employeeData.put("employment_status", "leaving");
//                } else if (activePeriodDate.isAfter(employmentTerminationDate)) {
//                    employeeData.put("employment_status", "terminated");
//                }
//
//            }
//            String whereClause = "employee_id = ?";
//
//           JsonObject result = GenericQueries.update(connection, "employee", employeeData, whereClause,new Object[]{employeeID});
//
//            if (result.get("success").getAsBoolean()) {
//                System.out.println("Employee updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
//            } else {
//                System.out.println("No rows were updated.");
//            }
//        } catch (SQLException e) {
//            System.out.println("An error occurred: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
