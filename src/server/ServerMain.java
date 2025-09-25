package server;

import service.StudentService;
import service.SubjectService;
import service.ScoreService;
import service.impl.StudentServiceImpl;
import service.impl.SubjectServiceImpl;
import service.impl.ScoreServiceImpl;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            // Khởi động RMI Registry tại cổng 1099
            LocateRegistry.createRegistry(1099);
            System.out.println(">>> RMI Registry đã khởi động tại cổng 1099.");

            // Khởi tạo các service
            StudentService studentService =(StudentService) new StudentServiceImpl();
            SubjectService subjectService = (SubjectService) new SubjectServiceImpl();
            ScoreService scoreService = (ScoreService) new ScoreServiceImpl();

            // Bind các service vào Registry
            Naming.bind("rmi://localhost:1099/StudentService", studentService);
            Naming.bind("rmi://localhost:1099/SubjectService", subjectService);
            Naming.bind("rmi://localhost:1099/ScoreService", scoreService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
