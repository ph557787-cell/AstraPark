package com.astrapark.quanly.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.astrapark.quanly.model.LichSuXe;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO Lịch sử xe
 * VERSION 3.0 FINAL - SYNC FIREBASE ↔ DATABASE + SEARCH & FILTER
 */
public class LichSuXeDAO {

    private static final String TAG = "LichSuXeDAO";

    private DatabaseHelper dbHelper;

    public LichSuXeDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // ==================== THÊM MỚI ====================

    /**
     * ✅ THÊM XE VÀO BÃI
     * @return id của bản ghi vừa thêm (> 0 nếu thành công)
     */
    public long themXeVao(LichSuXe lichSu) {
        SQLiteDatabase db = null;
        long result = -1;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHO_DO, lichSu.getChoDo());
            values.put(DatabaseHelper.COLUMN_BIEN_SO_LS, lichSu.getBienSoXe());
            values.put(DatabaseHelper.COLUMN_LOAI_XE_LS, lichSu.getLoaiXe());
            values.put(DatabaseHelper.COLUMN_THOI_GIAN_VAO, System.currentTimeMillis());
            values.put(DatabaseHelper.COLUMN_THOI_GIAN_RA, 0);
            values.put(DatabaseHelper.COLUMN_THOI_GIAN_DO, 0);
            values.put(DatabaseHelper.COLUMN_TIEN_PHI, 0);
            values.put(DatabaseHelper.COLUMN_PHUONG_THUC, "");
            values.put(DatabaseHelper.COLUMN_GHI_CHU_LS, lichSu.getGhiChu());
            values.put(DatabaseHelper.COLUMN_TRANG_THAI_LS, "DANG_DO");

            result = db.insert(DatabaseHelper.TABLE_LICH_SU_XE, null, values);

            if (result > 0) {
                Log.d(TAG, String.format("✅ Đã thêm xe vào: ID=%d, Chỗ=%d, Biển=%s",
                        result, lichSu.getChoDo(), lichSu.getBienSoXe()));
            } else {
                Log.e(TAG, "❌ Lỗi thêm xe vào bãi");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error themXeVao: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }

        return result;
    }

    /**
     * ✅ ALIAS: themLichSu() → themXeVao()
     */
    public long themLichSu(LichSuXe lichSu) {
        return themXeVao(lichSu);
    }

    // ==================== CẬP NHẬT ====================

    /**
     * ✅ CẬP NHẬT XE RA
     * @return số dòng bị ảnh hưởng (> 0 nếu thành công)
     */
    public int capNhatXeRa(int id, long thoiGianRa, int thoiGianDo,
                           long tienPhi, String phuongThuc) {
        SQLiteDatabase db = null;
        int result = 0;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_THOI_GIAN_RA, thoiGianRa);
            values.put(DatabaseHelper.COLUMN_THOI_GIAN_DO, thoiGianDo);
            values.put(DatabaseHelper.COLUMN_TIEN_PHI, tienPhi);
            values.put(DatabaseHelper.COLUMN_PHUONG_THUC, phuongThuc);
            values.put(DatabaseHelper.COLUMN_TRANG_THAI_LS, "DA_RA");

            result = db.update(
                    DatabaseHelper.TABLE_LICH_SU_XE,
                    values,
                    DatabaseHelper.COLUMN_ID_LS + " = ?",
                    new String[]{String.valueOf(id)}
            );

            if (result > 0) {
                Log.d(TAG, String.format("✅ Đã cập nhật xe ra: ID=%d, Tiền=%,d, PT=%s",
                        id, tienPhi, phuongThuc));
            } else {
                Log.e(TAG, "❌ Lỗi cập nhật xe ra - ID không tồn tại: " + id);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error capNhatXeRa: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }

        return result;
    }

    /**
     * ✅ ALIAS: capNhatLichSu() → capNhatXeRa()
     */
    public int capNhatLichSu(int id, long thoiGianRa, int thoiGianDo,
                             long tienPhi, String phuongThuc) {
        return capNhatXeRa(id, thoiGianRa, thoiGianDo, tienPhi, phuongThuc);
    }

    // ==================== TRUY VẤN CƠ BẢN ====================

    /**
     * ✅ LẤY DANH SÁCH XE ĐANG ĐỖ
     */
    public List<LichSuXe> layXeDangDo() {
        List<LichSuXe> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " WHERE " + DatabaseHelper.COLUMN_TRANG_THAI_LS + " = 'DANG_DO' " +
                    " ORDER BY " + DatabaseHelper.COLUMN_THOI_GIAN_VAO + " DESC";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToLichSuXe(cursor));
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "✅ Đã lấy danh sách xe đang đỗ: " + list.size() + " xe");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error layXeDangDo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return list;
    }

    /**
     * ✅ LẤY XE ĐANG ĐỖ TẠI CHỖ CỤ THỂ
     */
    public LichSuXe layTheoChoDoDangDo(int choDoId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        LichSuXe lichSu = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " WHERE " + DatabaseHelper.COLUMN_CHO_DO + " = ? " +
                    " AND " + DatabaseHelper.COLUMN_TRANG_THAI_LS + " = 'DANG_DO' " +
                    " LIMIT 1";

            cursor = db.rawQuery(query, new String[]{String.valueOf(choDoId)});

            if (cursor != null && cursor.moveToFirst()) {
                lichSu = cursorToLichSuXe(cursor);

                Log.d(TAG, String.format("✅ Tìm thấy xe đang đỗ chỗ %d: %s",
                        choDoId, lichSu.getBienSoXe()));
            } else {
                Log.d(TAG, "ℹ️ Không tìm thấy xe đang đỗ chỗ " + choDoId);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error layTheoChoDoDangDo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return lichSu;
    }

    /**
     * ✅ LẤY TẤT CẢ LỊCH SỬ (PHÂN TRANG)
     */
    public List<LichSuXe> layTatCa(int limit, int offset) {
        List<LichSuXe> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " ORDER BY " + DatabaseHelper.COLUMN_THOI_GIAN_VAO + " DESC " +
                    " LIMIT ? OFFSET ?";

            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(limit),
                    String.valueOf(offset)
            });

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToLichSuXe(cursor));
                } while (cursor.moveToNext());
            }

            Log.d(TAG, String.format("✅ Đã lấy %d lịch sử (limit=%d, offset=%d)",
                    list.size(), limit, offset));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error layTatCa: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return list;
    }

    /**
     * ✅ LẤY TẤT CẢ LỊCH SỬ (KHÔNG PHÂN TRANG)
     */
    public List<LichSuXe> layTatCa() {
        return layTatCa(1000, 0); // Mặc định lấy 1000 bản ghi đầu
    }

    /**
     * ✅ ALIAS: layTatCaLichSu() → layTatCa()
     */
    public List<LichSuXe> layTatCaLichSu() {
        return layTatCa();
    }

    /**
     * ✅ ALIAS: layTatCaLichSu() với phân trang
     */
    public List<LichSuXe> layTatCaLichSu(int limit, int offset) {
        return layTatCa(limit, offset);
    }

    // ==================== TÌM KIẾM & LỌC ====================

    /**
     * ✅ LẤY LỊCH SỬ THEO KHOẢNG THỜI GIAN
     * @param tuNgay timestamp bắt đầu
     * @param denNgay timestamp kết thúc
     * @return danh sách lịch sử trong khoảng thời gian
     */
    public List<LichSuXe> layTheoKhoangThoiGian(long tuNgay, long denNgay) {
        List<LichSuXe> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " WHERE " + DatabaseHelper.COLUMN_THOI_GIAN_VAO + " >= ? " +
                    " AND " + DatabaseHelper.COLUMN_THOI_GIAN_VAO + " <= ? " +
                    " ORDER BY " + DatabaseHelper.COLUMN_THOI_GIAN_VAO + " DESC";

            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(tuNgay),
                    String.valueOf(denNgay)
            });

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToLichSuXe(cursor));
                } while (cursor.moveToNext());
            }

            Log.d(TAG, String.format("✅ Lấy lịch sử theo khoảng thời gian: %d bản ghi", list.size()));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error layTheoKhoangThoiGian: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return list;
    }

    /**
     * ✅ TÌM KIẾM LỊCH SỬ THEO BIỂN SỐ
     * @param bienSo biển số xe cần tìm
     * @return danh sách lịch sử
     */
    public List<LichSuXe> timKiemTheoBienSo(String bienSo) {
        List<LichSuXe> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " WHERE " + DatabaseHelper.COLUMN_BIEN_SO_LS + " LIKE ? " +
                    " ORDER BY " + DatabaseHelper.COLUMN_THOI_GIAN_VAO + " DESC";

            cursor = db.rawQuery(query, new String[]{"%" + bienSo + "%"});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToLichSuXe(cursor));
                } while (cursor.moveToNext());
            }

            Log.d(TAG, String.format("✅ Tìm thấy %d lịch sử với biển số: %s", list.size(), bienSo));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error timKiemTheoBienSo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return list;
    }

    /**
     * ✅ LẤY LỊCH SỬ THEO TRẠNG THÁI
     * @param trangThai DANG_DO hoặc DA_RA
     * @return danh sách lịch sử
     */
    public List<LichSuXe> layTheoTrangThai(String trangThai) {
        List<LichSuXe> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " WHERE " + DatabaseHelper.COLUMN_TRANG_THAI_LS + " = ? " +
                    " ORDER BY " + DatabaseHelper.COLUMN_THOI_GIAN_VAO + " DESC";

            cursor = db.rawQuery(query, new String[]{trangThai});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToLichSuXe(cursor));
                } while (cursor.moveToNext());
            }

            Log.d(TAG, String.format("✅ Lấy lịch sử theo trạng thái '%s': %d bản ghi", trangThai, list.size()));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error layTheoTrangThai: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return list;
    }

    /**
     * ✅ TÌM KIẾM NÂNG CAO
     * @param bienSo biển số (có thể null)
     * @param tuNgay thời gian bắt đầu (0 nếu không lọc)
     * @param denNgay thời gian kết thúc (0 nếu không lọc)
     * @param trangThai trạng thái (null nếu không lọc)
     * @return danh sách lịch sử
     */
    public List<LichSuXe> timKiemNangCao(String bienSo, long tuNgay, long denNgay, String trangThai) {
        List<LichSuXe> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + DatabaseHelper.TABLE_LICH_SU_XE + " WHERE 1=1");
            List<String> args = new ArrayList<>();

            // Lọc theo biển số
            if (bienSo != null && !bienSo.isEmpty()) {
                queryBuilder.append(" AND ").append(DatabaseHelper.COLUMN_BIEN_SO_LS).append(" LIKE ?");
                args.add("%" + bienSo + "%");
            }

            // Lọc theo khoảng thời gian
            if (tuNgay > 0) {
                queryBuilder.append(" AND ").append(DatabaseHelper.COLUMN_THOI_GIAN_VAO).append(" >= ?");
                args.add(String.valueOf(tuNgay));
            }

            if (denNgay > 0) {
                queryBuilder.append(" AND ").append(DatabaseHelper.COLUMN_THOI_GIAN_VAO).append(" <= ?");
                args.add(String.valueOf(denNgay));
            }

            // Lọc theo trạng thái
            if (trangThai != null && !trangThai.isEmpty()) {
                queryBuilder.append(" AND ").append(DatabaseHelper.COLUMN_TRANG_THAI_LS).append(" = ?");
                args.add(trangThai);
            }

            queryBuilder.append(" ORDER BY ").append(DatabaseHelper.COLUMN_THOI_GIAN_VAO).append(" DESC");

            cursor = db.rawQuery(queryBuilder.toString(), args.toArray(new String[0]));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToLichSuXe(cursor));
                } while (cursor.moveToNext());
            }

            Log.d(TAG, String.format("✅ Tìm kiếm nâng cao: %d kết quả", list.size()));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error timKiemNangCao: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return list;
    }

    // ==================== THỐNG KÊ ====================

    /**
     * ✅ TÍNH DOANH THU HÔM NAY
     */
    public long tinhDoanhThuHomNay() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        long doanhThu = 0;

        try {
            db = dbHelper.getReadableDatabase();

            // Lấy timestamp đầu ngày hôm nay
            long startOfDay = getStartOfDay();

            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_TIEN_PHI + ") " +
                    "FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " WHERE " + DatabaseHelper.COLUMN_THOI_GIAN_RA + " >= ? " +
                    " AND " + DatabaseHelper.COLUMN_TRANG_THAI_LS + " = 'DA_RA'";

            cursor = db.rawQuery(query, new String[]{String.valueOf(startOfDay)});

            if (cursor != null && cursor.moveToFirst()) {
                doanhThu = cursor.getLong(0);
            }

            Log.d(TAG, String.format("💰 Doanh thu hôm nay: %,d VNĐ", doanhThu));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error tinhDoanhThuHomNay: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return doanhThu;
    }

    /**
     * ✅ TÍNH TỔNG DOANH THU THEO KHOẢNG THỜI GIAN
     * @param tuNgay timestamp bắt đầu
     * @param denNgay timestamp kết thúc
     * @return tổng tiền
     */
    public long tinhDoanhThuTheoKhoangThoiGian(long tuNgay, long denNgay) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        long doanhThu = 0;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_TIEN_PHI + ") " +
                    "FROM " + DatabaseHelper.TABLE_LICH_SU_XE +
                    " WHERE " + DatabaseHelper.COLUMN_THOI_GIAN_RA + " >= ? " +
                    " AND " + DatabaseHelper.COLUMN_THOI_GIAN_RA + " <= ? " +
                    " AND " + DatabaseHelper.COLUMN_TRANG_THAI_LS + " = 'DA_RA'";

            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(tuNgay),
                    String.valueOf(denNgay)
            });

            if (cursor != null && cursor.moveToFirst()) {
                doanhThu = cursor.getLong(0);
            }

            Log.d(TAG, String.format("💰 Doanh thu trong khoảng: %,d VNĐ", doanhThu));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error tinhDoanhThuTheoKhoangThoiGian: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return doanhThu;
    }

    /**
     * ✅ ĐẾM TỔNG SỐ LỊCH SỬ
     */
    public int demTongSo() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_LICH_SU_XE;

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error demTongSo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return count;
    }

    // ==================== XÓA ====================

    /**
     * ✅ XÓA LỊCH SỬ THEO ID
     */
    public int xoa(int id) {
        SQLiteDatabase db = null;
        int result = 0;

        try {
            db = dbHelper.getWritableDatabase();

            result = db.delete(
                    DatabaseHelper.TABLE_LICH_SU_XE,
                    DatabaseHelper.COLUMN_ID_LS + " = ?",
                    new String[]{String.valueOf(id)}
            );

            if (result > 0) {
                Log.d(TAG, "✅ Đã xóa lịch sử ID: " + id);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error xoa: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }

        return result;
    }

    /**
     * ✅ ALIAS: xoaLichSu() → xoa()
     */
    public int xoaLichSu(int id) {
        return xoa(id);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Chuyển Cursor thành đối tượng LichSuXe
     */
    private LichSuXe cursorToLichSuXe(Cursor cursor) {
        return new LichSuXe(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_LS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHO_DO)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIEN_SO_LS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOAI_XE_LS)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_THOI_GIAN_VAO)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_THOI_GIAN_RA)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_THOI_GIAN_DO)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIEN_PHI)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHUONG_THUC)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GHI_CHU_LS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANG_THAI_LS))
        );
    }

    /**
     * Lấy timestamp đầu ngày hôm nay (00:00:00)
     */
    private long getStartOfDay() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * ✅ CLOSE DATABASE HELPER
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}