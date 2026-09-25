package com.astrapark.quanly.model;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model cho lịch sử xe vào/ra
 * VERSION 2.0 - Hỗ trợ tính thời gian đỗ realtime
 */
public class LichSuXe {

    private int id;
    private int choDo;
    private String bienSoXe;
    private String loaiXe;
    private long thoiGianVao;
    private long thoiGianRa; // 0 nếu chưa ra
    private int thoiGianDoPhut;
    private long tienPhi;
    private String phuongThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN, THE
    private String ghiChu;
    private String trangThai; // DANG_DO, DA_RA

    /**
     * Constructor đầy đủ 11 tham số (dùng khi đọc từ database)
     */
    public LichSuXe(int id, int choDo, String bienSoXe, String loaiXe,
                    long thoiGianVao, long thoiGianRa, int thoiGianDoPhut,
                    long tienPhi, String phuongThucThanhToan, String ghiChu, String trangThai) {
        this.id = id;
        this.choDo = choDo;
        this.bienSoXe = bienSoXe;
        this.loaiXe = loaiXe;
        this.thoiGianVao = thoiGianVao;
        this.thoiGianRa = thoiGianRa;
        this.thoiGianDoPhut = thoiGianDoPhut;
        this.tienPhi = tienPhi;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    /**
     * ✅ Constructor 6 tham số (dùng khi thêm xe vào từ Firebase/Manual)
     * Các giá trị khác sẽ được set mặc định
     */
    public LichSuXe(int id, int choDo, String bienSoXe, String loaiXe,
                    String ghiChu, String trangThai) {
        this.id = id;
        this.choDo = choDo;
        this.bienSoXe = bienSoXe;
        this.loaiXe = loaiXe;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;

        // Set giá trị mặc định
        this.thoiGianVao = System.currentTimeMillis();
        this.thoiGianRa = 0;
        this.thoiGianDoPhut = 0;
        this.tienPhi = 0;
        this.phuongThucThanhToan = "";
    }

    /**
     * Constructor tạo mới khi xe vào (4 tham số)
     */
    public LichSuXe(int choDo, String bienSoXe, String loaiXe, String ghiChu) {
        this.choDo = choDo;
        this.bienSoXe = bienSoXe;
        this.loaiXe = loaiXe;
        this.thoiGianVao = System.currentTimeMillis();
        this.thoiGianRa = 0;
        this.thoiGianDoPhut = 0;
        this.tienPhi = 0;
        this.phuongThucThanhToan = "";
        this.ghiChu = ghiChu;
        this.trangThai = "DANG_DO";
    }

    // ========== GETTERS GỐC ==========

    public int getId() {
        return id;
    }

    public int getChoDo() {
        return choDo;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public String getLoaiXe() {
        return loaiXe;
    }

    public long getThoiGianVao() {
        return thoiGianVao;
    }

    public long getThoiGianRa() {
        return thoiGianRa;
    }

    public int getThoiGianDoPhut() {
        return thoiGianDoPhut;
    }

    public long getTienPhi() {
        return tienPhi;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    // ========== THÊM METHOD ALIAS ĐỂ TƯƠNG THÍCH ==========

    /**
     * Alias cho getThoiGianDoPhut() - để tương thích với TimKiemActivity
     */
    public int getThoiGianDo() {
        return thoiGianDoPhut;
    }

    /**
     * Alias cho getTienPhi() - để tương thích với TimKiemActivity
     */
    public long getTienThanhToan() {
        return tienPhi;
    }

    // ========================================================

    // ========== SETTERS ==========

    public void setId(int id) {
        this.id = id;
    }

    public void setChoDo(int choDo) {
        this.choDo = choDo;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public void setLoaiXe(String loaiXe) {
        this.loaiXe = loaiXe;
    }

    public void setThoiGianVao(long thoiGianVao) {
        this.thoiGianVao = thoiGianVao;
    }

    public void setThoiGianRa(long thoiGianRa) {
        this.thoiGianRa = thoiGianRa;
    }

    public void setThoiGianDoPhut(int thoiGianDoPhut) {
        this.thoiGianDoPhut = thoiGianDoPhut;
    }

    public void setTienPhi(long tienPhi) {
        this.tienPhi = tienPhi;
    }

    // ========== THÊM SETTER ALIAS ==========

    /**
     * Alias setter cho tienPhi
     */
    public void setTienThanhToan(long tien) {
        this.tienPhi = tien;
    }

    /**
     * Alias setter cho thoiGianDoPhut
     */
    public void setThoiGianDo(int phut) {
        this.thoiGianDoPhut = phut;
    }

    // ========================================

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    // ========== HELPER METHODS ==========

    /**
     * Lấy thời gian vào dạng String
     */
    public String getThoiGianVaoString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(thoiGianVao));
    }

    /**
     * Lấy thời gian ra dạng String
     */
    public String getThoiGianRaString() {
        if (thoiGianRa == 0) {
            return "Chưa ra";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(thoiGianRa));
    }

    /**
     * Lấy thời gian đỗ dạng String (từ database)
     */
    public String getThoiGianDoString() {
        if (thoiGianDoPhut == 0) {
            return "0 phút";
        }

        int gio = thoiGianDoPhut / 60;
        int phut = thoiGianDoPhut % 60;

        if (gio > 0) {
            return gio + "h " + phut + "p";
        } else {
            return phut + " phút";
        }
    }

    /**
     * ✅ LẤY THỜI GIAN ĐỖ REALTIME (PHÚT)
     * - Nếu xe ĐANG ĐỖ: tính từ lúc vào đến hiện tại
     * - Nếu xe ĐÃ RA: lấy từ database
     */
    public int getThoiGianDoPhutRealtime() {
        if ("DANG_DO".equalsIgnoreCase(trangThai)) {
            // Xe đang đỗ -> tính realtime
            if (thoiGianVao > 0) {
                long diff = System.currentTimeMillis() - thoiGianVao;
                return (int) (diff / 60000);
            }
            return 0;
        } else {
            // Xe đã ra -> lấy từ database
            return thoiGianDoPhut;
        }
    }

    /**
     * ✅ LẤY THỜI GIAN ĐỖ REALTIME DẠNG STRING
     */
    public String getThoiGianDoStringRealtime() {
        int phut = getThoiGianDoPhutRealtime();

        if (phut == 0) {
            return "0 phút";
        }

        int gio = phut / 60;
        int phutConLai = phut % 60;

        if (gio > 0) {
            return gio + "h " + phutConLai + "p";
        } else {
            return phut + " phút";
        }
    }

    /**
     * ✅ ALIAS: getThoiGianDoRealtime() → getThoiGianDoPhutRealtime()
     * Để tương thích với code khác
     */
    public int getThoiGianDoRealtime() {
        return getThoiGianDoPhutRealtime();
    }

    /**
     * Lấy tiền dạng String
     */
    public String getTienString() {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(tienPhi) + " VNĐ";
    }

    /**
     * Lấy tiền thanh toán dạng String (alias cho getTienString)
     */
    public String getTienThanhToanString() {
        if (tienPhi == 0) {
            return "0 VNĐ";
        }
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(tienPhi) + " VNĐ";
    }

    /**
     * Lấy tên phương thức thanh toán dạng đọc được
     */
    public String getPhuongThucThanhToanString() {
        if (phuongThucThanhToan == null || phuongThucThanhToan.isEmpty()) {
            return "Chưa thanh toán";
        }
        switch (phuongThucThanhToan) {
            case "TIEN_MAT":
                return "Tiền mặt";
            case "CHUYEN_KHOAN":
                return "Chuyển khoản";
            case "THE":
                return "Thẻ";
            default:
                return "Không xác định";
        }
    }

    /**
     * Kiểm tra xe đang đỗ
     */
    public boolean isDangDo() {
        return "DANG_DO".equals(trangThai);
    }

    /**
     * Kiểm tra xe đã ra
     */
    public boolean isDaRa() {
        return "DA_RA".equals(trangThai);
    }

    @Override
    public String toString() {
        return "LichSuXe{" +
                "id=" + id +
                ", choDo=" + choDo +
                ", bienSoXe='" + bienSoXe + '\'' +
                ", loaiXe='" + loaiXe + '\'' +
                ", thoiGianVao=" + thoiGianVao +
                ", thoiGianRa=" + thoiGianRa +
                ", thoiGianDoPhut=" + thoiGianDoPhut +
                ", tienPhi=" + tienPhi +
                ", phuongThucThanhToan='" + phuongThucThanhToan + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}