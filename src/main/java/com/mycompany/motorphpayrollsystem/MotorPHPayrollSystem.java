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

/**
 * MotorPH Payroll System
 *
 * Implements a semi-monthly payroll model:
 * - Cutoff 1 (1–15): earnings only (no deductions applied)
 * - Cutoff 2 (16–end): full statutory deductions applied
 *
 * All government deductions are computed based on total monthly earnings
 * and applied entirely during the second cutoff.
 */
public class MotorPHPayrollSystem {

    /** Shared input handler */
    public static final Scanner scanner = new Scanner(System.in);

    /** Formatter for consistent numeric output */
    public static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public static void main(String[] args) {
        authenticateUser();
    }

    /**
     * Authenticates user and routes to corresponding interface
     */
    public static void authenticateUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if ((username.equals("employee") || username.equals("payroll_staff"))
                && password.equals("12345")) {

            System.out.println("Login successful!\n");

            if (username.equals("employee")) {
                runEmployeeInterface();
            } else {
                runPayrollStaffMenu();
            }

        } else {
            System.out.println("Invalid credentials.");
        }
    }

    /**
     * Allows employee to retrieve personal record
     */
    public static void runEmployeeInterface() {
        System.out.print("Enter Employee ID: ");
        String id = scanner.nextLine();
        lookupEmployeeRecord(id);
    }

    /**
     * Main navigation menu for payroll staff
     */
    public static void runPayrollStaffMenu() {
        while (true) {
            System.out.println("\n=== Payroll Staff Menu ===");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit Program");
            System.out.print("Select option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    runPayrollProcessingMenu();
                    break;
                case "2":
                    System.out.println("Exiting program...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    /**
     * Submenu for payroll processing operations
     */
    public static void runPayrollProcessingMenu() {
        while (true) {
            System.out.println("\n=== Payroll Processing Options ===");
            System.out.println("1. Process One Employee");
            System.out.println("2. Process All Employees");
            System.out.println("3. Exit Program");
            System.out.print("Select option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Employee ID: ");
                    String empNumber = scanner.nextLine();
                    processPayrollForEmployee(empNumber);
                    break;
                case "2":
                    processPayrollForAllEmployees();
                    break;
                case "3":
                    System.out.println("Returning to Payroll Staff Menu...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    /**
     * Iterates through all employee records and generates full payroll reports
     * using the same computation logic applied to individual processing
     */
    public static void processPayrollForAllEmployees() {
        String employeeFile = "MotorPH_Employee Data - Employee Details.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(employeeFile))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Regex split ensures fields with embedded commas remain intact
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (int i = 0; i < data.length; i++) data[i] = data[i].trim();

                String empNumber = data[0];

                processPayrollForEmployee(empNumber);

                System.out.println("\n===========================================\n");
            }

        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        }
    }

    /**
     * Computes payroll per employee across all months and cutoffs
     */
    public static void processPayrollForEmployee(String empNumber) {
        String employeeFile = "MotorPH_Employee Data - Employee Details.csv";
        String attendanceFile = "MotorPH_Employee Data - Attendance Record.csv";

        String firstName = "";
        String lastName = "";
        String birthday = "";
        double hourlyRate = 0.0;
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(employeeFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (int i = 0; i < data.length; i++) data[i] = data[i].trim();

                if (data[0].equals(empNumber)) {
                    lastName = data[1];
                    firstName = data[2];
                    birthday = data[3];

                    // Remove quotes from numeric field before parsing
                    String rateStr = data[18].replace("\"", "").trim();
                    hourlyRate = rateStr.isEmpty() ? 0.0 : Double.parseDouble(rateStr);

                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
            return;
        }

        if (!found) {
            System.out.println("Employee not found.");
            return;
        }

        System.out.println("\n===================================");
        System.out.println("Employee # : " + empNumber);
        System.out.println("Name       : " + lastName + ", " + firstName);
        System.out.println("Birthday   : " + birthday);
        System.out.println("===================================");

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

        for (int month = 6; month <= 12; month++) {
            double firstHalfHours = 0;
            double secondHalfHours = 0;
            int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();

            try (BufferedReader br = new BufferedReader(new FileReader(attendanceFile))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] data = line.split(",");
                    for (int i = 0; i < data.length; i++) data[i] = data[i].trim();

                    if (!data[0].equals(empNumber)) continue;

                    String[] dateParts = data[3].split("/");
                    int recordMonth = Integer.parseInt(dateParts[0]);
                    int day = Integer.parseInt(dateParts[1]);
                    int year = Integer.parseInt(dateParts[2]);

                    // Only include records matching current processing month/year
                    if (year != 2024 || recordMonth != month) continue;

                    LocalTime login = LocalTime.parse(data[4], timeFormat);
                    LocalTime logout = LocalTime.parse(data[5], timeFormat);

                    double hoursWorked = computeHours(login, logout);

                    // Split working hours into semi-monthly periods
                    if (day <= 15) firstHalfHours += hoursWorked;
                    else secondHalfHours += hoursWorked;
                }
            } catch (IOException e) {
                System.out.println("Error reading attendance file: " + e.getMessage());
                continue;
            }

            String monthName = switch (month) {
                case 6 -> "June"; case 7 -> "July"; case 8 -> "August";
                case 9 -> "September"; case 10 -> "October";
                case 11 -> "November"; case 12 -> "December";
                default -> "Month " + month;
            };

            double firstGross = firstHalfHours * hourlyRate;
            double firstNet = firstGross;

            System.out.println("\nCutoff Date: " + monthName + " 1–15");
            System.out.println("Total Hours Worked : " + df.format(firstHalfHours));
            System.out.println("Gross Salary       : ₱" + df.format(firstGross));
            System.out.println("Net Salary         : ₱" + df.format(firstNet));

            double secondGross = secondHalfHours * hourlyRate;

            // Total monthly earnings used as basis for statutory deductions
            double totalGross = firstGross + secondGross;

            double sss = computeSSS(totalGross);
            double philhealth = computePhilHealth(totalGross);
            double pagibig = computePagIbig(totalGross);
            double tax = computeIncomeTax(totalGross);
            double totalDeductions = sss + philhealth + pagibig + tax;

            // Entire deduction is applied during second cutoff
            double secondNet = secondGross - totalDeductions;

            System.out.println("\nCutoff Date: " + monthName + " 16–" + daysInMonth);
            System.out.println("Total Hours Worked : " + df.format(secondHalfHours));
            System.out.println("Gross Salary       : ₱" + df.format(secondGross));
            System.out.println("Deductions:");
            System.out.println("  SSS              : ₱" + df.format(sss));
            System.out.println("  PhilHealth       : ₱" + df.format(philhealth));
            System.out.println("  Pag-IBIG         : ₱" + df.format(pagibig));
            System.out.println("  Withholding Tax  : ₱" + df.format(tax));
            System.out.println("Total Deductions   : ₱" + df.format(totalDeductions));
            System.out.println("Net Salary         : ₱" + df.format(secondNet));
        }
    }

    /**
     * Calculates effective work hours with constraints:
     * - Maximum of 8 billable hours
     * - 1-hour break deduction
     * - End time capped at 5:00 PM
     * - Grace period grants full credit if arrival is within threshold
     */
    static double computeHours(LocalTime login, LocalTime logout) {
        LocalTime graceTime = LocalTime.of(8, 10);
        LocalTime cutoffTime = LocalTime.of(17, 0);

        if (logout.isAfter(cutoffTime)) logout = cutoffTime;

        long minutesWorked = Duration.between(login, logout).toMinutes();
        if (minutesWorked > 60) minutesWorked -= 60;
        else minutesWorked = 0;

        double hours = minutesWorked / 60.0;
        if (!login.isAfter(graceTime)) return 8.0;
        return Math.min(hours, 8.0);
    }

    public static void lookupEmployeeRecord(String targetId) {
        String file = "MotorPH_Employee Data - Employee Details.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                for (int i = 0; i < data.length; i++) data[i] = data[i].trim();
                if (data[0].equals(targetId)) {
                    System.out.println("\nEmployee ID : " + data[0]);
                    System.out.println("Name        : " + data[1] + ", " + data[2]);
                    System.out.println("Birthday    : " + data[3]);
                    return;
                }
            }
            System.out.println("Record not found.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

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

    public static double computePhilHealth(double salary) {
        double premium = salary * 0.03;
        if (salary <= 10000) return 300;
        else if (salary >= 60000) return 1800;
        else return premium;
    }

    public static double computePagIbig(double salary) {
        if (salary >= 1000 && salary <= 1500) return salary * 0.01;
        else return salary * 0.02;
    }

    public static double computeIncomeTax(double salary) {
        if (salary <= 20832) return 0;
        else if (salary < 33333) return (salary - 20832) * 0.20;
        else if (salary < 66667) return 2500 + (salary - 33333) * 0.25;
        else if (salary < 166667) return 10833 + (salary - 66667) * 0.30;
        else if (salary < 666667) return 40833.33 + (salary - 166667) * 0.32;
        else return 200833.33 + (salary - 666667) * 0.35;
    }

    public static double computeNetPay(double salary) {
        return salary - (computeSSS(salary) + computePhilHealth(salary) + computePagIbig(salary) + computeIncomeTax(salary));
    }
}