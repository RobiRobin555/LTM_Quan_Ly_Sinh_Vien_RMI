package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private static final String URL = "jdbc:sqlite:student_db.sqlite";

    static {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                // Bảng Student
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Student (" +
                    "msv TEXT PRIMARY KEY," +
                    "ten TEXT NOT NULL," +
                    "ngay_sinh DATE," +
                    "que_quan TEXT)"
                );

                // Bảng Subject
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Subject (" +
                    "ma_mon TEXT PRIMARY KEY," +
                    "ten_mon TEXT NOT NULL," +
                    "so_tin_chi INTEGER NOT NULL)" 
          
                );

                // Bảng Score
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Score (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "msv TEXT NOT NULL," +
                    "ma_mon TEXT NOT NULL," +
                    "diem REAL CHECK (diem >= 0 AND diem <= 10)," +
                    "FOREIGN KEY (msv) REFERENCES Student(msv) ON DELETE CASCADE," +
                    "FOREIGN KEY (ma_mon) REFERENCES Subject(ma_mon) ON DELETE CASCADE," +
                    "UNIQUE(msv, ma_mon))"
                );

                System.out.println(">>> Database và bảng đã sẵn sàng.");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
