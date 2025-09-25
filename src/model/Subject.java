package model;

import java.io.Serializable;

public class Subject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maMon;
    private String tenMon;
    private int soLuongDangKy;
    private int soTinChi;  // NEW FIEL
    public Subject(String maMon, String tenMon) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soLuongDangKy = 0;
        this.soTinChi = 0;
    }

    public Subject(String maMon, String tenMon, int soTinChi, int soLuongDangKy) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.soLuongDangKy = soLuongDangKy;
    }


    // Getter & Setter
    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public int getSoLuongDangKy() {
        return soLuongDangKy;
    }

    public void setSoLuongDangKy(int soLuongDangKy) {
        this.soLuongDangKy = soLuongDangKy;
    }

    public int getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(int soTinChi) {
        this.soTinChi = soTinChi;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "maMon='" + maMon + '\'' +
                ", tenMon='" + tenMon + '\'' +
                ", soLuongDangKy=" + soLuongDangKy +
                ", soTinChi=" + soTinChi +
                '}';
    }
}
