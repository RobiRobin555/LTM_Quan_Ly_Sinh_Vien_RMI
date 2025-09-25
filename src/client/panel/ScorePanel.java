package client.panel;

import model.Score;
import model.Student;
import model.Subject;
import service.ScoreService;
import service.StudentService;
import service.SubjectService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ScorePanel extends JPanel {

    private final ScoreService scoreService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private JTable scoreTable;
    private DefaultTableModel scoreModel;

    public ScorePanel(ScoreService scoreService, StudentService studentService, SubjectService subjectService) {
        this.scoreService = scoreService;
        this.studentService = studentService;
        this.subjectService = subjectService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2, true),
                " Quản lý Điểm",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(52, 73, 94)
        ));
        setBackground(new Color(250, 248, 255));

        // Bảng điểm
        scoreModel = new DefaultTableModel(
                new Object[]{"MSV", "Tên SV", "Mã môn", "Tên môn", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        scoreTable = new JTable(scoreModel);
        scoreTable.setRowHeight(28);
        scoreTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        scoreTable.getTableHeader().setBackground(new Color(230, 220, 255));
        scoreTable.setSelectionBackground(new Color(186, 145, 221));
        scoreTable.setGridColor(new Color(210, 210, 210));

        add(new JScrollPane(scoreTable), BorderLayout.CENTER);

        // Nút chức năng
        JButton btnAdd = createStyledButton(" Thêm", new Color(46, 204, 113));
        JButton btnEdit = createStyledButton(" Sửa", new Color(52, 152, 219));
        JButton btnDelete = createStyledButton(" Xóa", new Color(231, 76, 60));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.setBackground(new Color(250, 248, 255));
        south.add(btnAdd);
        south.add(btnEdit);
        south.add(btnDelete);
        add(south, BorderLayout.SOUTH);

        // Load dữ liệu
        loadScores();

        // Sự kiện
        btnAdd.addActionListener(e -> addScoreDialog());
        btnEdit.addActionListener(e -> editScoreDialog(getSelectedScore()));
        btnDelete.addActionListener(e -> deleteScoreAction());
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 38));
        return btn;
    }

    private Score getSelectedScore() {
        int row = scoreTable.getSelectedRow();
        if (row == -1) return null;
        String msv = (String) scoreModel.getValueAt(row, 0);
        String maMon = (String) scoreModel.getValueAt(row, 2);
        double diem = Double.parseDouble(scoreModel.getValueAt(row, 4).toString());
        return new Score(msv, maMon, diem);
    }

    private void loadScores() {
        try {
            List<Score> scores = scoreService.getAllScores();
            scoreModel.setRowCount(0);
            for (Score sc : scores) {
                Student st = studentService.findStudentById(sc.getMsv());
                Subject subj = subjectService.findSubjectById(sc.getMaMon());
                scoreModel.addRow(new Object[]{
                        sc.getMsv(),
                        st != null ? st.getTen() : "N/A",
                        sc.getMaMon(),
                        subj != null ? subj.getTenMon() : "N/A",
                        sc.getDiem()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi load điểm: " + e.getMessage());
        }
    }

    // ============================
    // Dialog thêm điểm
    // ============================
    private void addScoreDialog() {
        JDialog dialog = createDialog("➕ Thêm Điểm");

        JComboBox<String> cbMsv = new JComboBox<>();
        JComboBox<String> cbMaMon = new JComboBox<>();
        JTextField txtDiem = new JTextField();

        try {
            for (Student st : studentService.getAllStudents()) cbMsv.addItem(st.getMsv());
            for (Subject sub : subjectService.getAllSubjects()) cbMaMon.addItem(sub.getMaMon());
        } catch (Exception ignored) {}

        JPanel form = createFormPanel(
                new String[]{"Mã SV:", "Mã môn:", "Điểm:"},
                new JComponent[]{cbMsv, cbMaMon, txtDiem}
        );
        dialog.add(form, BorderLayout.CENTER);

        JButton btnSave = createStyledButton("💾 Lưu", new Color(39, 174, 96));
        JButton btnCancel = createStyledButton("❌ Hủy", new Color(149, 165, 166));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnSave);
        bottom.add(btnCancel);
        dialog.add(bottom, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            try {
                String msv = (String) cbMsv.getSelectedItem();
                String maMon = (String) cbMaMon.getSelectedItem();
                String diemStr = txtDiem.getText().trim();

                if (msv == null || maMon == null || diemStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Vui lòng nhập đầy đủ!");
                    return;
                }
                double diem;
                try {
                    diem = Double.parseDouble(diemStr);
                    if (diem < 0 || diem > 10) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Điểm phải nằm trong khoảng 0 - 10!");
                    return;
                }

                // Tránh trùng
                for (int i = 0; i < scoreModel.getRowCount(); i++) {
                    if (scoreModel.getValueAt(i, 0).equals(msv) &&
                        scoreModel.getValueAt(i, 2).equals(maMon)) {
                        JOptionPane.showMessageDialog(dialog, "⚠ Điểm cho SV và môn này đã tồn tại!");
                        return;
                    }
                }

                Score sc = new Score(msv, maMon, diem);
                scoreService.addScore(sc);
                JOptionPane.showMessageDialog(dialog, "✅ Thêm thành công!");
                loadScores();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Lỗi thêm điểm: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ============================
    // Dialog sửa điểm
    // ============================
    private void editScoreDialog(Score s) {
        if (s == null) {
            JOptionPane.showMessageDialog(this, "⚠ Chọn điểm để sửa!");
            return;
        }

        JDialog dialog = createDialog("✏️ Sửa Điểm");

        JTextField txtMsv = new JTextField(s.getMsv());
        txtMsv.setEditable(false);
        JTextField txtMaMon = new JTextField(s.getMaMon());
        txtMaMon.setEditable(false);
        JTextField txtDiem = new JTextField(String.valueOf(s.getDiem()));

        JPanel form = createFormPanel(
                new String[]{"Mã SV:", "Mã môn:", "Điểm:"},
                new JComponent[]{txtMsv, txtMaMon, txtDiem}
        );
        dialog.add(form, BorderLayout.CENTER);

        JButton btnSave = createStyledButton("💾 Lưu", new Color(41, 128, 185));
        JButton btnCancel = createStyledButton("❌ Hủy", new Color(149, 165, 166));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnSave);
        bottom.add(btnCancel);
        dialog.add(bottom, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            try {
                double diem;
                try {
                    diem = Double.parseDouble(txtDiem.getText().trim());
                    if (diem < 0 || diem > 10) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Điểm phải hợp lệ (0-10)!");
                    return;
                }

                Score updated = new Score(s.getMsv(), s.getMaMon(), diem);
                scoreService.updateScore(updated);
                JOptionPane.showMessageDialog(dialog, "✅ Cập nhật thành công!");
                loadScores();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Lỗi cập nhật điểm: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ============================
    // Xóa điểm
    // ============================
    private void deleteScoreAction() {
        int row = scoreTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "⚠ Vui lòng chọn điểm để xóa!");
            return;
        }
        String msv = (String) scoreModel.getValueAt(row, 1);
        String maMon = (String) scoreModel.getValueAt(row, 3);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa điểm của sinh viên " + msv + " cho môn " + maMon + " ?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                scoreService.deleteScore(row);
                loadScores();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa điểm: " + e.getMessage());
            }
        }
    }

    // ============================
    // Helper
    // ============================
    private JDialog createDialog(String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(250, 248, 255));
        return dialog;
    }

    private JPanel createFormPanel(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(250, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            panel.add(fields[i], gbc);
        }
        return panel;
    }
}

    // ================================
    // Xóa điểm
    // ================================


 

