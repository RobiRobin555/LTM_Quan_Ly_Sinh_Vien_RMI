package model;

import java.io.Serializable;

public class Score implements Serializable {
    private int id;         // id tự tăng trong bảng Score
    private String msv;     // mã sinh viên
    private String maMon;   // mã môn học
    private double diem;    // điểm môn học

    public Score(int id, String msv, String maMon, double diem) {
        this.id = id;
        this.msv = msv;
        this.maMon = maMon;
        this.diem = diem;
    }

    public Score(String msv, String maMon, double diem) {
        this(0, msv, maMon, diem);
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMsv() {
        return msv;
    }

    public void setMsv(String msv) {
        this.msv = msv;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public double getDiem() {
        return diem;
    }

    public void setDiem(double diem) {
        this.diem = diem;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id=" + id +
                ", msv='" + msv + '\'' +
                ", maMon='" + maMon + '\'' +
                ", diem=" + diem +
                '}';
    }
}
