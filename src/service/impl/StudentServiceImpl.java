package service.impl;

import model.Student;
import service.StudentService;
import util.DBUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentServiceImpl extends UnicastRemoteObject implements StudentService {

    public StudentServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<Student> getAllStudents() throws RemoteException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.msv, s.ten, s.ngay_sinh, s.que_quan, " +
                     "COALESCE(AVG(sc.diem), 0) AS diem_tb " +
                     "FROM Student s LEFT JOIN Score sc ON s.msv = sc.msv " +
                     "GROUP BY s.msv";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LocalDate ngaySinh = rs.getString("ngay_sinh") != null
                        ? LocalDate.parse(rs.getString("ngay_sinh"))
                        : null;

                list.add(new Student(
                        rs.getString("msv"),
                        rs.getString("ten"),
                        ngaySinh,
                        rs.getString("que_quan"),
                        rs.getDouble("diem_tb")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Student findStudentById(String msv) throws RemoteException {
        String sql = "SELECT msv, ten, ngay_sinh, que_quan FROM Student WHERE msv = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, msv);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LocalDate ngaySinh = rs.getString("ngay_sinh") != null
                        ? LocalDate.parse(rs.getString("ngay_sinh"))
                        : null;

                return new Student(
                        rs.getString("msv"),
                        rs.getString("ten"),
                        ngaySinh,
                        rs.getString("que_quan"),
                        0.0
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addStudent(Student student) throws RemoteException {
        String sql = "INSERT INTO Student(msv, ten, ngay_sinh, que_quan) VALUES(?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getMsv());
            ps.setString(2, student.getTen());
            ps.setString(3, student.getNgaySinh() != null ? student.getNgaySinh().toString() : null);
            ps.setString(4, student.getQueQuan());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateStudent(Student student) throws RemoteException {
        String sql = "UPDATE Student SET ten=?, ngay_sinh=?, que_quan=? WHERE msv=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getTen());
            ps.setString(2, student.getNgaySinh() != null ? student.getNgaySinh().toString() : null);
            ps.setString(3, student.getQueQuan());
            ps.setString(4, student.getMsv());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteStudent(String msv) throws RemoteException {
        String sql = "DELETE FROM Student WHERE msv=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, msv);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
