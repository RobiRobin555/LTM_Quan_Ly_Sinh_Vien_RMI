package service;

import model.Score;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ScoreService extends Remote {
    List<Score> getAllScores() throws RemoteException;

    Score findScoreById(int id) throws RemoteException;

    void addScore(Score score) throws RemoteException;

    void updateScore(Score score) throws RemoteException;

    void deleteScore(int id) throws RemoteException;
}
