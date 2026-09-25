package com.astrapark.quanly.model;

/**
 * Model cho tài khoản quản lý viên
 */
public class QuanLyVien {

    private int id;
    private String taiKhoan;
    private String matKhau;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private long ngayTao;

    /**
     * Constructor đầy đủ
     */
    public QuanLyVien(int id, String taiKhoan, String matKhau, String hoTen,
                      String soDienThoai, String email, long ngayTao) {
        this.id = id;
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.ngayTao = ngayTao;
    }

    /**
     * Constructor tạo mới (không có ID)
     */
    public QuanLyVien(String taiKhoan, String matKhau, String hoTen,
                      String soDienThoai, String email) {
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.ngayTao = System.currentTimeMillis();
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(long ngayTao) {
        this.ngayTao = ngayTao;
    }
}