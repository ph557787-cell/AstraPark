package com.astrapark.quanly;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// THÊM IMPORT MỚI
import com.astrapark.quanly.database.QuanLyVienDAO;
import com.astrapark.quanly.model.QuanLyVien;

public class DangNhapActivity extends AppCompatActivity {

    // Khai báo các view
    private EditText edtTaiKhoan, edtMatKhau;
    private ImageView imgHienMatKhau;
    private CheckBox chkGhiNhoDangNhap;
    private Button btnDangNhap;
    private TextView txtQuenMatKhau, txtLienHeHoTro;
    private LinearLayout layoutDangNhapVanTay;

    // Biến kiểm tra hiển thị mật khẩu
    private boolean hienThiMatKhau = false;

    // SharedPreferences để lưu thông tin đăng nhập
    private SharedPreferences sharedPreferences;
    private static final String TEN_FILE_LUU = "ThongTinDangNhap";
    private static final String KEY_TAI_KHOAN = "taiKhoan";
    private static final String KEY_MAT_KHAU = "matKhau";
    private static final String KEY_GHI_NHO = "ghiNho";
    private static final String KEY_ID_QUAN_LY = "idQuanLy"; // THÊM MỚI
    private static final String KEY_HO_TEN = "hoTen"; // THÊM MỚI

    // ===== THÊM DAO =====
    private QuanLyVienDAO quanLyVienDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);

        // Ẩn thanh status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(TEN_FILE_LUU, MODE_PRIVATE);

        // ===== KHỞI TẠO DAO =====
        quanLyVienDAO = new QuanLyVienDAO(this);

        // Khởi tạo các view
        khoiTaoView();

        // Kiểm tra đã lưu thông tin đăng nhập chưa
        kiemTraThongTinDaLuu();

        // Thiết lập sự kiện
        thietLapSuKien();
    }

    /**
     * Khởi tạo các view từ layout
     */
    private void khoiTaoView() {
        edtTaiKhoan = findViewById(R.id.edtTaiKhoan);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        imgHienMatKhau = findViewById(R.id.imgHienMatKhau);
        chkGhiNhoDangNhap = findViewById(R.id.chkGhiNhoDangNhap);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        txtQuenMatKhau = findViewById(R.id.txtQuenMatKhau);
        txtLienHeHoTro = findViewById(R.id.txtLienHeHoTro);
        layoutDangNhapVanTay = findViewById(R.id.layoutDangNhapVanTay);
    }

    /**
     * Kiểm tra thông tin đã được lưu trong SharedPreferences chưa
     */
    private void kiemTraThongTinDaLuu() {
        boolean daGhiNho = sharedPreferences.getBoolean(KEY_GHI_NHO, false);

        if (daGhiNho) {
            // Lấy thông tin đã lưu
            String taiKhoanDaLuu = sharedPreferences.getString(KEY_TAI_KHOAN, "");
            String matKhauDaLuu = sharedPreferences.getString(KEY_MAT_KHAU, "");

            // Điền vào form
            edtTaiKhoan.setText(taiKhoanDaLuu);
            edtMatKhau.setText(matKhauDaLuu);
            chkGhiNhoDangNhap.setChecked(true);
        }
    }

    /**
     * Thiết lập sự kiện cho các view
     */
    private void thietLapSuKien() {
        // Sự kiện nút đăng nhập
        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyDangNhap();
            }
        });

        // Sự kiện hiển thị/ẩn mật khẩu
        imgHienMatKhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chuyenDoiHienThiMatKhau();
            }
        });

        // Sự kiện quên mật khẩu
        txtQuenMatKhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyQuenMatKhau();
            }
        });

        // Sự kiện liên hệ hỗ trợ
        txtLienHeHoTro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyLienHeHoTro();
            }
        });

        // Sự kiện đăng nhập vân tay
        layoutDangNhapVanTay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyDangNhapVanTay();
            }
        });
    }

    /**
     * Xử lý đăng nhập - SỬA LẠI DÙNG DATABASE
     */
    private void xuLyDangNhap() {
        // Lấy thông tin từ form
        String taiKhoan = edtTaiKhoan.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString().trim();

        // Validate dữ liệu
        if (taiKhoan.isEmpty()) {
            edtTaiKhoan.setError("Vui lòng nhập tài khoản");
            edtTaiKhoan.requestFocus();
            return;
        }

        if (matKhau.isEmpty()) {
            edtMatKhau.setError("Vui lòng nhập mật khẩu");
            edtMatKhau.requestFocus();
            return;
        }

        if (matKhau.length() < 6) {
            edtMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtMatKhau.requestFocus();
            return;
        }

        // ===== KIỂM TRA ĐĂNG NHẬP TỪ DATABASE =====
        QuanLyVien quanLy = quanLyVienDAO.kiemTraDangNhap(taiKhoan, matKhau);

        if (quanLy != null) {
            // Đăng nhập thành công

            // Lưu thông tin nếu chọn ghi nhớ
            if (chkGhiNhoDangNhap.isChecked()) {
                luuThongTinDangNhap(quanLy);
            } else {
                xoaThongTinDangNhap();
            }

            // Hiển thị thông báo
            Toast.makeText(this,
                    "Xin chào " + quanLy.getHoTen() + "!",
                    Toast.LENGTH_SHORT).show();

            // Chuyển sang màn hình Dashboard
            chuyenSangDashboard();

        } else {
            // Đăng nhập thất bại
            Toast.makeText(this, getString(R.string.dang_nhap_that_bai),
                    Toast.LENGTH_LONG).show();

            // Xóa mật khẩu và focus lại
            edtMatKhau.setText("");
            edtMatKhau.requestFocus();
        }
    }

    /**
     * Lưu thông tin đăng nhập vào SharedPreferences - SỬA LẠI
     */
    private void luuThongTinDangNhap(QuanLyVien quanLy) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID_QUAN_LY, quanLy.getId());
        editor.putString(KEY_TAI_KHOAN, quanLy.getTaiKhoan());
        editor.putString(KEY_MAT_KHAU, quanLy.getMatKhau());
        editor.putString(KEY_HO_TEN, quanLy.getHoTen());
        editor.putBoolean(KEY_GHI_NHO, true);
        editor.apply();
    }

    /**
     * Xóa thông tin đăng nhập đã lưu
     */
    private void xoaThongTinDangNhap() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ID_QUAN_LY);
        editor.remove(KEY_TAI_KHOAN);
        editor.remove(KEY_MAT_KHAU);
        editor.remove(KEY_HO_TEN);
        editor.putBoolean(KEY_GHI_NHO, false);
        editor.apply();
    }

    /**
     * Chuyển đổi hiển thị/ẩn mật khẩu
     */
    private void chuyenDoiHienThiMatKhau() {
        if (hienThiMatKhau) {
            // Ẩn mật khẩu
            edtMatKhau.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imgHienMatKhau.setImageResource(R.drawable.ic_visibility_off);
            hienThiMatKhau = false;
        } else {
            // Hiện mật khẩu
            edtMatKhau.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imgHienMatKhau.setImageResource(R.drawable.ic_visibility);
            hienThiMatKhau = true;
        }

        // Đưa con trỏ về cuối text
        edtMatKhau.setSelection(edtMatKhau.getText().length());
    }

    /**
     * Xử lý quên mật khẩu
     */
    private void xuLyQuenMatKhau() {
        Toast.makeText(this, "Chức năng đang phát triển\n" +
                        "Vui lòng liên hệ quản trị viên để lấy lại mật khẩu",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Xử lý liên hệ hỗ trợ
     */
    private void xuLyLienHeHoTro() {
        Toast.makeText(this, "Hotline: 1900-xxxx\nEmail: support@astrapark.com",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Xử lý đăng nhập bằng vân tay
     */
    private void xuLyDangNhapVanTay() {
        Toast.makeText(this, "Chức năng đăng nhập vân tay\n" +
                        "Sẽ được phát triển trong phiên bản tiếp theo",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Chuyển sang màn hình Dashboard
     */
    private void chuyenSangDashboard() {
        Intent intent = new Intent(DangNhapActivity.this, DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * Xử lý nút Back
     */
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Nhấn Back một lần nữa để thoát",
                Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }
}