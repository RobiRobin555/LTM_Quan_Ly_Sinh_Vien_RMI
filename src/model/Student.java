package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Student implements Serializable {
    private String msv;        // mã sinh viên
    private String ten;        // tên sinh viên
    private LocalDate ngaySinh; // ngày sinh
    private String queQuan;    // quê quán
    private double diemTB;     // điểm trung bình (tính từ bảng Score)


    public Student() {
    }


    // ✅ Constructor đầy đủ (không có điểm trung bình)
    public Student(String msv, String ten, LocalDate ngaySinh, String queQuan) {
        this.msv = msv;
        this.ten = ten;
        this.ngaySinh = ngaySinh;
        this.queQuan = queQuan;
        this.diemTB = 0.0;
    }

    // ✅ Constructor đầy đủ có điểm trung bình
    public Student(String msv, String ten, LocalDate ngaySinh, String queQuan, double diemTB) {
        this.msv = msv;
        this.ten = ten;
        this.ngaySinh = ngaySinh;
        this.queQuan = queQuan;
        this.diemTB = diemTB;
    }

    // Getter & Setter
    public String getMsv() {
        return msv;
    }

    public void setMsv(String msv) {
        this.msv = msv;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }

    public double getDiemTB() {
        return diemTB;
    }

    public void setDiemTB(double diemTB) {
        this.diemTB = diemTB;
    }

    @Override
    public String toString() {
        return "Student{" +
                "msv='" + msv + '\'' +
                ", ten='" + ten + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", queQuan='" + queQuan + '\'' +
                ", diemTB=" + diemTB +
                '}';
    }
}
