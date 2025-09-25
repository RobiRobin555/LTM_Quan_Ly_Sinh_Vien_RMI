package client;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import client.panel.ScorePanel;
import client.panel.StudentPanel;
import client.panel.SubjectPanel;
import service.ScoreService;
import service.StudentService;
import service.SubjectService;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;

public class ClientMain extends JFrame {

    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ScoreService scoreService;

    public ClientMain(StudentService studentService, SubjectService subjectService, ScoreService scoreService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.scoreService = scoreService;

        initUI();
    }

    private void initUI() {
        setTitle("Quản lý - Sinh viên | Môn học | Điểm");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        // Sidebar (màu xanh dương nhạt) — luôn mở rộng
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(52, 152, 219));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Header sidebar
        JLabel appTitle = new JLabel("  Quản lý Hệ thống");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(appTitle.getFont().deriveFont(Font.BOLD, 16f));
        appTitle.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sidebar.add(appTitle);

        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        // Buttons
        JButton btnStudents = createSidebarButton("Quản lý Sinh viên", "com/formdev/flatlaf/extras/icons/material/school.svg");
        JButton btnSubjects = createSidebarButton("Quản lý Môn học", "com/formdev/flatlaf/extras/icons/material/menu_book.svg");
        JButton btnScores = createSidebarButton("Quản lý Điểm", "com/formdev/flatlaf/extras/icons/material/assessment.svg");

        btnStudents.setToolTipText("Quản lý Sinh viên");
        btnSubjects.setToolTipText("Quản lý Môn học");
        btnScores.setToolTipText("Quản lý Điểm");

        sidebar.add(btnStudents);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnSubjects);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnScores);

        // Center area with tabbed panels (we'll still use JTabbedPane but show panels via sidebar)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(null);

        // Create panels (these classes handle their own UI and search/refresh)
        StudentPanel studentPanel = new StudentPanel(studentService, subjectService, scoreService);
        SubjectPanel subjectPanel = new SubjectPanel(subjectService);
        ScorePanel scorePanel = new ScorePanel(scoreService);

        tabbedPane.addTab("", studentPanel);
        tabbedPane.addTab("", subjectPanel);
        tabbedPane.addTab("", scorePanel);

        // Map sidebar buttons to tabs
        btnStudents.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        btnSubjects.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        btnScores.addActionListener(e -> tabbedPane.setSelectedIndex(2));

        // Layout main frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    private JButton createSidebarButton(String text, String svgPath) {
        JButton btn = new JButton("  " + text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 152, 219));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        try {
            // dùng FlatSVGIcon với đường dẫn đến tài nguyên của FlatLaf extras
            FlatSearchIcon icon = new FlatSearchIcon();
            btn.setIcon(icon);
        } catch (Exception ex) {
            // nếu không tìm thấy icon thì giữ text
        }
        return btn;
    }

    // =========================
    // Main
    // =========================
    public static void main(String[] args) {
        try {
            // FlatLaf theme
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // lookup remote services
                StudentService studentService = (StudentService) Naming.lookup("rmi://localhost:1099/StudentService");
                SubjectService subjectService = (SubjectService) Naming.lookup("rmi://localhost:1099/SubjectService");
                ScoreService scoreService = (ScoreService) Naming.lookup("rmi://localhost:1099/ScoreService");

                ClientMain main = new ClientMain(studentService, subjectService, scoreService);
                main.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Không thể kết nối RMI: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
