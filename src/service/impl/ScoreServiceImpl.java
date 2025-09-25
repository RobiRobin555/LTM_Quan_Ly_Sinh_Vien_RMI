package service.impl;

import model.Score;
import service.ScoreService;
import util.DBUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreServiceImpl extends UnicastRemoteObject implements ScoreService {

    public ScoreServiceImpl() throws RemoteException {
        super();
        createTableIfNotExists();
    }

    // ============================
    // Tạo bảng Score nếu chưa có
    // ============================
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS Score (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "msv TEXT NOT NULL, " +
                     "ma_mon TEXT NOT NULL, " +
                     "diem REAL CHECK (diem >= 0 AND diem <= 10), " +
                     "FOREIGN KEY (msv) REFERENCES Student(msv) ON DELETE CASCADE, " +
                     "FOREIGN KEY (ma_mon) REFERENCES Subject(ma_mon) ON DELETE CASCADE, " +
                     "UNIQUE(msv, ma_mon))";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================
    // Lấy toàn bộ điểm
    // ============================
    @Override
    public List<Score> getAllScores() throws RemoteException {
        List<Score> list = new ArrayList<>();
        String sql = "SELECT * FROM Score";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Score(
                        rs.getInt("id"),
                        rs.getString("msv"),
                        rs.getString("ma_mon"),
                        rs.getDouble("diem")
                ));
            }
        } catch (SQLException e) {
            throw new RemoteException("Lỗi khi lấy danh sách điểm", e);
        }
        return list;
    }

    // ============================
    // Tìm điểm theo id
    // ============================
    @Override
    public Score findScoreById(int id) throws RemoteException {
        String sql = "SELECT * FROM Score WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Score(
                        rs.getInt("id"),
                        rs.getString("msv"),
                        rs.getString("ma_mon"),
                        rs.getDouble("diem")
                );
            }
        } catch (SQLException e) {
            throw new RemoteException("Lỗi khi tìm điểm", e);
        }
        return null;
    }

    // ============================
    // Thêm điểm
    // ============================
    @Override
    public void addScore(Score score) throws RemoteException {
        String sql = "INSERT INTO Score(msv, ma_mon, diem) VALUES(?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, score.getMsv());
            ps.setString(2, score.getMaMon());
            ps.setDouble(3, score.getDiem());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RemoteException("Lỗi khi thêm điểm", e);
        }
    }

    // ============================
    // Cập nhật điểm
    // ============================
    @Override
    public void updateScore(Score score) throws RemoteException {
        String sql = "UPDATE Score SET diem = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, score.getDiem());
            ps.setInt(2, score.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RemoteException("Lỗi khi cập nhật điểm", e);
        }
    }

    // ============================
    // Xóa điểm
    // ============================
    @Override
    public void deleteScore(int id) throws RemoteException {
        String sql = "DELETE FROM Score WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RemoteException("Lỗi khi xóa điểm", e);
        }
    }


}
