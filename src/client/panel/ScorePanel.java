package client.panel;

import com.formdev.flatlaf.icons.FlatRevealIcon;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import model.Score;
import service.ScoreService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class ScorePanel extends JPanel {

    private final ScoreService scoreService;
    private JTable scoreTable;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public ScorePanel(ScoreService scoreService) {
        this.scoreService = scoreService;
        initUI();
        loadScores();
    }

    private void initUI() {
        setLayout(new BorderLayout(12,12));
        setBorder(new EmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(227, 242, 253));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,225,245)),
                BorderFactory.createEmptyBorder(10,12,10,12)
        ));
        JLabel title = new JLabel("📊 Quản lý Điểm");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(33,150,243));
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JTextField tfSearch = new JTextField(24);
        tfSearch.setToolTipText("Tìm kiếm (tìm trên tất cả cột)");
        JButton btnRefresh = new JButton(); btnRefresh.setPreferredSize(new Dimension(36,28));
        try { btnRefresh.setIcon(new FlatRevealIcon()); } catch (Exception ignored) {}
        right.add(tfSearch); right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID","MSV","Mã Môn","Điểm"},0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        scoreTable = new JTable(model);
        scoreTable.setRowHeight(28);
        scoreTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scroll = new JScrollPane(scoreTable);
        add(scroll, BorderLayout.CENTER);

        sorter = new TableRowSorter<>(model);
        scoreTable.setRowSorter(sorter);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        footer.setBackground(Color.WHITE);
        JButton btnAdd = styledButton("➕ Thêm", new Color(39,174,96));
        JButton btnEdit = styledButton("✏️ Sửa", new Color(41,128,185));
        JButton btnDelete = styledButton("🗑 Xóa", new Color(231,76,60));
        JButton btnRefreshBottom = styledButton("Làm mới", new Color(96,125,139));
        footer.add(btnAdd); footer.add(btnEdit); footer.add(btnDelete); footer.add(btnRefreshBottom);
        add(footer, BorderLayout.SOUTH);

        // search realtime
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

        btnRefresh.addActionListener(e -> loadScores());
        btnRefreshBottom.addActionListener(e -> loadScores());

        btnAdd.addActionListener(e -> addScoreDialog());
        btnEdit.addActionListener(e -> {
            int row = scoreTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn điểm để sửa"); return; }
            int modelRow = scoreTable.convertRowIndexToModel(row);
            int id = Integer.parseInt(model.getValueAt(modelRow,0).toString());
            String msv = model.getValueAt(modelRow,1).toString();
            String maMon = model.getValueAt(modelRow,2).toString();
            double diem = Double.parseDouble(model.getValueAt(modelRow,3).toString());
            editScoreDialog(id, msv, maMon, diem);
        });
        btnDelete.addActionListener(e -> deleteScoreAction());
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

    public void loadScores() {
        try {
            List<Score> list = scoreService.getAllScores();
            model.setRowCount(0);
            for (Score s : list) {
                model.addRow(new Object[]{s.getId(), s.getMsv(), s.getMaMon(), s.getDiem()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy điểm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addScoreDialog() {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Thêm điểm", Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(420,260); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(new EmptyBorder(12,12,12,12));
        JTextField tfMsv = new JTextField(); JTextField tfMa = new JTextField(); JTextField tfDiem = new JTextField();
        p.add(new JLabel("MSV:")); p.add(tfMsv);
        p.add(new JLabel("Mã môn:")); p.add(tfMa);
        p.add(new JLabel("Điểm:")); p.add(tfDiem);
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bs = styledButton("Lưu", new Color(39,174,96)); JButton bc = styledButton("Hủy", new Color(149,165,166));
        btnP.add(bs); btnP.add(bc);
        d.add(p, BorderLayout.CENTER); d.add(btnP, BorderLayout.SOUTH);

        bs.addActionListener(e -> {
            if (tfMsv.getText().trim().isEmpty() || tfMa.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d, "MSV và Mã môn không được trống"); return; }
            double diem;
            try { diem = Double.parseDouble(tfDiem.getText().trim()); if (diem < 0 || diem > 10) throw new NumberFormatException(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Điểm phải là số trong khoảng 0 - 10"); return; }
            try { scoreService.addScore(new Score(tfMsv.getText().trim(), tfMa.getText().trim(), diem)); loadScores(); d.dispose(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Lỗi khi thêm: " + ex.getMessage()); }
        });
        bc.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    private void editScoreDialog(int id, String msv, String maMon, double diem) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Sửa điểm", Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(420,260); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(new EmptyBorder(12,12,12,12));
        JTextField tfMsv = new JTextField(msv); JTextField tfMa = new JTextField(maMon); JTextField tfDiem = new JTextField(String.valueOf(diem));
        p.add(new JLabel("MSV:")); p.add(tfMsv);
        p.add(new JLabel("Mã môn:")); p.add(tfMa);
        p.add(new JLabel("Điểm:")); p.add(tfDiem);
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bs = styledButton("Lưu", new Color(41,128,185)); JButton bc = styledButton("Hủy", new Color(149,165,166));
        btnP.add(bs); btnP.add(bc);
        d.add(p, BorderLayout.CENTER); d.add(btnP, BorderLayout.SOUTH);

        bs.addActionListener(e -> {
            if (tfMsv.getText().trim().isEmpty() || tfMa.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d, "MSV và Mã môn không được trống"); return; }
            double nd;
            try { nd = Double.parseDouble(tfDiem.getText().trim()); if (nd < 0 || nd > 10) throw new NumberFormatException(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Điểm phải là số trong khoảng 0 - 10"); return; }
            try { scoreService.updateScore(new Score(id, tfMsv.getText().trim(), tfMa.getText().trim(), nd)); loadScores(); d.dispose(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Lỗi khi cập nhật: " + ex.getMessage()); }
        });
        bc.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    private void deleteScoreAction() {
        int row = scoreTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn điểm để xóa"); return; }
        int modelRow = scoreTable.convertRowIndexToModel(row);
        int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        int c = JOptionPane.showConfirmDialog(this, "Xóa điểm ID " + id + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try { scoreService.deleteScore(id); loadScores(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage()); }
        }
    }
}
