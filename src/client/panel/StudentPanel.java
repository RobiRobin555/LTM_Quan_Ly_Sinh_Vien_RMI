package client.panel;

import model.Score;
import model.Student;
import model.Subject;
import service.ScoreService;
import service.StudentService;
import service.SubjectService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

import com.formdev.flatlaf.icons.FlatRevealIcon;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StudentPanel extends JPanel {

    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ScoreService scoreService;

    private JTable studentTable;
    private DefaultTableModel studentModel;
    private TableRowSorter<DefaultTableModel> sorter;

    public StudentPanel(StudentService studentService, SubjectService subjectService, ScoreService scoreService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.scoreService = scoreService;

        initUI();
        loadStudents();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        // Header (tiêu đề phía trên bảng)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(227, 242, 253)); // #e3f2fd
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 225, 245)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("📚 Quản lý Sinh viên");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(33, 150, 243));
        header.add(title, BorderLayout.WEST);

        // Search + Refresh panel (phía trên bảng)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);

        JTextField tfSearch = new JTextField(24);
        tfSearch.setToolTipText("Tìm kiếm (tìm trên tất cả cột)");
        JButton btnRefresh = new JButton();
        btnRefresh.setPreferredSize(new Dimension(36, 28));

        // icon cho search và refresh
        try {
            FlatSearchIcon searchIcon = new FlatSearchIcon();
            FlatRevealIcon refreshIcon = new FlatRevealIcon();
            tfSearch.putClientProperty("JTextField.placeholderText", ""); // không dùng placeholder per yêu cầu
            btnRefresh.setIcon(refreshIcon);
        } catch (Exception ignored) {
        }
        btnRefresh.setToolTipText("Làm mới dữ liệu");

        searchPanel.add(tfSearch);
        searchPanel.add(btnRefresh);

        header.add(searchPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Bảng Sinh viên
        studentModel = new DefaultTableModel(new Object[]{"MSV", "Tên", "Ngày sinh", "Quê quán", "Điểm TB"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        studentTable = new JTable(studentModel);
        studentTable.setRowHeight(28);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        studentTable.setGridColor(new Color(230,230,230));
        studentTable.setSelectionBackground(new Color(212, 235, 255));

        // center align điểm
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        studentTable.getColumnModel().getColumn(4).setCellRenderer(center);

        sorter = new TableRowSorter<>(studentModel);
        studentTable.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(studentTable);
        add(scroll, BorderLayout.CENTER);

        // Footer buttons (Add / Edit / Delete)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setBackground(Color.WHITE);

        JButton btnAdd = styledButton("➕ Thêm", new Color(39, 174, 96));
        JButton btnEdit = styledButton("✏️ Sửa", new Color(41, 128, 185));
        JButton btnDelete = styledButton("🗑 Xóa", new Color(231, 76, 60));
        JButton btnDetail = styledButton("🔎 Chi tiết", new Color(108, 99, 255));
        JButton btnRefreshBottom = styledButton("Làm mới", new Color(96, 125, 139));

        footer.add(btnDetail);
        footer.add(btnAdd);
        footer.add(btnEdit);
        footer.add(btnDelete);
        footer.add(btnRefreshBottom);

        add(footer, BorderLayout.SOUTH);

        // Event: search realtime
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String text = tfSearch.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                try {
                    // (?i) không phân biệt hoa thường, lọc tất cả cột
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                } catch (PatternSyntaxException ex) {
                    sorter.setRowFilter(null);
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        // Event: refresh
        btnRefresh.addActionListener(e -> loadStudents());
        btnRefreshBottom.addActionListener(e -> loadStudents());

        // CRUD events
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn sinh viên để sửa"); return; }
            int modelRow = studentTable.convertRowIndexToModel(row);
            Student s = getStudentFromModel(modelRow);
            showEditDialog(s);
        });
        btnDelete.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn sinh viên để xóa"); return; }
            int modelRow = studentTable.convertRowIndexToModel(row);
            Student s = getStudentFromModel(modelRow);
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa sinh viên " + s.getMsv() + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    studentService.deleteStudent(s.getMsv());
                    // đảm bảo xóa điểm liên quan (nếu DB có cascade thì DB sẽ xử lý)
                    loadStudents();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnDetail.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn sinh viên để xem chi tiết"); return; }
            int modelRow = studentTable.convertRowIndexToModel(row);
            Student s = getStudentFromModel(modelRow);
            showDetailDialog(s);
        });

        // double-click để xem chi tiết
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = studentTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = studentTable.convertRowIndexToModel(row);
                        Student s = getStudentFromModel(modelRow);
                        showDetailDialog(s);
                    }
                }
            }
        });
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(110, 32));
        return b;
    }

    private Student getStudentFromModel(int row) {
        String msv = (String) studentModel.getValueAt(row, 0);
        String ten = (String) studentModel.getValueAt(row, 1);
        LocalDate ngaySinh = studentModel.getValueAt(row, 2) != null ?
                LocalDate.parse(studentModel.getValueAt(row, 2).toString()) : null;
        String queQuan = (String) studentModel.getValueAt(row, 3);
        double diem = 0.0;
        try { diem = Double.parseDouble(studentModel.getValueAt(row, 4).toString()); } catch (Exception ignored) {}
        return new Student(msv, ten, ngaySinh, queQuan, diem);
    }

    // =========================
    // Load dữ liệu
    // =========================
    public void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentModel.setRowCount(0);
            for (Student s : students) {
                studentModel.addRow(new Object[]{s.getMsv(), s.getTen(),
                        s.getNgaySinh() != null ? s.getNgaySinh().toString() : "",
                        s.getQueQuan(), s.getDiemTB()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy danh sách sinh viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================
    // Dialog thêm / sửa
    // =========================
    private void showAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Thêm sinh viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(new EmptyBorder(12,12,12,12));

        JTextField tfMsv = new JTextField();
        JTextField tfTen = new JTextField();
        JTextField tfNgaySinh = new JTextField("2000-01-01");
        JTextField tfQueQuan = new JTextField();

        form.add(new JLabel("MSV:")); form.add(tfMsv);
        form.add(new JLabel("Tên:")); form.add(tfTen);
        form.add(new JLabel("Ngày sinh (yyyy-MM-dd):")); form.add(tfNgaySinh);
        form.add(new JLabel("Quê quán:")); form.add(tfQueQuan);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = styledButton("Lưu", new Color(39, 174, 96));
        JButton btnCancel = styledButton("Hủy", new Color(149, 165, 166));
        buttons.add(btnSave); buttons.add(btnCancel);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            // Validation cơ bản
            if (tfMsv.getText().trim().isEmpty() || tfTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "MSV và Tên không được để trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                LocalDate.parse(tfNgaySinh.getText().trim()); // nếu sai sẽ ném exception
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày sinh phải dạng yyyy-MM-dd", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Student s = new Student(tfMsv.getText().trim(), tfTen.getText().trim(),
                        LocalDate.parse(tfNgaySinh.getText().trim()), tfQueQuan.getText().trim(), 0.0);
                studentService.addStudent(s);
                loadStudents();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showEditDialog(Student s) {
        if (s == null) return;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Sửa sinh viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(new EmptyBorder(12,12,12,12));

        JTextField tfMsv = new JTextField(s.getMsv());
        tfMsv.setEditable(false);
        JTextField tfTen = new JTextField(s.getTen());
        JTextField tfNgaySinh = new JTextField(s.getNgaySinh() != null ? s.getNgaySinh().toString() : "");
        JTextField tfQueQuan = new JTextField(s.getQueQuan());

        form.add(new JLabel("MSV:")); form.add(tfMsv);
        form.add(new JLabel("Tên:")); form.add(tfTen);
        form.add(new JLabel("Ngày sinh (yyyy-MM-dd):")); form.add(tfNgaySinh);
        form.add(new JLabel("Quê quán:")); form.add(tfQueQuan);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = styledButton("Lưu", new Color(41, 128, 185));
        JButton btnCancel = styledButton("Hủy", new Color(149, 165, 166));
        buttons.add(btnSave); buttons.add(btnCancel);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            if (tfTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên không được để trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                LocalDate.parse(tfNgaySinh.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày sinh phải dạng yyyy-MM-dd", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Student updated = new Student(s.getMsv(), tfTen.getText().trim(),
                        LocalDate.parse(tfNgaySinh.getText().trim()), tfQueQuan.getText().trim(), s.getDiemTB());
                studentService.updateStudent(updated);
                loadStudents();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // Chi tiết: hiển thị thông tin sinh viên + bảng điểm
    private void showDetailDialog(Student s) {
        if (s == null) return;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết sinh viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(760, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10,10));

        JPanel info = new JPanel(new GridLayout(0,1));
        info.setBorder(BorderFactory.createTitledBorder("Thông tin sinh viên"));
        info.add(new JLabel("MSV: " + s.getMsv()));
        info.add(new JLabel("Tên: " + s.getTen()));
        info.add(new JLabel("Ngày sinh: " + (s.getNgaySinh()!=null ? s.getNgaySinh().toString() : "")));
        info.add(new JLabel("Quê quán: " + s.getQueQuan()));
        info.add(new JLabel("Điểm trung bình: " + s.getDiemTB()));

        dialog.add(info, BorderLayout.NORTH);

        DefaultTableModel scModel = new DefaultTableModel(new Object[]{"Mã môn","Tên môn","Điểm"},0);
        JTable scTable = new JTable(scModel);
        scTable.setRowHeight(24);

        try {
            List<Score> scores = scoreService.getAllScores();
            for (Score sc : scores) {
                if (sc.getMsv().equals(s.getMsv())) {
                    Subject subj = subjectService.findSubjectById(sc.getMaMon());
                    scModel.addRow(new Object[]{sc.getMaMon(), subj!=null?subj.getTenMon():sc.getMaMon(), sc.getDiem()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi load điểm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        dialog.add(new JScrollPane(scTable), BorderLayout.CENTER);
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bClose = styledButton("Đóng", new Color(125, 134, 139));
        bClose.addActionListener(e -> dialog.dispose());
        btnP.add(bClose);
        dialog.add(btnP, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
