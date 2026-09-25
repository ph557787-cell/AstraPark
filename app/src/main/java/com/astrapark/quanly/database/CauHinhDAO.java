package com.astrapark.quanly.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * DAO cho cấu hình hệ thống
 *
 * CẤU HÌNH GIÁ:
 * - 50,000 VNĐ / 1 giờ
 * - 15 phút đầu MIỄN PHÍ
 */
public class CauHinhDAO {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    // ✅ CẤU HÌNH GIÁ MẶC ĐỊNH
    private static final long GIA_OTO_GIO = 50000;        // 50,000 VNĐ/giờ
    private static final int THOI_GIAN_MIEN_PHI = 15;     // 15 phút miễn phí

    public CauHinhDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void mo() {
        db = dbHelper.getWritableDatabase();
    }

    public void dong() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    /**
     * ✅ TÍNH TIỀN ĐỖ XE
     *
     * CÔNG THỨC:
     * - 0-15 phút: MIỄN PHÍ
     * - > 15 phút: 50,000 VNĐ/giờ (tính theo phút)
     *
     * @param phut Số phút đỗ
     * @return Tiền phải trả (VNĐ)
     */
    public long tinhTienDoXe(int phut) {
        // Nếu <= 15 phút → MIỄN PHÍ
        if (phut <= THOI_GIAN_MIEN_PHI) {
            return 0;
        }

        // Trừ đi 15 phút miễn phí
        int phutTinhTien = phut - THOI_GIAN_MIEN_PHI;

        // Tính tiền: (phút - 15) * (50,000 / 60)
        // = phút * 833.33 (làm tròn lên)
        long tien = (phutTinhTien * GIA_OTO_GIO) / 60;

        // Làm tròn lên nghìn
        return lamTronNghin(tien);
    }

    /**
     * Làm tròn lên nghìn
     * VD: 15,600 → 16,000
     */
    private long lamTronNghin(long tien) {
        if (tien % 1000 == 0) {
            return tien;
        }
        return ((tien / 1000) + 1) * 1000;
    }

    /**
     * Lấy giá ô tô theo giờ
     */
    public long layGiaOtoGio() {
        return GIA_OTO_GIO;
    }

    /**
     * Lấy thời gian miễn phí
     */
    public int layThoiGianMienPhi() {
        return THOI_GIAN_MIEN_PHI;
    }

    /**
     * ✅ CẬP NHẬT GIÁ (nếu cần thay đổi sau)
     */
    public void capNhatGia(long giaGio, int phutMienPhi) {
        mo();

        ContentValues values = new ContentValues();
        values.put("gia_oto_gio", giaGio);
        values.put("thoi_gian_mien_phi", phutMienPhi);

        // Cập nhật hoặc insert
        db.execSQL("DELETE FROM cau_hinh");
        db.insert("cau_hinh", null, values);

        dong();
    }

    /**
     * ✅ HIỂN THI CHI TIẾT TÍNH TIỀN (cho debug)
     */
    public String chiTietTinhTien(int phut) {
        if (phut <= THOI_GIAN_MIEN_PHI) {
            return String.format(
                    "Thời gian đỗ: %d phút\n" +
                            "Miễn phí: %d phút\n" +
                            "Tổng tiền: 0 VNĐ",
                    phut, THOI_GIAN_MIEN_PHI
            );
        }

        int phutTinhTien = phut - THOI_GIAN_MIEN_PHI;
        long tienChuaLamTron = (phutTinhTien * GIA_OTO_GIO) / 60;
        long tienDaLamTron = lamTronNghin(tienChuaLamTron);

        return String.format(
                "Thời gian đỗ: %d phút\n" +
                        "Miễn phí: %d phút\n" +
                        "Tính tiền: %d phút\n" +
                        "Giá: %,d VNĐ/giờ\n" +
                        "Tiền tạm: %,d VNĐ\n" +
                        "Làm tròn: %,d VNĐ",
                phut,
                THOI_GIAN_MIEN_PHI,
                phutTinhTien,
                GIA_OTO_GIO,
                tienChuaLamTron,
                tienDaLamTron
        );
    }
}