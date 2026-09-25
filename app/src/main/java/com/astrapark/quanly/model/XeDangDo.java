package com.astrapark.quanly.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class đại diện cho 1 xe đang đỗ
 */
public class XeDangDo {

    private int id; // ID tự tăng
    private int choDo; // Chỗ đỗ (1-6)
    private String bienSoXe; // Biển số xe
    private long thoiGianVao; // Timestamp thời gian vào (milliseconds)
    private String loaiXe; // "OTO"
    private String ghiChu; // Ghi chú (nếu có)

    /**
     * Constructor đầy đủ
     */
    public XeDangDo(int id, int choDo, String bienSoXe, long thoiGianVao, String loaiXe, String ghiChu) {
        this.id = id;
        this.choDo = choDo;
        this.bienSoXe = bienSoXe;
        this.thoiGianVao = thoiGianVao;
        this.loaiXe = loaiXe;
        this.ghiChu = ghiChu;
    }

    /**
     * Constructor không có ghi chú
     */
    public XeDangDo(int id, int choDo, String bienSoXe, long thoiGianVao, String loaiXe) {
        this(id, choDo, bienSoXe, thoiGianVao, loaiXe, "");
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChoDo() {
        return choDo;
    }

    public void setChoDo(int choDo) {
        this.choDo = choDo;
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

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    /**
     * Lấy thời gian vào dạng String
     */
    public String getThoiGianVaoString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(thoiGianVao));
    }

    /**
     * Tính thời gian đã đỗ (phút)
     */
    public int getThoiGianDoPhut() {
        long hienTai = System.currentTimeMillis();
        long thoiGianDoMs = hienTai - thoiGianVao;
        return (int) ((thoiGianDoMs / 1000) / 60);
    }

    /**
     * Lấy thời gian đỗ dạng String (X giờ Y phút)
     */
    public String getThoiGianDoString() {
        int phut = getThoiGianDoPhut();
        int gio = phut / 60;
        int phutConLai = phut % 60;

        if (gio > 0) {
            return gio + "h " + phutConLai + "p";
        } else {
            return phut + " phút";
        }
    }

    /**
     * Tính tiền đỗ xe (5.000đ/phút)
     */
    public long tinhTien() {
        return getThoiGianDoPhut() * 5000L;
    }

    /**
     * Lấy tiền dạng String đã format
     */
    public String getTienString() {
        long tien = tinhTien();
        if (tien >= 1000000) {
            return String.format(Locale.getDefault(), "%.1fM", tien / 1000000.0);
        } else if (tien >= 1000) {
            return String.format(Locale.getDefault(), "%.0fK", tien / 1000.0);
        } else {
            return tien + "đ";
        }
    }
}