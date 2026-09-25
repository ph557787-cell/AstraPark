package com.astrapark.quanly.model;

/**
 * Class đại diện cho 1 chỗ đỗ xe
 */
public class ChoDo {

    private int id; // Số thứ tự chỗ đỗ (1-6)
    private String trangThai; // "TRONG", "DANG_DO", "DA_DAT", "BAO_TRI"
    private String bienSoXe; // Biển số xe đang đỗ (nếu có)
    private long thoiGianVao; // Timestamp thời gian vào (milliseconds)
    private String loaiXe; // "OTO"

    /**
     * Constructor đầy đủ
     */
    public ChoDo(int id, String trangThai, String bienSoXe, long thoiGianVao, String loaiXe) {
        this.id = id;
        this.trangThai = trangThai;
        this.bienSoXe = bienSoXe;
        this.thoiGianVao = thoiGianVao;
        this.loaiXe = loaiXe;
    }

    /**
     * Constructor cho chỗ trống
     */
    public ChoDo(int id, String loaiXe) {
        this.id = id;
        this.trangThai = "TRONG";
        this.bienSoXe = "";
        this.thoiGianVao = 0;
        this.loaiXe = loaiXe;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public long getThoiGianVao() {
        return thoiGianVao;
    }

    public void setThoiGianVao(long thoiGianVao) {
        this.thoiGianVao = thoiGianVao;
    }

    public String getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(String loaiXe) {
        this.loaiXe = loaiXe;
    }

    /**
     * Kiểm tra chỗ đỗ có trống không
     */
    public boolean laTrong() {
        return trangThai.equals("TRONG");
    }

    /**
     * Lấy màu theo trạng thái
     */
    public String getMauTheoTrangThai() {
        switch (trangThai) {
            case "TRONG":
                return "#2D5016"; // Xanh lá
            case "DANG_DO":
                return "#C41E3A"; // Đỏ
            case "DA_DAT":
                return "#FFB700"; // Vàng
            case "BAO_TRI":
                return "#808080"; // Xám
            default:
                return "#1A1A1A";
        }
    }
}