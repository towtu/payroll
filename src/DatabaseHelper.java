import java.sql.*;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:payroll.db";

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = """
                CREATE TABLE IF NOT EXISTS employees (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    position TEXT,
                    base_salary REAL,
                    hourly_rate REAL,
                    hours_worked REAL,
                    overtime_hours REAL,
                    working_days INTEGER,
                    sss REAL,
                    philhealth REAL,
                    pagibig REAL,
                    tax REAL
                )""";
            stmt.execute(sql);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}