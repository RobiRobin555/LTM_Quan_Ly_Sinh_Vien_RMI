package service;

import model.Student;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface StudentService extends Remote {
    List<Student> getAllStudents() throws RemoteException;
    Student findStudentById(String msv) throws RemoteException;
    void addStudent(Student student) throws RemoteException;
    void updateStudent(Student student) throws RemoteException;
    void deleteStudent(String msv) throws RemoteException;
}
