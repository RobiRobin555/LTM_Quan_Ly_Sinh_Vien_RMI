package client;

import com.formdev.flatlaf.FlatLightLaf;
import client.panel.StudentPanel;
import client.panel.SubjectPanel;
import client.panel.ScorePanel;
import service.ScoreService;
import service.StudentService;
import service.SubjectService;

import javax.swing.*;

public class ClientMain extends JFrame {
    public ClientMain(StudentService studentService,
                      SubjectService subjectService,
                      ScoreService scoreService) {
        setTitle("📚 Quản lý Sinh viên & Môn học - RMI");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("👨‍🎓 Sinh viên", new StudentPanel(studentService, subjectService, scoreService));
        tabbedPane.addTab("📘 Môn học", new SubjectPanel(subjectService));
        tabbedPane.addTab("📊 Điểm", new ScorePanel(scoreService, studentService, subjectService));

        add(tabbedPane);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            StudentService studentService = (StudentService) java.rmi.Naming.lookup("rmi://localhost:1099/StudentService");
            SubjectService subjectService = (SubjectService) java.rmi.Naming.lookup("rmi://localhost:1099/SubjectService");
            ScoreService scoreService = (ScoreService) java.rmi.Naming.lookup("rmi://localhost:1099/ScoreService");

            SwingUtilities.invokeLater(() -> 	
                new ClientMain(studentService, subjectService, scoreService).setVisible(true)
            );
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Không thể kết nối RMI: " + e.getMessage());
        }
    }
}
