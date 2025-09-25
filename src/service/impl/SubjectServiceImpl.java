package service.impl;

import model.Subject;
import service.SubjectService;
import util.DBUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectServiceImpl extends UnicastRemoteObject implements SubjectService {

    private static final long serialVersionUID = 1L;
    private final Connection conn;

    public SubjectServiceImpl() throws RemoteException, SQLException {
        super();
        this.conn = DBUtil.getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement st = conn.createStatement()) {
            String sql = """
                    CREATE TABLE IF NOT EXISTS subject (
                        ma_mon VARCHAR(20) PRIMARY KEY,
                        ten_mon VARCHAR(100) NOT NULL,
                        so_tin_chi INT NOT NULL
                    )
                    """;
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Subject> getAllSubjects() throws RemoteException {
        List<Subject> list = new ArrayList<>();
        String sql =
                "SELECT s.ma_mon, s.ten_mon, s.so_tin_chi, " +
                "       COUNT(sc.msv) AS so_luong_dang_ky " +
                "FROM subject s " +
                "LEFT JOIN score sc ON s.ma_mon = sc.ma_mon " +
                "GROUP BY s.ma_mon, s.ten_mon, s.so_tin_chi";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Subject s = new Subject(
                        rs.getString("ma_mon"),
                        rs.getString("ten_mon"),
                        rs.getInt("so_tin_chi"),
                        rs.getInt("so_luong_dang_ky")
                );
                list.add(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách môn học", e);
        }
        return list;
    }

    @Override
    public Subject findSubjectById(String maMon) throws RemoteException {
        String sql = "SELECT s.ma_mon, s.ten_mon, s.so_tin_chi, " +
                     "COUNT(sc.msv) AS so_luong_dang_ky " +
                     "FROM subject s " +
                     "LEFT JOIN score sc ON s.ma_mon = sc.ma_mon " +
                     "WHERE s.ma_mon = ? " +
                     "GROUP BY s.ma_mon, s.ten_mon, s.so_tin_chi";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Subject(
                            rs.getString("ma_mon"),
                            rs.getString("ten_mon"),
                            rs.getInt("so_tin_chi"),
                            rs.getInt("so_luong_dang_ky")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RemoteException("Lỗi khi tìm môn học", e);
        }
        return null;
    }

    @Override
    public void addSubject(Subject s) throws RemoteException {
        String sql = "INSERT INTO subject(ma_mon, ten_mon, so_tin_chi) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getMaMon());
            ps.setString(2, s.getTenMon());
            ps.setInt(3, s.getSoTinChi());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi thêm môn học", e);
        }
    }

    @Override
    public void updateSubject(Subject s) throws RemoteException {
        String sql = "UPDATE subject SET ten_mon=?, so_tin_chi=? WHERE ma_mon=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getTenMon());
            ps.setInt(2, s.getSoTinChi());
            ps.setString(3, s.getMaMon());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi cập nhật môn học", e);
        }
    }

    @Override
    public void deleteSubject(String maMon) throws RemoteException {
        String sql = "DELETE FROM subject WHERE ma_mon=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi xóa môn học", e);
        }
    }
}
