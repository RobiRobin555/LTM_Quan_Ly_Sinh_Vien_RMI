package client.panel;

import com.formdev.flatlaf.icons.FlatRevealIcon;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import model.Subject;
import service.SubjectService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class SubjectPanel extends JPanel {

    private final SubjectService subjectService;
    private JTable subjectTable;
    private DefaultTableModel subjectModel;
    private TableRowSorter<DefaultTableModel> sorter;

    public SubjectPanel(SubjectService subjectService) {
        this.subjectService = subjectService;
        initUI();
        loadSubjects();
    }

    private void initUI() {
        setLayout(new BorderLayout(12,12));
        setBorder(new EmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        // Header with search + refresh
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(227, 242, 253));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,225,245)),
                BorderFactory.createEmptyBorder(10,12,10,12)
        ));
        JLabel title = new JLabel("📘 Quản lý Môn học");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(33, 150, 243));
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setOpaque(false);
        JTextField tfSearch = new JTextField(24);
        tfSearch.setToolTipText("Tìm kiếm (tìm trên tất cả cột)");
        JButton btnRefresh = new JButton();
        btnRefresh.setPreferredSize(new Dimension(36,28));
        try {
            FlatRevealIcon refresh = new FlatRevealIcon();
            btnRefresh.setIcon(refresh);
        } catch (Exception ignored) {}
        right.add(tfSearch);
        right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Table
        subjectModel = new DefaultTableModel(new Object[]{"Mã môn", "Tên môn", "Số lượng ĐK", "Số tín chỉ"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        subjectTable = new JTable(subjectModel);
        subjectTable.setRowHeight(28);
        subjectTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subjectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        subjectTable.setSelectionBackground(new Color(212,235,255));
        JScrollPane scroll = new JScrollPane(subjectTable);
        add(scroll, BorderLayout.CENTER);

        sorter = new TableRowSorter<>(subjectModel);
        subjectTable.setRowSorter(sorter);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBackground(Color.WHITE);
        JButton btnAdd = styledButton("➕ Thêm", new Color(39, 174, 96));
        JButton btnEdit = styledButton("✏️ Sửa", new Color(41, 128, 185));
        JButton btnDelete = styledButton("🗑 Xóa", new Color(231, 76, 60));
        JButton btnRefreshBottom = styledButton("Làm mới", new Color(96, 125, 139));
        footer.add(btnAdd); footer.add(btnEdit); footer.add(btnDelete); footer.add(btnRefreshBottom);
        add(footer, BorderLayout.SOUTH);

        // listeners
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String t = tfSearch.getText().trim();
                if (t.isEmpty()) { sorter.setRowFilter(null); return; }
                try { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t))); } catch (PatternSyntaxException ex) { sorter.setRowFilter(null); }
            }
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        btnRefresh.addActionListener(e -> loadSubjects());
        btnRefreshBottom.addActionListener(e -> loadSubjects());

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> {
            int row = subjectTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn môn để sửa"); return; }
            int modelRow = subjectTable.convertRowIndexToModel(row);
            String ma = (String) subjectModel.getValueAt(modelRow, 0);
            String ten = (String) subjectModel.getValueAt(modelRow, 1);
            int soTin = Integer.parseInt(subjectModel.getValueAt(modelRow, 3).toString());
            showEditDialog(new Subject(ma, ten, Integer.parseInt(subjectModel.getValueAt(modelRow,2).toString()), soTin));
        });
        btnDelete.addActionListener(e -> {
            int row = subjectTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn môn để xóa"); return; }
            int modelRow = subjectTable.convertRowIndexToModel(row);
            String ma = (String) subjectModel.getValueAt(modelRow, 0);
            int c = JOptionPane.showConfirmDialog(this, "Xóa môn " + ma + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                try { subjectService.deleteSubject(ma); loadSubjects(); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()); }
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

    public void loadSubjects() {
        try {
            List<Subject> list = subjectService.getAllSubjects();
            subjectModel.setRowCount(0);
            for (Subject s : list) {
                subjectModel.addRow(new Object[]{s.getMaMon(), s.getTenMon(), s.getSoLuongDangKy(), s.getSoTinChi()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy môn học: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Thêm môn học", Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(420,240); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(new EmptyBorder(12,12,12,12));
        JTextField tfMa = new JTextField(); JTextField tfTen = new JTextField(); JTextField tfTin = new JTextField("3");
        p.add(new JLabel("Mã môn:")); p.add(tfMa);
        p.add(new JLabel("Tên môn:")); p.add(tfTen);
        p.add(new JLabel("Số tín chỉ:")); p.add(tfTin);
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bs = styledButton("Lưu", new Color(39, 174, 96)); JButton bc = styledButton("Hủy", new Color(149,165,166));
        btnP.add(bs); btnP.add(bc);
        d.add(p, BorderLayout.CENTER); d.add(btnP, BorderLayout.SOUTH);

        bs.addActionListener(e -> {
            if (tfMa.getText().trim().isEmpty() || tfTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Mã và tên không được để trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int soTin = 3;
            try { soTin = Integer.parseInt(tfTin.getText().trim()); if (soTin <= 0) throw new NumberFormatException(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Số tín chỉ phải là số nguyên dương", "Lỗi", JOptionPane.WARNING_MESSAGE); return; }
            try { subjectService.addSubject(new Subject(tfMa.getText().trim(), tfTen.getText().trim(), 0, soTin)); loadSubjects(); d.dispose(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Lỗi khi thêm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });
        bc.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    private void showEditDialog(Subject s) {
        if (s == null) return;
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Sửa môn học", Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(420,240); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(new EmptyBorder(12,12,12,12));
        JTextField tfMa = new JTextField(s.getMaMon()); tfMa.setEditable(false);
        JTextField tfTen = new JTextField(s.getTenMon());
        JTextField tfTin = new JTextField(String.valueOf(s.getSoTinChi()));
        p.add(new JLabel("Mã môn:")); p.add(tfMa);
        p.add(new JLabel("Tên môn:")); p.add(tfTen);
        p.add(new JLabel("Số tín chỉ:")); p.add(tfTin);
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bs = styledButton("Lưu", new Color(41,128,185)); JButton bc = styledButton("Hủy", new Color(149,165,166));
        btnP.add(bs); btnP.add(bc);
        d.add(p, BorderLayout.CENTER); d.add(btnP, BorderLayout.SOUTH);

        bs.addActionListener(e -> {
            if (tfTen.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d, "Tên không được để trống"); return; }
            int soTin = 3; try { soTin = Integer.parseInt(tfTin.getText().trim()); if (soTin<=0) throw new NumberFormatException(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Số tín chỉ phải là số nguyên dương"); return; }
            try { subjectService.updateSubject(new Subject(tfMa.getText().trim(), tfTen.getText().trim(), 0, soTin)); loadSubjects(); d.dispose(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Lỗi khi cập nhật: " + ex.getMessage()); }
        });
        bc.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }
}
