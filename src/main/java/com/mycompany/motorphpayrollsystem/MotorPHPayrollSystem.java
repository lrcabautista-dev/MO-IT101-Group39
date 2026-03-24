package com.mycompany.motorphpayrollsystem;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * MotorPH Payroll System
 *
 * Start Date: January 2026
 * End Date: March 2026
 *
 * Semi‑monthly payroll system that reads employee and attendance records
 * from CSV files and computes payroll from June to December.
 *
 * This version includes:
 * - Efficient file reading: employee and attendance data loaded once.
 * - Clear separation of concerns: employee lookup, payroll computation, and display are separate.
 * - Load the employee and attendance CSV files once at program start to avoid repeated file I/O.
 * - This prevents reading the same file multiple times .
 * - Full inline comments explaining not just what but why each calculation is done.
 * - Termination of program every time an employee number does not exist.
 * - Refactor: replace System.exit(0) with return for cleaner program termination
 */
public class MotorPHPayrollSystem {

    public static final Scanner scanner = new Scanner(System.in);
    public static final DecimalFormat df = new DecimalFormat("#,##0.######");

    private static final String EMPLOYEE_FILE = "MotorPH_Employee Data - Employee Details.csv";
    private static final String ATTENDANCE_FILE = "MotorPH_Employee Data - Attendance Record.csv";

    private static List<String[]> employeeRecords = new ArrayList<>();
    private static List<String[]> attendanceRecords = new ArrayList<>();

    public static void main(String[] args) {
        // Ensures UTF-8 output for currency symbols
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));

        // Load all employee and attendance records once for efficiency
        loadEmployeeRecords();
        loadAttendanceRecords();

        // Authenticate user before allowing access
        authenticateUser();
    }

    /**
     * Validates login credentials and routes the user to the correct menu.
     * Exits program if login fails to prevent unauthorized access.
     */
    public static void authenticateUser() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        boolean validUser = username.equals("employee") || username.equals("payroll_staff");
        boolean validPassword = password.equals("12345");

        if (!validUser || !validPassword) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        if (username.equals("employee")) {
            runEmployeeInterface();
        } else {
            runPayrollStaffMenu();
        }
    }

    /**
     * Employee interface menu.
     * Allows employee to view personal information.
     */
    public static void runEmployeeInterface() {

        while (true) {

            System.out.println("\nEmployee Menu");
            System.out.println("1. Enter Employee Number");
            System.out.println("2. Exit Program");
            System.out.print("Select option: ");

            String option = scanner.nextLine();

            if (option.equals("1")) {
                System.out.print("Enter Employee #: ");
                String empNumber = scanner.nextLine();

                lookupEmployeeRecord(empNumber);

            } else if (option.equals("2")) {
                System.out.println("Program terminated.");
                return;

            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Payroll staff main menu.
     * Allows navigation to payroll processing or exit.
     */
    public static void runPayrollStaffMenu() {

        while (true) {

            System.out.println("\nPayroll Staff Menu");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit Program");
            System.out.print("Select option: ");

            String option = scanner.nextLine();

            switch (option) {

                case "1":
                    runPayrollProcessingMenu();
                    break;

                case "2":
                    System.out.println("Program terminated.");
                    return;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Payroll processing submenu.
     * Allows processing for one employee or all employees.
     */
    public static void runPayrollProcessingMenu() {

        while (true) {

            System.out.println("\nProcess Payroll");
            System.out.println("1. One Employee");
            System.out.println("2. All Employees");
            System.out.println("3. Exit Program");
            System.out.print("Select option: ");

            String option = scanner.nextLine();

            switch (option) {

                case "1":
                    System.out.print("Enter Employee #: ");
                    String empNumber = scanner.nextLine();

                    processPayrollForEmployee(empNumber);

                    break;

                case "2":
                    processPayrollForAllEmployees();
                    break;

                case "3":
                    System.exit(0);

                default:
                    System.out.println("Invalid option.");
                    
            }
        }
    }

    /**
     * Loads employee records from the CSV file once at program start.
     * Using regex ensures fields with embedded commas remain intact.
     */
    public static void loadEmployeeRecords() {

        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {

            br.readLine(); // Skip header line

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Remove whitespace from each field
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim();
                }

                employeeRecords.add(data); // Store for repeated access
            }

        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        }
    }

    /**
     * Loads attendance records from the CSV file once at program start.
     * Prevents repeated file I/O during monthly calculations for efficiency.
     */
    public static void loadAttendanceRecords() {

        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {

            br.readLine(); // Skip header

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim();
                }

                attendanceRecords.add(data); // Store for processing
            }

        } catch (IOException e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }
    }

    /**
     * Finds employee data using employee number.
     * Returns null if employee does not exist.
     */
    public static String[] findEmployee(String empNumber) {

        for (String[] employee : employeeRecords) {

            if (employee[0].equals(empNumber)) {
                return employee;
            }
        }

        return null;
    }

    /**
     * Processes payroll for all employees.
     * Iterates over the stored employee list to compute payroll efficiently.
     */
    public static void processPayrollForAllEmployees() {

        for (String[] employee : employeeRecords) {

            String empNumber = employee[0];

            processPayrollForEmployee(empNumber);

            System.out.println("\n--------------------------------------------\n");
        }
    }

    /**
     * Processes payroll for a single employee.
     * Splits working hours into first and second cutoffs.
     * Computes gross salary and applies statutory deductions for second cutoff.
     */
    public static void processPayrollForEmployee(String empNumber) {

        String[] employee = findEmployee(empNumber);

        if (employee == null) {

            System.out.println("Employee number does not exist.");
            return;
        }

        String lastName = employee[1];
        String firstName = employee[2];
        String birthday = employee[3];

        String rateStr = employee[18].replace("\"", "").trim();
        double hourlyRate = rateStr.isEmpty() ? 0.0 : Double.parseDouble(rateStr);

        System.out.println("\nEmployee #: " + empNumber);
        System.out.println("Employee Name: " + lastName + ", " + firstName);
        System.out.println("Birthday: " + birthday);

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

        // Compute payroll for June to December
        for (int month = 6; month <= 12; month++) {

            double firstCutoffHours = 0;
            double secondCutoffHours = 0;

            int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();

            // Iterate through all attendance records to compute hours
            for (String[] record : attendanceRecords) {

                if (!record[0].equals(empNumber)) continue;

                String[] dateParts = record[3].split("/");

                int recordMonth = Integer.parseInt(dateParts[0]);
                int recordDay = Integer.parseInt(dateParts[1]);
                int recordYear = Integer.parseInt(dateParts[2]);

                // Only include records matching current month/year
                if (recordYear != 2024 || recordMonth != month) continue;

                LocalTime login = LocalTime.parse(record[4], timeFormat);
                LocalTime logout = LocalTime.parse(record[5], timeFormat);

                double hoursWorked = computeHours(login, logout);

                // Split into first and second cutoffs
                if (recordDay <= 15) {
                    firstCutoffHours += hoursWorked;
                } else {
                    secondCutoffHours += hoursWorked;
                }
            }

            displayPayrollForMonth(month, daysInMonth, firstCutoffHours, secondCutoffHours, hourlyRate);
        }
    }

    /**
     * Displays payroll results for a specific month.
     * First cutoff: earnings only.
     * Second cutoff: applies all statutory deductions.
     */
    public static void displayPayrollForMonth(int month, int daysInMonth,
            double firstHours, double secondHours, double hourlyRate) {

        String monthName = getMonthName(month);

        // First cutoff: 1–15, no deductions
        double firstGross = firstHours * hourlyRate;

        System.out.println("\nCutoff Date: " + monthName + " 1 to 15");
        System.out.println("Total Hours Worked: " + df.format(firstHours));
        System.out.println("Gross Salary: ₱" + df.format(firstGross));
        System.out.println("Net Salary: ₱" + df.format(firstGross));

        // Second cutoff: 16–end, deductions applied
        double secondGross = secondHours * hourlyRate;

        double monthlyGross = firstGross + secondGross;

        // Compute statutory deductions based on full month
        double sss = computeSSS(monthlyGross);
        double philhealth = computePhilHealth(monthlyGross);
        double pagibig = computePagIbig(monthlyGross);
        double tax = computeIncomeTax(monthlyGross);

        double totalDeductions = sss + philhealth + pagibig + tax;

        double secondNet = secondGross - totalDeductions;

        System.out.println("\nCutoff Date: " + monthName + " 16 to " + daysInMonth);
        System.out.println("Total Hours Worked: " + df.format(secondHours));
        System.out.println("Gross Salary: ₱" + df.format(secondGross));

        System.out.println("SSS: ₱" + df.format(sss));
        System.out.println("PhilHealth: ₱" + df.format(philhealth));
        System.out.println("Pag-IBIG: ₱" + df.format(pagibig));
        System.out.println("Tax: ₱" + df.format(tax));

        System.out.println("Total Deductions: ₱" + df.format(totalDeductions));
        System.out.println("Net Salary: ₱" + df.format(secondNet));
    }

    /**
     * Computes daily working hours with payroll rules:
     * - Maximum of 8 billable hours
     * - 1-hour break deduction if worked >4 hours
     * - End time capped at 5:00 PM
     * - Grace period gives full credit if login within 8:00–8:10
     */
    static double computeHours(LocalTime login, LocalTime logout) {

        LocalTime officialStart = LocalTime.of(8, 0);
        LocalTime graceLimit = LocalTime.of(8, 10);
        LocalTime officialEnd = LocalTime.of(17, 0);

        if (!login.isBefore(officialStart) && !login.isAfter(graceLimit)) {
            login = officialStart;
        }

        if (logout.isAfter(officialEnd)) {
            logout = officialEnd;
        }

        long minutesWorked = Duration.between(login, logout).toMinutes();

        if (minutesWorked > 240) { // Subtract lunch break if worked >4 hours
            minutesWorked -= 60;
        }

        double hours = minutesWorked / 60.0;

        if (hours > 8) { // Cap at 8 hours
            hours = 8;
        }

        return hours;
    }

    /**
     * Displays employee information for employee login.
     * Terminates the program if employee number input is not valid.
     */
    public static void lookupEmployeeRecord(String empNumber) {

        String[] employee = findEmployee(empNumber);

        if (employee == null) {
            System.out.println("Employee number does not exist.");
            System.exit(0);
        }

        System.out.println("Employee Number: " + employee[0]);
        System.out.println("Employee Name: " + employee[1] + ", " + employee[2]);
        System.out.println("Birthday: " + employee[3]);
    }

    public static String getMonthName(int month) {

        switch (month) {

            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";

            default: return "";
        }
    }

    /**
     * Computes SSS contribution based on total monthly earnings.
     * Table-based lookup according to statutory rates.
     */
    public static double computeSSS(double salary) {
        if (salary < 3250) return 135.00;
        else if (salary < 3750) return 157.50;
        else if (salary < 4250) return 180.00;
        else if (salary < 4750) return 202.50;
        else if (salary < 5250) return 225.00;
        else if (salary < 5750) return 247.50;
        else if (salary < 6250) return 270.00;
        else if (salary < 6750) return 292.50;
        else if (salary < 7250) return 315.00;
        else if (salary < 7750) return 337.50;
        else if (salary < 8250) return 360.00;
        else if (salary < 8750) return 382.50;
        else if (salary < 9250) return 405.00;
        else if (salary < 9750) return 427.50;
        else if (salary < 10250) return 450.00;
        else if (salary < 10750) return 472.50;
        else if (salary < 11250) return 495.00;
        else if (salary < 11750) return 517.50;
        else if (salary < 12250) return 540.00;
        else if (salary < 12750) return 562.50;
        else if (salary < 13250) return 585.00;
        else if (salary < 13750) return 607.50;
        else if (salary < 14250) return 630.00;
        else if (salary < 14750) return 652.50;
        else if (salary < 15250) return 675.00;
        else if (salary < 15750) return 697.50;
        else if (salary < 16250) return 720.00;
        else if (salary < 16750) return 742.50;
        else if (salary < 17250) return 765.00;
        else if (salary < 17750) return 787.50;
        else if (salary < 18250) return 810.00;
        else if (salary < 18750) return 832.50;
        else if (salary < 19250) return 855.00;
        else if (salary < 19750) return 877.50;
        else if (salary < 20250) return 900.00;
        else if (salary < 20750) return 922.50;
        else if (salary < 21250) return 945.00;
        else if (salary < 21750) return 967.50;
        else if (salary < 22250) return 990.00;
        else if (salary < 22750) return 1012.50;
        else if (salary < 23250) return 1035.00;
        else if (salary < 23750) return 1057.50;
        else if (salary < 24250) return 1080.00;
        else if (salary < 24750) return 1102.50;
        else return 1125.00;
    }

    /**
     * Computes PhilHealth contribution based on total monthly salary.
     * Rules:
     * - 3% of salary (standard)
     * - Minimum 300, maximum 1800
     */
    public static double computePhilHealth(double salary) {

        double premium = salary * 0.03;

        if (salary <= 10000) return 300;
        if (salary >= 60000) return 1800;

        return premium;
    }

    /**
     * Computes Pag-IBIG contribution based on total monthly salary.
     * Rules:
     * - 1% for low-income (1000–1500)
     * - 2% for other salaries
     */
    public static double computePagIbig(double salary) {

        if (salary >= 1000 && salary <= 1500) return salary * 0.01;

        return salary * 0.02;
    }

    /**
     * Computes withholding tax based on total monthly salary.
     * Progressive tax rates according to Philippine law.
     */
    public static double computeIncomeTax(double salary) {

        if (salary <= 20832) return 0;
        else if (salary < 33333) return (salary - 20832) * 0.20;
        else if (salary < 66667) return 2500 + (salary - 33333) * 0.25;
        else if (salary < 166667) return 10833 + (salary - 66667) * 0.30;
        else if (salary < 666667) return 40833.33 + (salary - 166667) * 0.32;
        else return 200833.33 + (salary - 666667) * 0.35;
    }
}