package client.panel;

import model.Subject;
import service.SubjectService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SubjectPanel extends JPanel {

    private final SubjectService subjectService;
    private JTable subjectTable;
    private DefaultTableModel subjectModel;

    public SubjectPanel(SubjectService subjectService) {
        this.subjectService = subjectService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(26, 188, 156), 2, true),
                " Quản lý Môn học",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(44, 62, 80)
        ));
        setBackground(new Color(245, 247, 250));

        // Bảng Môn học
        subjectModel = new DefaultTableModel(
                new Object[]{"Mã môn", "Tên môn", "Số tín chỉ", "Số lượng ĐK"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        subjectTable = new JTable(subjectModel);
        subjectTable.setRowHeight(28);
        subjectTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subjectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        subjectTable.getTableHeader().setBackground(new Color(200, 255, 235));
        subjectTable.setSelectionBackground(new Color(102, 205, 170));
        subjectTable.setGridColor(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(subjectTable);
        add(scrollPane, BorderLayout.CENTER);

        // Nút chức năng
        JButton btnAdd = createStyledButton(" Thêm", new Color(46, 204, 113));
        JButton btnEdit = createStyledButton(" Sửa", new Color(52, 152, 219));
        JButton btnDelete = createStyledButton(" Xóa", new Color(231, 76, 60));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.setBackground(new Color(245, 247, 250));
        south.add(btnAdd);
        south.add(btnEdit);
        south.add(btnDelete);
        add(south, BorderLayout.SOUTH);

        // Load danh sách
        loadSubjects();

        // Sự kiện
        btnAdd.addActionListener(e -> addSubjectDialog());
        btnEdit.addActionListener(e -> editSubjectDialog(getSelectedSubject()));
        btnDelete.addActionListener(e -> deleteSubjectAction());
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

    private Subject getSelectedSubject() {
        int row = subjectTable.getSelectedRow();
        if (row == -1) return null;
        String maMon = (String) subjectModel.getValueAt(row, 0);
        String tenMon = (String) subjectModel.getValueAt(row, 1);
        int tinChi = Integer.parseInt(subjectModel.getValueAt(row, 2).toString());
        int soLuong = Integer.parseInt(subjectModel.getValueAt(row, 3).toString());
        return new Subject(maMon, tenMon, soLuong, tinChi);
    }

    private void loadSubjects() {
        try {
            List<Subject> list = subjectService.getAllSubjects();
            subjectModel.setRowCount(0);
            for (Subject s : list) {
                subjectModel.addRow(new Object[]{s.getMaMon(), s.getTenMon(), s.getSoTinChi(), s.getSoLuongDangKy()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi load môn học: " + e.getMessage());
        }
    }

    // ============================
    // Dialog thêm môn học
    // ============================
    private void addSubjectDialog() {
        JDialog dialog = createDialog("➕ Thêm Môn học");

        JTextField txtMaMon = new JTextField();
        JTextField txtTenMon = new JTextField();
        JTextField txtTinChi = new JTextField();

        JPanel form = createFormPanel(
                new String[]{"Mã môn:", "Tên môn:", "Số tín chỉ:"},
                new JComponent[]{txtMaMon, txtTenMon, txtTinChi}
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
                String maMon = txtMaMon.getText().trim();
                String tenMon = txtTenMon.getText().trim();
                String tinChiStr = txtTinChi.getText().trim();

                if (maMon.isEmpty() || tenMon.isEmpty() || tinChiStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Vui lòng nhập đầy đủ thông tin!");
                    return;
                }
                int tinChi;
                try {
                    tinChi = Integer.parseInt(tinChiStr);
                    if (tinChi <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Số tín chỉ phải là số nguyên dương!");
                    return;
                }

                if (subjectService.findSubjectById(maMon) != null) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Mã môn đã tồn tại!");
                    return;
                }

                Subject s = new Subject(maMon, tenMon, 0, tinChi);
                subjectService.addSubject(s);
                JOptionPane.showMessageDialog(dialog, "✅ Thêm thành công!");
                loadSubjects();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Lỗi thêm môn học: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ============================
    // Dialog sửa môn học
    // ============================
    private void editSubjectDialog(Subject s) {
        if (s == null) {
            JOptionPane.showMessageDialog(this, "⚠ Vui lòng chọn môn học để sửa!");
            return;
        }

        JDialog dialog = createDialog("✏️ Sửa Môn học");

        JTextField txtMaMon = new JTextField(s.getMaMon());
        txtMaMon.setEditable(false);
        JTextField txtTenMon = new JTextField(s.getTenMon());
        JTextField txtTinChi = new JTextField(String.valueOf(s.getSoTinChi()));

        JPanel form = createFormPanel(
                new String[]{"Mã môn:", "Tên môn:", "Số tín chỉ:"},
                new JComponent[]{txtMaMon, txtTenMon, txtTinChi}
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
                String tenMon = txtTenMon.getText().trim();
                String tinChiStr = txtTinChi.getText().trim();

                if (tenMon.isEmpty() || tinChiStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Không được để trống!");
                    return;
                }
                int tinChi;
                try {
                    tinChi = Integer.parseInt(tinChiStr);
                    if (tinChi <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "⚠ Số tín chỉ không hợp lệ!");
                    return;
                }

                Subject updated = new Subject(
                        s.getMaMon(),
                        tenMon,
                        s.getSoLuongDangKy(),
                        tinChi
                );
                subjectService.updateSubject(updated);
                JOptionPane.showMessageDialog(dialog, "✅ Cập nhật thành công!");
                loadSubjects();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Lỗi khi cập nhật: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ============================
    // Xóa môn học
    // ============================
    private void deleteSubjectAction() {
        Subject s = getSelectedSubject();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "⚠ Chọn môn học để xóa");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa môn học " + s.getTenMon() + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                subjectService.deleteSubject(s.getMaMon());
                loadSubjects();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi xóa môn học: " + e.getMessage());
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
