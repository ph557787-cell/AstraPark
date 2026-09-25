package com.astrapark.quanly.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.astrapark.quanly.model.ChoDo;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng ChoDo
 * VERSION 2.0 - ĐÃ THÊM FIREBASE SYNC METHODS
 */
public class ChoDoDAO {

    private static final String TAG = "ChoDoDAO";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public ChoDoDAO(Context context) {
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
     * Lấy tất cả chỗ đỗ
     */
    public List<ChoDo> layTatCa() {
        mo();
        List<ChoDo> danhSach = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseHelper.getBangChoDo(),
                null, null, null, null, null,
                DatabaseHelper.getCdId() + " ASC"
        );

        while (cursor.moveToNext()) {
            danhSach.add(taoChoDoTuCursor(cursor));
        }

        cursor.close();
        dong();

        return danhSach;
    }

    /**
     * Lấy chỗ đỗ theo ID
     */
    public ChoDo layTheoId(int id) {
        mo();
        ChoDo choDo = null;

        Cursor cursor = db.query(
                DatabaseHelper.getBangChoDo(),
                null,
                DatabaseHelper.getCdId() + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            choDo = taoChoDoTuCursor(cursor);
        }

        cursor.close();
        dong();

        return choDo;
    }

    /**
     * Cập nhật trạng thái chỗ đỗ
     */
    public int capNhatTrangThai(int id, String trangThai, String bienSo, long thoiGianVao) {
        mo();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.getCdTrangThai(), trangThai);
        values.put(DatabaseHelper.getCdBienSo(), bienSo);
        values.put(DatabaseHelper.getCdThoiGianVao(), thoiGianVao);

        int ketQua = db.update(
                DatabaseHelper.getBangChoDo(),
                values,
                DatabaseHelper.getCdId() + " = ?",
                new String[]{String.valueOf(id)}
        );

        dong();

        Log.d(TAG, String.format("📝 Updated chỗ %d: %s - %s", id, trangThai, bienSo));

        return ketQua;
    }

    /**
     * Đặt chỗ đỗ trống
     */
    public int datTrong(int id) {
        return capNhatTrangThai(id, "TRONG", "", 0);
    }

    /**
     * Đặt chỗ đỗ đang đỗ
     */
    public int datDangDo(int id, String bienSo) {
        return capNhatTrangThai(id, "DANG_DO", bienSo, System.currentTimeMillis());
    }

    /**
     * Đặt chỗ đỗ đã đặt
     */
    public int datDaDat(int id) {
        return capNhatTrangThai(id, "DA_DAT", "", 0);
    }

    /**
     * Đặt chỗ đỗ bảo trì
     */
    public int datBaoTri(int id) {
        return capNhatTrangThai(id, "BAO_TRI", "", 0);
    }

    /**
     * Đếm số chỗ trống
     */
    public int demChoTrong() {
        mo();

        Cursor cursor = db.query(
                DatabaseHelper.getBangChoDo(),
                new String[]{DatabaseHelper.getCdId()},
                DatabaseHelper.getCdTrangThai() + " = ?",
                new String[]{"TRONG"},
                null, null, null
        );

        int count = cursor.getCount();
        cursor.close();
        dong();

        return count;
    }

    /**
     * Đếm số xe đang đỗ
     */
    public int demXeDangDo() {
        mo();

        Cursor cursor = db.query(
                DatabaseHelper.getBangChoDo(),
                new String[]{DatabaseHelper.getCdId()},
                DatabaseHelper.getCdTrangThai() + " = ?",
                new String[]{"DANG_DO"},
                null, null, null
        );

        int count = cursor.getCount();
        cursor.close();
        dong();

        return count;
    }

    /**
     * Lấy danh sách chỗ trống
     */
    public List<ChoDo> layDanhSachChoTrong() {
        mo();
        List<ChoDo> danhSach = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseHelper.getBangChoDo(),
                null,
                DatabaseHelper.getCdTrangThai() + " = ?",
                new String[]{"TRONG"},
                null, null,
                DatabaseHelper.getCdId() + " ASC"
        );

        while (cursor.moveToNext()) {
            danhSach.add(taoChoDoTuCursor(cursor));
        }

        cursor.close();
        dong();

        return danhSach;
    }

    /**
     * Kiểm tra chỗ đỗ có trống không
     */
    public boolean kiemTraTrong(int id) {
        ChoDo choDo = layTheoId(id);
        return choDo != null && choDo.getTrangThai().equals("TRONG");
    }

    /**
     * ===== METHOD MỚI: SYNC VỚI FIREBASE =====
     * Cập nhật từ Firebase (khi nhận realtime update)
     */
    public int syncFromFirebase(int id, String trangThai, String bienSo) {
        mo();

        // Validate trạng thái từ Firebase
        String trangThaiUpper = trangThai.toUpperCase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.getCdTrangThai(), trangThaiUpper);
        values.put(DatabaseHelper.getCdBienSo(), bienSo != null ? bienSo : "");

        // ✅ XỬ LÝ THỜI GIAN VÀO
        if ("DANG_DO".equals(trangThaiUpper)) {
            // Kiểm tra xe cũ
            ChoDo choCu = layTheoId(id);

            // Nếu chỗ này chưa có xe → xe mới vào → lưu timestamp
            if (choCu == null || !"DANG_DO".equalsIgnoreCase(choCu.getTrangThai())) {
                long now = System.currentTimeMillis();
                values.put(DatabaseHelper.getCdThoiGianVao(), now);
                Log.d(TAG, "🚗 Xe mới vào chỗ " + id + " lúc " + now);
            }
            // Nếu đã có xe → KHÔNG CẬP NHẬT thời gian (giữ nguyên)
        } else {
            // Xe ra → reset thời gian
            values.put(DatabaseHelper.getCdThoiGianVao(), 0);
            Log.d(TAG, "🚙 Xe rời chỗ " + id);
        }

        int result = db.update(
                DatabaseHelper.getBangChoDo(),
                values,
                DatabaseHelper.getCdId() + " = ?",
                new String[]{String.valueOf(id)}
        );

        dong();

        Log.d(TAG, String.format("📝 Synced chỗ %d: %s - %s", id, trangThai, bienSo));

        return result;
    }

    /**
     * ===== METHOD MỚI: BATCH UPDATE =====
     * Cập nhật nhiều chỗ đỗ cùng lúc (từ Firebase initial sync)
     */
    public void batchUpdateFromFirebase(List<ChoDo> danhSachChoDo) {
        mo();
        db.beginTransaction();

        try {
            for (ChoDo choDo : danhSachChoDo) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.getCdTrangThai(), choDo.getTrangThai());
                values.put(DatabaseHelper.getCdBienSo(), choDo.getBienSoXe());
                values.put(DatabaseHelper.getCdThoiGianVao(), choDo.getThoiGianVao());

                db.update(
                        DatabaseHelper.getBangChoDo(),
                        values,
                        DatabaseHelper.getCdId() + " = ?",
                        new String[]{String.valueOf(choDo.getId())}
                );
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "✅ Batch updated " + danhSachChoDo.size() + " chỗ đỗ from Firebase");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error batch update: " + e.getMessage());
        } finally {
            db.endTransaction();
            dong();
        }
    }

    /**
     * ===== METHOD MỚI: CONFLICT RESOLUTION =====
     * So sánh local vs Firebase, chọn version mới hơn
     */
    public boolean needsUpdate(int id, long firebaseTimestamp) {
        ChoDo localChoDo = layTheoId(id);
        if (localChoDo == null) return true;

        // So sánh timestamp
        return firebaseTimestamp > localChoDo.getThoiGianVao();
    }

    /**
     * ===== METHOD MỚI: GET STATUS SUMMARY =====
     * Lấy tổng quan trạng thái (để sync lên Firebase)
     */
    public StatusSummary getStatusSummary() {
        int totalSlots = 6;
        int dangDo = demXeDangDo();
        int trong = demChoTrong();

        return new StatusSummary(totalSlots, dangDo, trong);
    }

    /**
     * Inner class: Status Summary
     */
    public static class StatusSummary {
        public int totalSlots;
        public int totalCars;
        public int availableSlots;

        public StatusSummary(int totalSlots, int totalCars, int availableSlots) {
            this.totalSlots = totalSlots;
            this.totalCars = totalCars;
            this.availableSlots = availableSlots;
        }
    }

    /**
     * Tạo đối tượng ChoDo từ Cursor
     */
    private ChoDo taoChoDoTuCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.getCdId()));
        String trangThai = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getCdTrangThai()));
        String bienSo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getCdBienSo()));
        long thoiGianVao = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getCdThoiGianVao()));
        String loaiXe = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getCdLoaiXe()));

        return new ChoDo(id, trangThai, bienSo, thoiGianVao, loaiXe);
    }
}