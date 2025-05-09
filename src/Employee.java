import java.sql.*;
import java.util.ArrayList;

public class Employee {
    private int id;
    private String name;
    private String position;
    private double baseSalary;
    private double hourlyRate;
    private double hoursWorked;
    private double overtimeHours;
    private int workingDays;
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;

    public Employee(String name, String position, double hourlyRate, int workingDays) {
        this.name = name;
        this.position = position;
        this.hourlyRate = hourlyRate;
        this.workingDays = workingDays;
        this.baseSalary = hourlyRate * 12 * workingDays * 4;
        computeDeductions();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public double getBaseSalary() { return baseSalary; }
    public double getHourlyRate() { return hourlyRate; }
    public double getHoursWorked() { return hoursWorked; }
    public double getOvertimeHours() { return overtimeHours; }
    public int getWorkingDays() { return workingDays; }
    public double getSss() { return sss; }
    public double getPhilhealth() { return philhealth; }
    public double getPagibig() { return pagibig; }
    public double getTax() { return tax; }

    public void setHoursWorked(double hours) throws IllegalArgumentException { 
        if (hours < 0 || hours > 12) {
            throw new IllegalArgumentException("Regular hours must be between 0 and 12");
        }
        this.hoursWorked = hours;
        computeDeductions();
    }
    
    public void setOvertimeHours(double hours) throws IllegalArgumentException {
        if (hours < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative");
        }
        this.overtimeHours = hours;
        computeDeductions();
    }
    
    public void setWorkingDays(int days) throws IllegalArgumentException {
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException("Working days must be between 1 and 7");
        }
        this.workingDays = days;
        this.baseSalary = hourlyRate * 12 * workingDays * 4;
        computeDeductions();
    }

    private void computeDeductions() {
        if (baseSalary <= 3250) this.sss = 135.00;
        else if (baseSalary <= 3750) this.sss = 157.50;
        else if (baseSalary <= 4250) this.sss = 180.00;
        else this.sss = 1125.00;
        
        this.philhealth = baseSalary * 0.02;
        this.pagibig = Math.min(baseSalary * 0.02, 100);
        
        double taxableIncome = calculateMonthlyGrossPay() - (sss + philhealth + pagibig);
        if (taxableIncome <= 20833) this.tax = 0;
        else if (taxableIncome <= 33333) this.tax = (taxableIncome - 20833) * 0.20;
        else if (taxableIncome <= 66667) this.tax = 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome <= 166667) this.tax = 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome <= 666667) this.tax = 40833 + (taxableIncome - 166667) * 0.32;
        else this.tax = 200833 + (taxableIncome - 666667) * 0.35;
    }

    public double calculateDailyGrossPay() {
        return (hoursWorked * hourlyRate) + (overtimeHours * hourlyRate * 1.25);
    }
    
    public double calculateWeeklyGrossPay() {
        return calculateDailyGrossPay() * workingDays;
    }
    
    public double calculateMonthlyGrossPay() {
        return calculateWeeklyGrossPay() * 4;
    }
    
    public double calculateDailyNetPay() {
        return calculateDailyGrossPay() - ((sss + philhealth + pagibig + tax) / (workingDays * 4));
    }
    
    public double calculateWeeklyNetPay() {
        return calculateWeeklyGrossPay() - ((sss + philhealth + pagibig + tax) / 4);
    }
    
    public double calculateMonthlyNetPay() {
        return calculateMonthlyGrossPay() - (sss + philhealth + pagibig + tax);
    }

    public void save() throws SQLException {
        if (id == 0) {
            String sql = "INSERT INTO employees(name, position, hourly_rate, base_salary, " +
                         "hours_worked, overtime_hours, working_days, sss, philhealth, pagibig, tax) " +
                         "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, position);
                pstmt.setDouble(3, hourlyRate);
                pstmt.setDouble(4, baseSalary);
                pstmt.setDouble(5, hoursWorked);
                pstmt.setDouble(6, overtimeHours);
                pstmt.setInt(7, workingDays);
                pstmt.setDouble(8, sss);
                pstmt.setDouble(9, philhealth);
                pstmt.setDouble(10, pagibig);
                pstmt.setDouble(11, tax);
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.id = rs.getInt(1);
                    }
                }
            }
        } else {
            String sql = "UPDATE employees SET name = ?, position = ?, hourly_rate = ?, " +
                         "base_salary = ?, hours_worked = ?, overtime_hours = ?, working_days = ?, " +
                         "sss = ?, philhealth = ?, pagibig = ?, tax = ? WHERE id = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, position);
                pstmt.setDouble(3, hourlyRate);
                pstmt.setDouble(4, baseSalary);
                pstmt.setDouble(5, hoursWorked);
                pstmt.setDouble(6, overtimeHours);
                pstmt.setInt(7, workingDays);
                pstmt.setDouble(8, sss);
                pstmt.setDouble(9, philhealth);
                pstmt.setDouble(10, pagibig);
                pstmt.setDouble(11, tax);
                pstmt.setInt(12, id);
                pstmt.executeUpdate();
            }
        }
    }

    public void delete() throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public static ArrayList<Employee> loadAll() throws SQLException {
        ArrayList<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Employee emp = new Employee(
                    rs.getString("name"),
                    rs.getString("position"),
                    rs.getDouble("hourly_rate"),
                    rs.getInt("working_days")
                );
                emp.id = rs.getInt("id");
                emp.baseSalary = rs.getDouble("base_salary");
                emp.hoursWorked = rs.getDouble("hours_worked");
                emp.overtimeHours = rs.getDouble("overtime_hours");
                emp.sss = rs.getDouble("sss");
                emp.philhealth = rs.getDouble("philhealth");
                emp.pagibig = rs.getDouble("pagibig");
                emp.tax = rs.getDouble("tax");
                employees.add(emp);
            }
        }
        return employees;
    }
}