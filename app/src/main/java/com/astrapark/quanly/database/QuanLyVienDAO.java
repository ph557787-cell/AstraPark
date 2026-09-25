package com.astrapark.quanly.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.astrapark.quanly.model.QuanLyVien;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng QuanLyVien
 */
public class QuanLyVienDAO {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    /**
     * Constructor
     */
    public QuanLyVienDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Mở kết nối database để ghi
     */
    public void mo() {
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Đóng kết nối database
     */
    public void dong() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    /**
     * Kiểm tra đăng nhập
     * @return QuanLyVien nếu đúng, null nếu sai
     */
    public QuanLyVien kiemTraDangNhap(String taiKhoan, String matKhau) {
        mo();
        QuanLyVien quanLy = null;

        String query = "SELECT * FROM " + DatabaseHelper.getBangQuanLyVien() +
                " WHERE " + DatabaseHelper.getQlTaiKhoan() + " = ? AND " +
                DatabaseHelper.getQlMatKhau() + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{taiKhoan, matKhau});

        if (cursor.moveToFirst()) {
            quanLy = taoQuanLyTuCursor(cursor);
        }

        cursor.close();
        dong();

        return quanLy;
    }

    /**
     * Thêm quản lý viên mới
     * @return ID của bản ghi mới, -1 nếu lỗi
     */
    public long themQuanLy(QuanLyVien quanLy) {
        mo();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.getQlTaiKhoan(), quanLy.getTaiKhoan());
        values.put(DatabaseHelper.getQlMatKhau(), quanLy.getMatKhau());
        values.put(DatabaseHelper.getQlHoTen(), quanLy.getHoTen());
        values.put(DatabaseHelper.getQlSoDienThoai(), quanLy.getSoDienThoai());
        values.put(DatabaseHelper.getQlEmail(), quanLy.getEmail());
        values.put(DatabaseHelper.getQlNgayTao(), quanLy.getNgayTao());

        long ketQua = db.insert(DatabaseHelper.getBangQuanLyVien(), null, values);

        dong();
        return ketQua;
    }

    /**
     * Cập nhật thông tin quản lý viên
     */
    public int capNhatQuanLy(QuanLyVien quanLy) {
        mo();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.getQlMatKhau(), quanLy.getMatKhau());
        values.put(DatabaseHelper.getQlHoTen(), quanLy.getHoTen());
        values.put(DatabaseHelper.getQlSoDienThoai(), quanLy.getSoDienThoai());
        values.put(DatabaseHelper.getQlEmail(), quanLy.getEmail());

        int ketQua = db.update(
                DatabaseHelper.getBangQuanLyVien(),
                values,
                DatabaseHelper.getQlId() + " = ?",
                new String[]{String.valueOf(quanLy.getId())}
        );

        dong();
        return ketQua;
    }

    /**
     * Xóa quản lý viên
     */
    public int xoaQuanLy(int id) {
        mo();

        int ketQua = db.delete(
                DatabaseHelper.getBangQuanLyVien(),
                DatabaseHelper.getQlId() + " = ?",
                new String[]{String.valueOf(id)}
        );

        dong();
        return ketQua;
    }

    /**
     * Lấy tất cả quản lý viên
     */
    public List<QuanLyVien> layTatCa() {
        mo();
        List<QuanLyVien> danhSach = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseHelper.getBangQuanLyVien(),
                null, null, null, null, null,
                DatabaseHelper.getQlNgayTao() + " DESC"
        );

        while (cursor.moveToNext()) {
            danhSach.add(taoQuanLyTuCursor(cursor));
        }

        cursor.close();
        dong();

        return danhSach;
    }

    /**
     * Lấy quản lý theo ID
     */
    public QuanLyVien layTheoId(int id) {
        mo();
        QuanLyVien quanLy = null;

        Cursor cursor = db.query(
                DatabaseHelper.getBangQuanLyVien(),
                null,
                DatabaseHelper.getQlId() + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            quanLy = taoQuanLyTuCursor(cursor);
        }

        cursor.close();
        dong();

        return quanLy;
    }

    /**
     * Kiểm tra tài khoản đã tồn tại chưa
     */
    public boolean kiemTraTaiKhoanTonTai(String taiKhoan) {
        mo();

        Cursor cursor = db.query(
                DatabaseHelper.getBangQuanLyVien(),
                new String[]{DatabaseHelper.getQlId()},
                DatabaseHelper.getQlTaiKhoan() + " = ?",
                new String[]{taiKhoan},
                null, null, null
        );

        boolean tonTai = cursor.getCount() > 0;
        cursor.close();
        dong();

        return tonTai;
    }

    /**
     * Đổi mật khẩu
     */
    public boolean doiMatKhau(String taiKhoan, String matKhauCu, String matKhauMoi) {
        // Kiểm tra mật khẩu cũ
        QuanLyVien quanLy = kiemTraDangNhap(taiKhoan, matKhauCu);

        if (quanLy == null) {
            return false; // Mật khẩu cũ sai
        }

        // Cập nhật mật khẩu mới
        mo();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.getQlMatKhau(), matKhauMoi);

        int ketQua = db.update(
                DatabaseHelper.getBangQuanLyVien(),
                values,
                DatabaseHelper.getQlTaiKhoan() + " = ?",
                new String[]{taiKhoan}
        );

        dong();

        return ketQua > 0;
    }

    /**
     * Tạo đối tượng QuanLyVien từ Cursor
     */
    private QuanLyVien taoQuanLyTuCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.getQlId()));
        String taiKhoan = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getQlTaiKhoan()));
        String matKhau = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getQlMatKhau()));
        String hoTen = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getQlHoTen()));
        String soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getQlSoDienThoai()));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getQlEmail()));
        long ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getQlNgayTao()));

        return new QuanLyVien(id, taiKhoan, matKhau, hoTen, soDienThoai, email, ngayTao);
    }
}