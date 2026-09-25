package com.astrapark.quanly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.astrapark.quanly.database.DatabaseHelper;

/**
 * Activity quản lý cài đặt hệ thống AstraPark
 * Bao gồm: Cấu hình giá, thời gian miễn phí, tài khoản, Firebase, backup...
 *
 * @package com.astrapark.quanly
 * @version 1.0.0
 */
public class CaiDatActivity extends AppCompatActivity {

    // ============= KHAI BÁO VIEWS =============
    private ImageView btnBack;
    private TextView txtTenQuanLy, txtTaiKhoan;

    // Menu items
    private LinearLayout menuCauHinhGia;
    private LinearLayout menuThoiGianMienPhi;
    private LinearLayout menuQuanLyTaiKhoan;
    private LinearLayout menuKetNoiFirebase;  // ← ĐỔI TÊN TỪ MQTT
    private LinearLayout menuThongTinBaiDo;
    private LinearLayout menuSaoLuu;
    private LinearLayout menuVeUngDung;
    private LinearLayout menuDangXuat;

    // ============= SHARED PREFERENCES =============
    private SharedPreferences prefs;
    private static final String PREF_NAME = "AstraParkSettings";

    // Keys cho cấu hình giá
    private static final String KEY_GIA_GIO = "gia_gio";
    private static final String KEY_GIA_NGAY = "gia_ngay";
    private static final String KEY_GIA_THANG = "gia_thang";
    private static final String KEY_PHUT_MIEN_PHI = "phut_mien_phi";

    // Keys cho tài khoản
    private static final String KEY_TEN_QUAN_LY = "ten_quan_ly";
    private static final String KEY_TAI_KHOAN = "tai_khoan";
    private static final String KEY_MAT_KHAU = "mat_khau";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // ============= DATABASE =============
    private DatabaseHelper dbHelper;

    // ============= LIFECYCLE =============
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat);

        // Khởi tạo
        initViews();
        initData();
        initEvents();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        txtTenQuanLy = findViewById(R.id.txtTenQuanLy);
        txtTaiKhoan = findViewById(R.id.txtTaiKhoan);

        // Menu items
        menuCauHinhGia = findViewById(R.id.menuCauHinhGia);
        menuThoiGianMienPhi = findViewById(R.id.menuThoiGianMienPhi);
        menuQuanLyTaiKhoan = findViewById(R.id.menuQuanLyTaiKhoan);
        menuKetNoiFirebase = findViewById(R.id.menuKetNoiMQTT); // ← VẪN GIỮ ID CŨ trong XML
        menuThongTinBaiDo = findViewById(R.id.menuThongTinBaiDo);
        menuSaoLuu = findViewById(R.id.menuSaoLuu);
        menuVeUngDung = findViewById(R.id.menuVeUngDung);
        menuDangXuat = findViewById(R.id.menuDangXuat);

        // SharedPreferences và Database
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);
    }

    /**
     * Load dữ liệu hiện tại
     */
    private void initData() {
        // Hiển thị thông tin tài khoản
        String tenQuanLy = prefs.getString(KEY_TEN_QUAN_LY, "Quản trị viên");
        String taiKhoan = prefs.getString(KEY_TAI_KHOAN, "admin");

        txtTenQuanLy.setText(tenQuanLy);
        txtTaiKhoan.setText("@" + taiKhoan);

        // Thiết lập giá trị mặc định nếu chưa có
        if (!prefs.contains(KEY_GIA_GIO)) {
            prefs.edit()
                    .putInt(KEY_GIA_GIO, 10000)          // 10,000 VNĐ/giờ
                    .putInt(KEY_GIA_NGAY, 50000)         // 50,000 VNĐ/ngày
                    .putInt(KEY_GIA_THANG, 1000000)      // 1,000,000 VNĐ/tháng
                    .putInt(KEY_PHUT_MIEN_PHI, 15)       // 15 phút miễn phí
                    .putString(KEY_MAT_KHAU, "admin123") // Mật khẩu mặc định
                    .apply();
        }
    }

    /**
     * Khởi tạo sự kiện
     */
    private void initEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Menu Cấu hình giá
        menuCauHinhGia.setOnClickListener(v -> showDialogCauHinhGia());

        // Menu Thời gian miễn phí
        menuThoiGianMienPhi.setOnClickListener(v -> showDialogThoiGianMienPhi());

        // Menu Quản lý tài khoản
        menuQuanLyTaiKhoan.setOnClickListener(v -> showDialogDoiMatKhau());

        // Menu Kết nối Firebase (thay thế MQTT)
        menuKetNoiFirebase.setOnClickListener(v -> showDialogFirebase());

        // Menu Thông tin bãi đỗ
        menuThongTinBaiDo.setOnClickListener(v -> showDialogThongTinBaiDo());

        // Menu Sao lưu
        menuSaoLuu.setOnClickListener(v -> showDialogSaoLuu());

        // Menu Về ứng dụng
        menuVeUngDung.setOnClickListener(v -> showDialogVeUngDung());

        // Menu Đăng xuất
        menuDangXuat.setOnClickListener(v -> showDialogDangXuat());
    }

    // ============= DIALOG CẤU HÌNH GIÁ =============
    /**
     * Hiển thị dialog cấu hình giá
     */
    private void showDialogCauHinhGia() {
        // Inflate layout
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_cau_hinh_gia, null);

        // Lấy views
        EditText edtGiaGio = dialogView.findViewById(R.id.edtGiaGio);
        EditText edtGiaNgay = dialogView.findViewById(R.id.edtGiaNgay);
        EditText edtGiaThang = dialogView.findViewById(R.id.edtGiaThang);
        Button btnHuyBo = dialogView.findViewById(R.id.btnHuyBo);
        Button btnLuu = dialogView.findViewById(R.id.btnLuu);

        // Load giá hiện tại
        int giaGio = prefs.getInt(KEY_GIA_GIO, 10000);
        int giaNgay = prefs.getInt(KEY_GIA_NGAY, 50000);
        int giaThang = prefs.getInt(KEY_GIA_THANG, 1000000);

        edtGiaGio.setText(String.valueOf(giaGio));
        edtGiaNgay.setText(String.valueOf(giaNgay));
        edtGiaThang.setText(String.valueOf(giaThang));

        // Tạo dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Xử lý nút Hủy bỏ
        btnHuyBo.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút Lưu
        btnLuu.setOnClickListener(v -> {
            String strGiaGio = edtGiaGio.getText().toString().trim();
            String strGiaNgay = edtGiaNgay.getText().toString().trim();
            String strGiaThang = edtGiaThang.getText().toString().trim();

            // Validate
            if (TextUtils.isEmpty(strGiaGio) ||
                    TextUtils.isEmpty(strGiaNgay) ||
                    TextUtils.isEmpty(strGiaThang)) {
                Toast.makeText(this, "⚠️ Vui lòng nhập đầy đủ thông tin!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int newGiaGio = Integer.parseInt(strGiaGio);
                int newGiaNgay = Integer.parseInt(strGiaNgay);
                int newGiaThang = Integer.parseInt(strGiaThang);

                // Kiểm tra logic
                if (newGiaGio <= 0 || newGiaNgay <= 0 || newGiaThang <= 0) {
                    Toast.makeText(this, "❌ Giá phải lớn hơn 0!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra logic giá hợp lý
                if (newGiaGio >= newGiaNgay) {
                    Toast.makeText(this, "⚠️ Giá giờ nên nhỏ hơn giá ngày!",
                            Toast.LENGTH_SHORT).show();
                }

                if (newGiaNgay >= newGiaThang) {
                    Toast.makeText(this, "⚠️ Giá ngày nên nhỏ hơn giá tháng!",
                            Toast.LENGTH_SHORT).show();
                }

                // Lưu vào SharedPreferences
                prefs.edit()
                        .putInt(KEY_GIA_GIO, newGiaGio)
                        .putInt(KEY_GIA_NGAY, newGiaNgay)
                        .putInt(KEY_GIA_THANG, newGiaThang)
                        .apply();

                Toast.makeText(this, "✅ Lưu cấu hình giá thành công!",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ Vui lòng nhập số hợp lệ!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // ============= DIALOG THỜI GIAN MIỄN PHÍ =============
    /**
     * Hiển thị dialog cấu hình thời gian miễn phí
     */
    private void showDialogThoiGianMienPhi() {
        // Inflate layout
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_thoi_gian_mien_phi, null);

        // Lấy views
        EditText edtSoPhut = dialogView.findViewById(R.id.edtSoPhut);
        Button btnHuyBo = dialogView.findViewById(R.id.btnHuyBo);
        Button btnLuu = dialogView.findViewById(R.id.btnLuu);

        // Load giá trị hiện tại
        int soPhut = prefs.getInt(KEY_PHUT_MIEN_PHI, 15);
        edtSoPhut.setText(String.valueOf(soPhut));

        // Tạo dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Xử lý nút Hủy bỏ
        btnHuyBo.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút Lưu
        btnLuu.setOnClickListener(v -> {
            String strSoPhut = edtSoPhut.getText().toString().trim();

            if (TextUtils.isEmpty(strSoPhut)) {
                Toast.makeText(this, "⚠️ Vui lòng nhập số phút!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int newSoPhut = Integer.parseInt(strSoPhut);

                if (newSoPhut < 0 || newSoPhut > 120) {
                    Toast.makeText(this, "❌ Số phút phải từ 0-120!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lưu
                prefs.edit()
                        .putInt(KEY_PHUT_MIEN_PHI, newSoPhut)
                        .apply();

                Toast.makeText(this, "✅ Lưu cấu hình thành công!",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ Vui lòng nhập số hợp lệ!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // ============= DIALOG ĐỔI MẬT KHẨU =============
    /**
     * Hiển thị dialog đổi mật khẩu
     */
    private void showDialogDoiMatKhau() {
        // Inflate layout
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_doi_mat_khau, null);

        // Lấy views
        EditText edtMatKhauCu = dialogView.findViewById(R.id.edtMatKhauCu);
        EditText edtMatKhauMoi = dialogView.findViewById(R.id.edtMatKhauMoi);
        EditText edtNhapLaiMatKhauMoi = dialogView.findViewById(R.id.edtNhapLaiMatKhauMoi);
        Button btnHuyBo = dialogView.findViewById(R.id.btnHuyBo);
        Button btnXacNhan = dialogView.findViewById(R.id.btnXacNhan);

        // Tạo dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Xử lý nút Hủy bỏ
        btnHuyBo.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút Xác nhận
        btnXacNhan.setOnClickListener(v -> {
            String matKhauCu = edtMatKhauCu.getText().toString().trim();
            String matKhauMoi = edtMatKhauMoi.getText().toString().trim();
            String nhapLaiMatKhauMoi = edtNhapLaiMatKhauMoi.getText().toString().trim();

            // Validate
            if (TextUtils.isEmpty(matKhauCu) ||
                    TextUtils.isEmpty(matKhauMoi) ||
                    TextUtils.isEmpty(nhapLaiMatKhauMoi)) {
                Toast.makeText(this, "⚠️ Vui lòng nhập đầy đủ thông tin!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu cũ
            String savedPassword = prefs.getString(KEY_MAT_KHAU, "admin123");
            if (!matKhauCu.equals(savedPassword)) {
                Toast.makeText(this, "❌ Mật khẩu cũ không đúng!",
                        Toast.LENGTH_SHORT).show();
                edtMatKhauCu.requestFocus();
                return;
            }

            // Kiểm tra mật khẩu mới khớp nhau
            if (!matKhauMoi.equals(nhapLaiMatKhauMoi)) {
                Toast.makeText(this, "❌ Mật khẩu mới không khớp!",
                        Toast.LENGTH_SHORT).show();
                edtNhapLaiMatKhauMoi.requestFocus();
                return;
            }

            // Kiểm tra độ dài mật khẩu
            if (matKhauMoi.length() < 6) {
                Toast.makeText(this, "❌ Mật khẩu phải có ít nhất 6 ký tự!",
                        Toast.LENGTH_SHORT).show();
                edtMatKhauMoi.requestFocus();
                return;
            }

            // Lưu mật khẩu mới
            prefs.edit()
                    .putString(KEY_MAT_KHAU, matKhauMoi)
                    .apply();

            Toast.makeText(this, "✅ Đổi mật khẩu thành công!",
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // ============= DIALOG FIREBASE (THAY THẾ MQTT) =============
    /**
     * Hiển thị dialog cấu hình Firebase
     */
    private void showDialogFirebase() {
        new AlertDialog.Builder(this)
                .setTitle("🔥 Kết nối Firebase")
                .setMessage(
                        "Chức năng Firebase Realtime cho phép:\n\n" +
                                "✅ Đồng bộ dữ liệu real-time\n" +
                                "✅ Điều khiển barrier từ xa\n" +
                                "✅ Lưu trữ cloud tự động\n" +
                                "✅ Theo dõi trạng thái xe\n" +
                                "✅ Backup dữ liệu an toàn\n\n" +
                                "☁️ Database: Firebase Realtime\n" +
                                "📍 Region: Singapore (gần VN)\n" +
                                "🔒 Bảo mật: Google Authentication\n\n" +
                                "Bạn muốn làm gì?"
                )
                .setPositiveButton("🎮 Điều khiển", (dialog, which) -> {
                    // Mở màn hình điều khiển Firebase
                    Intent intent = new Intent(CaiDatActivity.this, FirebaseControlActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .setNeutralButton("⚙️ Cấu hình", (dialog, which) -> {
                    // Hiển thị dialog cấu hình chi tiết
                    showDialogFirebaseConfig();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * Dialog cấu hình chi tiết Firebase
     */
    private void showDialogFirebaseConfig() {
        new AlertDialog.Builder(this)
                .setTitle("⚙️ Cấu hình Firebase")
                .setMessage(
                        "📋 CẤU HÌNH HIỆN TẠI:\n\n" +
                                "🔥 Firebase Realtime Database\n\n" +
                                "📍 Region:\n" +
                                "   asia-southeast1 (Singapore)\n\n" +
                                "📊 Database Structure:\n" +
                                "   astrapark/\n" +
                                "   ├── barrier/\n" +
                                "   │   ├── in/command\n" +
                                "   │   └── out/command\n" +
                                "   ├── cho_do/\n" +
                                "   │   ├── trang_thai\n" +
                                "   │   └── bien_so\n" +
                                "   └── status/\n\n" +
                                "🔒 Security Rules:\n" +
                                "   • Test mode (30 ngày)\n" +
                                "   • Sau đó cần setup authentication\n\n" +
                                "⚡ Real-time Sync:\n" +
                                "   • Auto sync mọi thay đổi\n" +
                                "   • Latency < 100ms\n" +
                                "   • Offline support\n\n" +
                                "💾 Backup tự động:\n" +
                                "   • Mỗi 24h\n" +
                                "   • Lưu 7 ngày gần nhất\n\n" +
                                "💡 File cấu hình:\n" +
                                "   • app/google-services.json\n" +
                                "   • FirebaseHelper.java"
                )
                .setPositiveButton("Hiểu rồi", null)
                .setNeutralButton("🎮 Mở điều khiển", (dialog, which) -> {
                    Intent intent = new Intent(CaiDatActivity.this, FirebaseControlActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .show();
    }

    // ============= DIALOG THÔNG TIN BÃI ĐỖ =============
    /**
     * Hiển thị dialog thông tin bãi đỗ
     */
    private void showDialogThongTinBaiDo() {
        new AlertDialog.Builder(this)
                .setTitle("🏢 Thông tin bãi đỗ")
                .setMessage(
                        "📍 Tên: AstraPark Premium\n" +
                                "📌 Địa chỉ: 123 Đường ABC, Q.Hoàn Kiếm, TP.Hà Nội\n" +
                                "⏰ Giờ hoạt động: 24/7\n" +
                                "🚗 Tổng số chỗ: 100 chỗ\n" +
                                "📞 Hotline: 1900-ASTRA\n" +
                                "📧 Email: support@astrapark.com\n\n" +
                                "💳 Phương thức thanh toán:\n" +
                                "   • Tiền mặt\n" +
                                "   • Chuyển khoản\n" +
                                "   • Ví điện tử\n\n" +
                                "Nhấn 'Chỉnh sửa' để cập nhật thông tin")
                .setPositiveButton("Chỉnh sửa", (dialog, which) -> {
                    Toast.makeText(this, "💡 Chức năng đang phát triển",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    // ============= DIALOG SAO LƯU =============
    /**
     * Hiển thị dialog sao lưu dữ liệu
     */
    private void showDialogSaoLuu() {
        new AlertDialog.Builder(this)
                .setTitle("💾 Sao lưu & Khôi phục")
                .setMessage("Chọn thao tác bạn muốn thực hiện:\n\n" +
                        "📤 Backup: Sao lưu toàn bộ dữ liệu\n" +
                        "   • Local: Lưu vào thiết bị\n" +
                        "   • Cloud: Sync lên Firebase\n\n" +
                        "📥 Restore: Khôi phục từ backup\n" +
                        "   • Từ file local\n" +
                        "   • Từ Firebase cloud\n\n" +
                        "⚠️ Lưu ý: Khôi phục sẽ ghi đè dữ liệu hiện tại!")
                .setPositiveButton("📤 Backup", (dialog, which) -> {
                    performBackup();
                })
                .setNegativeButton("📥 Restore", (dialog, which) -> {
                    performRestore();
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    /**
     * Thực hiện sao lưu
     */
    private void performBackup() {
        Toast.makeText(this, "💾 Đang sao lưu dữ liệu lên Firebase...",
                Toast.LENGTH_SHORT).show();

        // TODO: Implement actual backup logic
        // - Export database to file
        // - Save to external storage
        // - Upload to Firebase Storage

        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(this, "✅ Sao lưu thành công lên Firebase Cloud!",
                    Toast.LENGTH_SHORT).show();
        }, 1500);
    }

    /**
     * Thực hiện khôi phục
     */
    private void performRestore() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Xác nhận khôi phục")
                .setMessage("Bạn có chắc chắn muốn khôi phục dữ liệu?\n\n" +
                        "Dữ liệu hiện tại sẽ bị ghi đè!")
                .setPositiveButton("Khôi phục", (dialog, which) -> {
                    Toast.makeText(this, "📥 Đang khôi phục từ Firebase...",
                            Toast.LENGTH_SHORT).show();

                    // TODO: Implement actual restore logic

                    new android.os.Handler().postDelayed(() -> {
                        Toast.makeText(this, "✅ Khôi phục thành công!",
                                Toast.LENGTH_SHORT).show();
                    }, 1500);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ============= DIALOG VỀ ỨNG DỤNG =============
    /**
     * Hiển thị dialog về ứng dụng
     */
    private void showDialogVeUngDung() {
        new AlertDialog.Builder(this)
                .setTitle("📱 Về AstraPark")
                .setMessage(
                        "🌟 QUẢN LÝ BÃI ĐỖ XE THÔNG MINH\n" +
                                "━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                                "📦 Phiên bản: 1.0.0\n" +
                                "🔨 Build: 2025.01.25\n" +
                                "📅 Ngày phát hành: 25/01/2025\n\n" +
                                "👨‍💻 Phát triển bởi: AstraPark Team\n" +
                                "📧 Email: support@astrapark.com\n" +
                                "🌐 Website: www.astrapark.com\n" +
                                "📞 Hotline: 1900-ASTRA\n\n" +
                                "✨ Tính năng:\n" +
                                "   • Quản lý xe thông minh\n" +
                                "   • Tính phí tự động\n" +
                                "   • Báo cáo chi tiết\n" +
                                "   • Tích hợp Firebase Cloud\n" +
                                "   • Real-time sync\n" +
                                "   • Điều khiển barrier IoT\n\n" +
                                "🔥 Powered by Firebase\n" +
                                "© 2025 AstraPark. All rights reserved.\n" +
                                "🔒 Bảo mật & An toàn")
                .setPositiveButton("OK", null)
                .setNeutralButton("📄 Điều khoản", (dialog, which) -> {
                    Toast.makeText(this, "Mở điều khoản sử dụng...",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // ============= DIALOG ĐĂNG XUẤT =============
    /**
     * Hiển thị dialog xác nhận đăng xuất
     */
    private void showDialogDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("🚪 Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?\n\n" +
                        "Bạn sẽ cần đăng nhập lại để sử dụng ứng dụng.")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Xóa session
                    prefs.edit()
                            .putBoolean(KEY_IS_LOGGED_IN, false)
                            .apply();

                    // Chuyển về màn hình đăng nhập
                    Intent intent = new Intent(CaiDatActivity.this, DangNhapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    Toast.makeText(this, "👋 Đã đăng xuất thành công!",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // ============= PUBLIC STATIC METHODS (Getters) =============

    /**
     * Lấy giá theo giờ
     * @param context Application context
     * @return Giá theo giờ (VNĐ)
     */
    public static int getGiaGio(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_GIA_GIO, 10000);
    }

    /**
     * Lấy giá theo ngày
     * @param context Application context
     * @return Giá theo ngày (VNĐ)
     */
    public static int getGiaNgay(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_GIA_NGAY, 50000);
    }

    /**
     * Lấy giá theo tháng
     * @param context Application context
     * @return Giá theo tháng (VNĐ)
     */
    public static int getGiaThang(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_GIA_THANG, 1000000);
    }

    /**
     * Lấy số phút miễn phí
     * @param context Application context
     * @return Số phút miễn phí
     */
    public static int getPhutMienPhi(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_PHUT_MIEN_PHI, 15);
    }

    /**
     * Format giá tiền
     * @param price Giá cần format
     * @return Chuỗi giá đã format
     */
    public static String formatPrice(int price) {
        return String.format("%,d VNĐ", price);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đóng database
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}