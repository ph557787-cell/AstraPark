package com.astrapark.quanly.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class quản lý database SQLite
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Thông tin database
    private static final String TEN_DATABASE = "AstraPark.db";
    private static final int PHIEN_BAN = 2;

    // Tên các bảng
    private static final String BANG_QUAN_LY_VIEN = "QuanLyVien";
    private static final String BANG_CHO_DO = "ChoDo";
    private static final String BANG_LICH_SU = "LichSuXe";
    private static final String BANG_THANH_TOAN = "ThanhToan";
    private static final String BANG_CAU_HINH = "CauHinh";

    // Cột bảng QuanLyVien
    private static final String QL_ID = "id";
    private static final String QL_TAI_KHOAN = "taiKhoan";
    private static final String QL_MAT_KHAU = "matKhau";
    private static final String QL_HO_TEN = "hoTen";
    private static final String QL_SO_DIEN_THOAI = "soDienThoai";
    private static final String QL_EMAIL = "email";
    private static final String QL_NGAY_TAO = "ngayTao";

    // Cột bảng ChoDo
    private static final String CD_ID = "id";
    private static final String CD_TRANG_THAI = "trangThai";
    private static final String CD_BIEN_SO = "bienSoXe";
    private static final String CD_THOI_GIAN_VAO = "thoiGianVao";
    private static final String CD_LOAI_XE = "loaiXe";

    // Cột bảng LichSuXe
    private static final String LS_ID = "id";
    private static final String LS_CHO_DO = "choDo";
    private static final String LS_BIEN_SO = "bienSoXe";
    private static final String LS_LOAI_XE = "loaiXe";
    private static final String LS_THOI_GIAN_VAO = "thoiGianVao";
    private static final String LS_THOI_GIAN_RA = "thoiGianRa";
    private static final String LS_THOI_GIAN_DO_PHUT = "thoiGianDoPhut";
    private static final String LS_TIEN_PHI = "tienPhi";
    private static final String LS_PHUONG_THUC = "phuongThucThanhToan";
    private static final String LS_GHI_CHU = "ghiChu";
    private static final String LS_TRANG_THAI = "trangThai";

    // Cột bảng ThanhToan
    private static final String TT_ID = "id";
    private static final String TT_ID_LICH_SU = "idLichSu";
    private static final String TT_SO_TIEN = "soTien";
    private static final String TT_PHUONG_THUC = "phuongThuc";
    private static final String TT_THOI_GIAN = "thoiGian";
    private static final String TT_TRANG_THAI = "trangThai";

    // Cột bảng CauHinh
    private static final String CH_ID = "id";
    private static final String CH_KEY = "configKey";
    private static final String CH_VALUE = "value";

    // ==================== PUBLIC CONSTANTS CHO DAO ====================
    // Tên bảng
    public static final String TABLE_LICH_SU_XE = "LichSuXe";
    public static final String TABLE_CHO_DO = "ChoDo";
    public static final String TABLE_QUAN_LY_VIEN = "QuanLyVien";
    public static final String TABLE_THANH_TOAN = "ThanhToan";
    public static final String TABLE_CAU_HINH = "CauHinh";

    // Cột bảng LichSuXe
    public static final String COLUMN_ID_LS = "id";
    public static final String COLUMN_CHO_DO = "choDo";
    public static final String COLUMN_BIEN_SO_LS = "bienSoXe";
    public static final String COLUMN_LOAI_XE_LS = "loaiXe";
    public static final String COLUMN_THOI_GIAN_VAO = "thoiGianVao";
    public static final String COLUMN_THOI_GIAN_RA = "thoiGianRa";
    public static final String COLUMN_THOI_GIAN_DO = "thoiGianDoPhut";
    public static final String COLUMN_TIEN_PHI = "tienPhi";
    public static final String COLUMN_PHUONG_THUC = "phuongThucThanhToan";
    public static final String COLUMN_GHI_CHU_LS = "ghiChu";
    public static final String COLUMN_TRANG_THAI_LS = "trangThai";

    /**
     * Constructor
     */
    public DatabaseHelper(Context context) {
        super(context, TEN_DATABASE, null, PHIEN_BAN);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng QuanLyVien
        String taoQuanLyVien = "CREATE TABLE " + BANG_QUAN_LY_VIEN + " (" +
                QL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QL_TAI_KHOAN + " TEXT UNIQUE NOT NULL, " +
                QL_MAT_KHAU + " TEXT NOT NULL, " +
                QL_HO_TEN + " TEXT, " +
                QL_SO_DIEN_THOAI + " TEXT, " +
                QL_EMAIL + " TEXT, " +
                QL_NGAY_TAO + " INTEGER)";
        db.execSQL(taoQuanLyVien);

        // Tạo bảng ChoDo
        String taoChoDo = "CREATE TABLE " + BANG_CHO_DO + " (" +
                CD_ID + " INTEGER PRIMARY KEY, " +
                CD_TRANG_THAI + " TEXT NOT NULL, " +
                CD_BIEN_SO + " TEXT, " +
                CD_THOI_GIAN_VAO + " INTEGER, " +
                CD_LOAI_XE + " TEXT)";
        db.execSQL(taoChoDo);

        // Tạo bảng LichSuXe
        String taoLichSu = "CREATE TABLE " + BANG_LICH_SU + " (" +
                LS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LS_CHO_DO + " INTEGER, " +
                LS_BIEN_SO + " TEXT NOT NULL, " +
                LS_LOAI_XE + " TEXT, " +
                LS_THOI_GIAN_VAO + " INTEGER, " +
                LS_THOI_GIAN_RA + " INTEGER, " +
                LS_THOI_GIAN_DO_PHUT + " INTEGER, " +
                LS_TIEN_PHI + " INTEGER, " +
                LS_PHUONG_THUC + " TEXT, " +
                LS_GHI_CHU + " TEXT, " +
                LS_TRANG_THAI + " TEXT)";
        db.execSQL(taoLichSu);

        // Tạo bảng ThanhToan
        String taoThanhToan = "CREATE TABLE " + BANG_THANH_TOAN + " (" +
                TT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TT_ID_LICH_SU + " INTEGER, " +
                TT_SO_TIEN + " INTEGER, " +
                TT_PHUONG_THUC + " TEXT, " +
                TT_THOI_GIAN + " INTEGER, " +
                TT_TRANG_THAI + " TEXT, " +
                "FOREIGN KEY(" + TT_ID_LICH_SU + ") REFERENCES " +
                BANG_LICH_SU + "(" + LS_ID + "))";
        db.execSQL(taoThanhToan);

        // Tạo bảng CauHinh - ĐÃ SỬA
        String taoCauHinh = "CREATE TABLE " + BANG_CAU_HINH + " (" +
                CH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CH_KEY + " TEXT UNIQUE NOT NULL, " +  // Đã đổi thành configKey
                CH_VALUE + " TEXT)";
        db.execSQL(taoCauHinh);

        // Thêm dữ liệu mặc định
        themDuLieuMacDinh(db);

        Log.d("DatabaseHelper", "Database created successfully");
    }

    /**
     * Thêm dữ liệu mặc định khi tạo database
     */
    private void themDuLieuMacDinh(SQLiteDatabase db) {
        // Thêm tài khoản admin mặc định
        ContentValues admin = new ContentValues();
        admin.put(QL_TAI_KHOAN, "admin");
        admin.put(QL_MAT_KHAU, "admin123");
        admin.put(QL_HO_TEN, "Quản trị viên");
        admin.put(QL_SO_DIEN_THOAI, "0123456789");
        admin.put(QL_EMAIL, "admin@astrapark.com");
        admin.put(QL_NGAY_TAO, System.currentTimeMillis());
        db.insert(BANG_QUAN_LY_VIEN, null, admin);

        // Khởi tạo 6 chỗ đỗ trống
        for (int i = 1; i <= 6; i++) {
            ContentValues choDo = new ContentValues();
            choDo.put(CD_ID, i);
            choDo.put(CD_TRANG_THAI, "TRONG");
            choDo.put(CD_BIEN_SO, "");
            choDo.put(CD_THOI_GIAN_VAO, 0);
            choDo.put(CD_LOAI_XE, "OTO");
            db.insert(BANG_CHO_DO, null, choDo);
        }

        // Thêm cấu hình giá mặc định
        themCauHinh(db, "GIA_OTO_GIO", "50000"); // 50k/giờ
        themCauHinh(db, "GIA_OTO_NGAY", "200000"); // 200k/ngày
        themCauHinh(db, "GIA_OTO_THANG", "3000000"); // 3tr/tháng
        themCauHinh(db, "THOI_GIAN_MIEN_PHI", "15"); // 15 phút miễn phí

        Log.d("DatabaseHelper", "Default data inserted");
    }

    /**
     * Thêm cấu hình vào database
     */
    private void themCauHinh(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(CH_KEY, key);
        values.put(CH_VALUE, value);
        db.insert(BANG_CAU_HINH, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa các bảng cũ nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + BANG_QUAN_LY_VIEN);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_CHO_DO);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_LICH_SU);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_THANH_TOAN);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_CAU_HINH);

        // Tạo lại
        onCreate(db);

        Log.d("DatabaseHelper", "Database upgraded from version " +
                oldVersion + " to " + newVersion);
    }

    /**
     * Xóa toàn bộ database (để reset)
     */
    public void xoaDatabase(Context context) {
        context.deleteDatabase(TEN_DATABASE);
        Log.d("DatabaseHelper", "Database deleted");
    }

    // ==================== GETTER CHO TÊN BẢNG & CỘT ====================
    // (Dùng cho các DAO class)

    public static String getBangQuanLyVien() {
        return BANG_QUAN_LY_VIEN;
    }

    public static String getBangChoDo() {
        return BANG_CHO_DO;
    }

    public static String getBangLichSu() {
        return BANG_LICH_SU;
    }

    public static String getBangThanhToan() {
        return BANG_THANH_TOAN;
    }

    public static String getBangCauHinh() {
        return BANG_CAU_HINH;
    }

    // Getter cho cột QuanLyVien
    public static String getQlId() { return QL_ID; }
    public static String getQlTaiKhoan() { return QL_TAI_KHOAN; }
    public static String getQlMatKhau() { return QL_MAT_KHAU; }
    public static String getQlHoTen() { return QL_HO_TEN; }
    public static String getQlSoDienThoai() { return QL_SO_DIEN_THOAI; }
    public static String getQlEmail() { return QL_EMAIL; }
    public static String getQlNgayTao() { return QL_NGAY_TAO; }

    // Getter cho cột ChoDo
    public static String getCdId() { return CD_ID; }
    public static String getCdTrangThai() { return CD_TRANG_THAI; }
    public static String getCdBienSo() { return CD_BIEN_SO; }
    public static String getCdThoiGianVao() { return CD_THOI_GIAN_VAO; }
    public static String getCdLoaiXe() { return CD_LOAI_XE; }

    // Getter cho cột LichSuXe
    public static String getLsId() { return LS_ID; }
    public static String getLsChoDo() { return LS_CHO_DO; }
    public static String getLsBienSo() { return LS_BIEN_SO; }
    public static String getLsLoaiXe() { return LS_LOAI_XE; }
    public static String getLsThoiGianVao() { return LS_THOI_GIAN_VAO; }
    public static String getLsThoiGianRa() { return LS_THOI_GIAN_RA; }
    public static String getLsThoiGianDoPhut() { return LS_THOI_GIAN_DO_PHUT; }
    public static String getLsTienPhi() { return LS_TIEN_PHI; }
    public static String getLsPhuongThuc() { return LS_PHUONG_THUC; }
    public static String getLsGhiChu() { return LS_GHI_CHU; }
    public static String getLsTrangThai() { return LS_TRANG_THAI; }

    // Getter cho cột ThanhToan
    public static String getTtId() { return TT_ID; }
    public static String getTtIdLichSu() { return TT_ID_LICH_SU; }
    public static String getTtSoTien() { return TT_SO_TIEN; }
    public static String getTtPhuongThuc() { return TT_PHUONG_THUC; }
    public static String getTtThoiGian() { return TT_THOI_GIAN; }
    public static String getTtTrangThai() { return TT_TRANG_THAI; }

    // Getter cho cột CauHinh
    public static String getChId() { return CH_ID; }
    public static String getChKey() { return CH_KEY; }
    public static String getChValue() { return CH_VALUE; }
}