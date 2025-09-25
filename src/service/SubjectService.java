package service;

import model.Subject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SubjectService extends Remote {
    List<Subject> getAllSubjects() throws RemoteException;

    Subject findSubjectById(String maMon) throws RemoteException;

    void addSubject(Subject subject) throws RemoteException;

    void updateSubject(Subject subject) throws RemoteException;

    void deleteSubject(String maMon) throws RemoteException;
}
