package client.panel;

import model.Score;
import model.Student;
import model.Subject;
import service.ScoreService;
import service.StudentService;
import service.SubjectService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class StudentPanel extends JPanel {

    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ScoreService scoreService;
    private JTable studentTable;
    private DefaultTableModel studentModel;

    public StudentPanel(StudentService studentService, SubjectService subjectService, ScoreService scoreService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.scoreService = scoreService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                " Quản lý Sinh viên",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(44, 62, 80)
        ));
        setBackground(new Color(245, 247, 250));

        // Bảng Sinh viên
        studentModel = new DefaultTableModel(
                new Object[]{"MSV", "Tên", "Ngày sinh", "Quê quán", "Điểm TB"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentTable = new JTable(studentModel);
        studentTable.setRowHeight(28);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        studentTable.getTableHeader().setBackground(new Color(200, 230, 255));
        studentTable.setSelectionBackground(new Color(135, 206, 250));
        studentTable.setGridColor(new Color(220, 220, 220));

        // Căn giữa cột Điểm TB
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        studentTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        add(scrollPane, BorderLayout.CENTER);

        // Nút chức năng
        JButton btnAdd = createStyledButton(" Thêm", new Color(46, 204, 113));
        JButton btnEdit = createStyledButton(" Sửa", new Color(52, 152, 219));
        JButton btnDelete = createStyledButton(" Xóa", new Color(231, 76, 60));
        JButton btnRefresh = createStyledButton(" Làm mới", new Color(155, 89, 182));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.setBackground(new Color(245, 249, 255));
        south.add(btnAdd);
        south.add(btnEdit);
        south.add(btnDelete);
        south.add(btnRefresh);
        add(south, BorderLayout.SOUTH);

        // Load danh sách sinh viên
        loadStudents();

        // Sự kiện các nút
        btnAdd.addActionListener(e -> addStudentDialog());
        btnEdit.addActionListener(e -> editStudentDialog(getSelectedStudent()));
        btnDelete.addActionListener(e -> deleteStudentAction());
        btnRefresh.addActionListener(e -> loadStudents());


        // Double click -> xem chi tiết
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && studentTable.getSelectedRow() != -1) {
                    Student s = getSelectedStudent();
                    showStudentDetail(s);
                }
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 38));
        return button;
    }

    private Student getSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) return null;
        String msv = (String) studentModel.getValueAt(row, 0);
        String ten = (String) studentModel.getValueAt(row, 1);
        LocalDate ngaySinh = LocalDate.parse(studentModel.getValueAt(row, 2).toString());
        String queQuan = (String) studentModel.getValueAt(row, 3);
        double diemTB = Double.parseDouble(studentModel.getValueAt(row, 4).toString());
        return new Student(msv, ten, ngaySinh, queQuan, diemTB);
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentModel.setRowCount(0);
            for (Student s : students) {
                studentModel.addRow(new Object[]{s.getMsv(), s.getTen(), s.getNgaySinh(),
                        s.getQueQuan(), s.getDiemTB()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi load dữ liệu: " + e.getMessage());
        }
    }

    // ============================
    // Dialog thêm sinh viên
    // ============================
    private void addStudentDialog() {
        JDialog dialog = createDialog("➕ Thêm Sinh viên");

        JTextField txtMsv = new JTextField();
        JTextField txtTen = new JTextField();
        JTextField txtNgaySinh = new JTextField("2000-01-01");
        JTextField txtQueQuan = new JTextField();

        JPanel form = createFormPanel(
                new String[]{"Mã SV:", "Tên:", "Ngày sinh (yyyy-MM-dd):", "Quê quán:"},
                new JComponent[]{txtMsv, txtTen, txtNgaySinh, txtQueQuan}
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
                String msv = txtMsv.getText().trim();
                String ten = txtTen.getText().trim();
                String ngaySinhStr = txtNgaySinh.getText().trim();
                String queQuan = txtQueQuan.getText().trim();

                if (msv.isEmpty() || ten.isEmpty() || queQuan.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Vui lòng nhập đầy đủ thông tin!");
                    return;
                }
                try {
                    LocalDate.parse(ngaySinhStr);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Ngày sinh không hợp lệ!");
                    return;
                }

                if (studentService.findStudentById(msv) != null) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Mã SV đã tồn tại!");
                    return;
                }

                Student s = new Student(msv, ten, LocalDate.parse(ngaySinhStr), queQuan, 0.0);
                studentService.addStudent(s);
                JOptionPane.showMessageDialog(dialog, "✅ Thêm thành công!");
                loadStudents();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ============================
    // Dialog sửa sinh viên
    // ============================
    private void editStudentDialog(Student s) {
        if (s == null) {
            JOptionPane.showMessageDialog(this, "⚠ Vui lòng chọn sinh viên để sửa!");
            return;
        }

        JDialog dialog = createDialog("✏️ Sửa Sinh viên");

        JTextField txtMsv = new JTextField(s.getMsv());
        txtMsv.setEditable(false);
        JTextField txtTen = new JTextField(s.getTen());
        JTextField txtNgaySinh = new JTextField(s.getNgaySinh().toString());
        JTextField txtQueQuan = new JTextField(s.getQueQuan());

        JPanel form = createFormPanel(
                new String[]{"Mã SV:", "Tên:", "Ngày sinh:", "Quê quán:"},
                new JComponent[]{txtMsv, txtTen, txtNgaySinh, txtQueQuan}
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
                String ten = txtTen.getText().trim();
                String ngaySinhStr = txtNgaySinh.getText().trim();
                String queQuan = txtQueQuan.getText().trim();

                if (ten.isEmpty() || queQuan.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Không được để trống tên hoặc quê quán!");
                    return;
                }
                try {
                    LocalDate.parse(ngaySinhStr);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Ngày sinh không hợp lệ!");
                    return;
                }

                Student updated = new Student(
                        s.getMsv(),
                        ten,
                        LocalDate.parse(ngaySinhStr),
                        queQuan,
                        s.getDiemTB()
                );
                studentService.updateStudent(updated);
                JOptionPane.showMessageDialog(dialog, "✅ Cập nhật thành công!");
                loadStudents();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Lỗi khi cập nhật: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ============================
    // Xóa sinh viên
    // ============================
    private void deleteStudentAction() {
        Student s = getSelectedStudent();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "⚠ Chọn SV để xóa");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa SV " + s.getMsv() + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.deleteStudent(s.getMsv());
                loadStudents();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi xóa SV: " + e.getMessage());
            }
        }
    }

    // ============================
    // Dialog chi tiết sinh viên
    // ============================
    private void showStudentDetail(Student s) {
        if (s == null) return;

        JDialog dialog = createDialog("📖 Chi tiết Sinh viên");
        dialog.setSize(750, 480);

        // Thông tin cơ bản
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin"));
        infoPanel.setBackground(new Color(245, 247, 250));
        infoPanel.add(new JLabel("Mã SV: " + s.getMsv()));
        infoPanel.add(new JLabel("Tên: " + s.getTen()));
        infoPanel.add(new JLabel("Ngày sinh: " + s.getNgaySinh()));
        infoPanel.add(new JLabel("Quê quán: " + s.getQueQuan()));
        infoPanel.add(new JLabel("Điểm TB: " + s.getDiemTB()));
        dialog.add(infoPanel, BorderLayout.NORTH);

        // Bảng điểm
        DefaultTableModel scoreModel = new DefaultTableModel(new Object[]{"Mã môn", "Tên môn", "Điểm"}, 0);
        JTable scoreTable = new JTable(scoreModel);
        scoreTable.setRowHeight(26);
        scoreTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        scoreTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreTable.getTableHeader().setBackground(new Color(220, 220, 220));

        try {
            List<Score> scores = scoreService.getAllScores();
            for (Score sc : scores) {
                if (sc.getMsv().equals(s.getMsv())) {
                    Subject subj = subjectService.findSubjectById(sc.getMaMon());
                    scoreModel.addRow(new Object[]{sc.getMaMon(), subj != null ? subj.getTenMon() : "N/A", sc.getDiem()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi load điểm: " + ex.getMessage());
        }
        dialog.add(new JScrollPane(scoreTable), BorderLayout.CENTER);

        JButton btnClose = createStyledButton("Đóng", new Color(127, 140, 141));
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnClose);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ============================
    // Helper
    // ============================
    private JDialog createDialog(String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(245, 247, 250));
        return dialog;
    }

    private JPanel createFormPanel(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
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
