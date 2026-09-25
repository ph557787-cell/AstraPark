package com.astrapark.quanly;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat; // ← THÊM
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem; // ← THÊM
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astrapark.quanly.adapter.ChoDoAdapter;
import com.astrapark.quanly.firebase.FirebaseHelper;
import com.astrapark.quanly.model.ChoDo;
import com.astrapark.quanly.database.ChoDoDAO;
import com.astrapark.quanly.database.LichSuXeDAO;
import com.astrapark.quanly.database.CauHinhDAO;
import com.astrapark.quanly.model.LichSuXe;
import com.google.android.material.navigation.NavigationView; // ← THÊM

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private FirebaseHelper firebaseHelper;

    // Khai báo các view
    private DrawerLayout drawerLayout;
    private NavigationView navigationView; // ← THÊM
    private ImageView imgIconMenu;
    private TextView txtTenQuanLy;
    private TextView txtTongXeDangDo, txtChoTrong, txtDoanhThuHomNay;
    private RecyclerView recyclerViewChoDo;
    private LinearLayout btnXeVaoRa, btnLichSu, btnThongKe;

    // Adapter cho RecyclerView
    private ChoDoAdapter choDoAdapter;
    private List<ChoDo> danhSachChoDo;

    // Handler để cập nhật real-time
    private Handler handler;
    private Runnable capNhatRunnable;

    // SharedPreferences
    private SharedPreferences sharedPreferences;

    // DAO
    private ChoDoDAO choDoDAO;
    private LichSuXeDAO lichSuXeDAO;
    private CauHinhDAO cauHinhDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Ẩn status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Khởi tạo
        sharedPreferences = getSharedPreferences("ThongTinDangNhap", MODE_PRIVATE);

        // Khởi tạo DAO
        choDoDAO = new ChoDoDAO(this);
        lichSuXeDAO = new LichSuXeDAO(this);
        cauHinhDAO = new CauHinhDAO(this);

        // Khởi tạo view
        khoiTaoView();

        // ========== THÊM PHẦN NÀY ==========
        // Setup Navigation Drawer
        setupNavigationDrawer();
        // ===================================

        // Tải dữ liệu từ database
        taiDuLieuTuDatabase();

        // Setup RecyclerView
        setupRecyclerView();

        // Hiển thị thông tin
        hienThiThongTin();

        // Thiết lập sự kiện
        thietLapSuKien();

        // Bắt đầu cập nhật real-time
        batDauCapNhatRealTime();

        // Xử lý nút Back
        xuLyNutBack();

        // DEBUG
        debugDatabase();

        // ===== KHỞI TẠO FIREBASE =====
        initFirebase();
    }

    /**
     * Khởi tạo các view
     */
    private void khoiTaoView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView); // ← THÊM
        imgIconMenu = findViewById(R.id.imgIconMenu);
        txtTenQuanLy = findViewById(R.id.txtTenQuanLy);
        txtTongXeDangDo = findViewById(R.id.txtTongXeDangDo);
        txtChoTrong = findViewById(R.id.txtChoTrong);
        txtDoanhThuHomNay = findViewById(R.id.txtDoanhThuHomNay);
        recyclerViewChoDo = findViewById(R.id.recyclerViewChoDo);
        btnXeVaoRa = findViewById(R.id.btnXeVaoRa);
        btnLichSu = findViewById(R.id.btnLichSu);
        btnThongKe = findViewById(R.id.btnThongKe);

        // ========== THÊM DÒNG NÀY ==========
        LinearLayout btnSoDoRealtime = findViewById(R.id.btnSoDoRealtime);
        // ===================================

        // Lấy tên quản lý từ SharedPreferences
        String tenQuanLy = sharedPreferences.getString("hoTen", "Admin");
        txtTenQuanLy.setText(tenQuanLy.toUpperCase());
    }

    /**
     * ✅ KHỞI TẠO FIREBASE - LẮNG NGHE REALTIME
     */
    private void initFirebase() {
        try {
            Log.d("Dashboard", "========== INIT FIREBASE ==========");

            firebaseHelper = new FirebaseHelper();

            // ✅ Lắng nghe thay đổi chỗ đỗ
            firebaseHelper.setChoDoListener((choDoId, trangThai, bienSo, thoiGianVao) -> {
                runOnUiThread(() -> {
                    Log.d("Dashboard", String.format("🔄 Firebase update: Chỗ %d = %s (%s) | Time: %d",
                            choDoId, trangThai, bienSo, thoiGianVao));

                    // Reload data
                    taiDuLieuTuDatabase();
                    choDoAdapter.capNhatDanhSach(danhSachChoDo);
                    hienThiThongTin();
                });
            });

            // ✅ Lắng nghe status
            firebaseHelper.setStatusListener((totalCars, availableSlots) -> {
                runOnUiThread(() -> {
                    txtTongXeDangDo.setText(String.valueOf(totalCars));
                    txtChoTrong.setText(String.valueOf(availableSlots));
                });
            });

            Log.d("Dashboard", "✅ Firebase listener started");

        } catch (Exception e) {
            Log.e("Dashboard", "❌ Error init Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== THÊM METHOD MỚI ==========
    /**
     * Setup Navigation Drawer
     */
    private void setupNavigationDrawer() {
        // Cập nhật thông tin trong Navigation Header
        View headerView = navigationView.getHeaderView(0);
        TextView navTenQuanLy = headerView.findViewById(R.id.navTenQuanLy);
        TextView navEmail = headerView.findViewById(R.id.navEmail);

        String tenQuanLy = sharedPreferences.getString("hoTen", "Admin");
        String taiKhoan = sharedPreferences.getString("taiKhoan", "admin");

        navTenQuanLy.setText(tenQuanLy.toUpperCase());
        navEmail.setText(taiKhoan + "@astrapark.com");

        // Set màn hình hiện tại là Dashboard
        navigationView.setCheckedItem(R.id.nav_dashboard);

        // Xử lý sự kiện click menu items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                // Đóng drawer trước
                drawerLayout.closeDrawer(GravityCompat.START);

                // Delay 250ms để animation drawer mượt
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xuLyClickMenuItem(id);
                    }
                }, 250);

                return true;
            }
        });
    }

    /**
     * Xử lý click menu item
     */
    private void xuLyClickMenuItem(int itemId) {
        Intent intent = null;

        if (itemId == R.id.nav_dashboard) {
            // Đã ở Dashboard rồi
            Toast.makeText(this, "🏠 Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show();
            return;

        } else if (itemId == R.id.nav_xe_vao_ra) {
            // Mở màn hình Xe vào/ra
            intent = new Intent(this, XeVaoRaActivity.class);

        } else if (itemId == R.id.nav_lich_su) {
            // Mở màn hình Lịch sử
            intent = new Intent(this, LichSuActivity.class);

        } else if (itemId == R.id.nav_thong_ke) {
            // Mở màn hình Thống kê
            intent = new Intent(this, ThongKeActivity.class);

            // ========== THÊM ĐOẠN NÀY ==========
        } else if (itemId == R.id.nav_so_do_realtime) {
            // Mở màn hình Sơ đồ Realtime
            intent = new Intent(this, ChoDoRealtimeActivity.class);
            // ===================================

        } else if (itemId == R.id.nav_tim_kiem) {
            // Mở màn hình Tìm kiếm
            intent = new Intent(this, TimKiemActivity.class);

        } else if (itemId == R.id.nav_cai_dat) {
            // Mở màn hình Cài đặt
            intent = new Intent(this, CaiDatActivity.class);

        } else if (itemId == R.id.nav_help) {
            // Hiển thị trợ giúp
            hienThiTroGiup();
            return;

        } else if (itemId == R.id.nav_logout) {
            // Đăng xuất
            xacNhanDangXuat();
            return;
        }

        // Nếu có intent, mở màn hình mới
        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    /**
     * Hiển thị dialog trợ giúp
     */
    private void hienThiTroGiup() {
        new AlertDialog.Builder(this)
                .setTitle("❓ TRỢ GIÚP")
                .setMessage(
                        "📱 HƯỚNG DẪN SỬ DỤNG ASTRAPARK\n\n" +
                                "🏠 Trang chủ:\n" +
                                "   • Xem tổng quan bãi đỗ\n" +
                                "   • Click vào chỗ đỗ để xem chi tiết\n\n" +
                                "🚗 Xe vào/ra:\n" +
                                "   • Quản lý xe vào/ra bãi\n" +
                                "   • Quét biển số tự động\n\n" +
                                "📋 Lịch sử:\n" +
                                "   • Xem lịch sử đỗ xe\n" +
                                "   • Lọc theo thời gian\n\n" +
                                "📊 Thống kê:\n" +
                                "   • Xem báo cáo doanh thu\n" +
                                "   • Biểu đồ trực quan\n\n" +
                                "⚙️ Cài đặt:\n" +
                                "   • Cấu hình giá, thời gian\n" +
                                "   • Quản lý tài khoản\n\n" +
                                "📞 Hỗ trợ: 1900-ASTRA\n" +
                                "📧 Email: support@astrapark.com"
                )
                .setPositiveButton("Đã hiểu", null)
                .setNeutralButton("📞 Liên hệ", (dialog, which) -> {
                    Toast.makeText(this, "☎️ Gọi: 1900-ASTRA", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * Xác nhận đăng xuất
     */
    private void xacNhanDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("🚪 Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Xóa session
                        sharedPreferences.edit()
                                .putBoolean("isLoggedIn", false)
                                .apply();

                        // Chuyển về màn hình đăng nhập
                        Intent intent = new Intent(DashboardActivity.this, DangNhapActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                        Toast.makeText(DashboardActivity.this,
                                "👋 Đã đăng xuất thành công!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    // =====================================

    /**
     * Tải dữ liệu từ database
     */
    private void taiDuLieuTuDatabase() {
        danhSachChoDo = choDoDAO.layTatCa();

        if (danhSachChoDo == null || danhSachChoDo.isEmpty()) {
            danhSachChoDo = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                danhSachChoDo.add(new ChoDo(i, "OTO"));
            }
        }
    }

    /**
     * Debug database
     */
    private void debugDatabase() {
        try {
            List<ChoDo> dsChoDo = choDoDAO.layTatCa();
            Log.d("DEBUG", "=== DANH SÁCH CHỖ ĐỖ ===");
            for (ChoDo cho : dsChoDo) {
                Log.d("DEBUG", "Chỗ " + cho.getId() + ": " + cho.getTrangThai() +
                        " - " + cho.getBienSoXe());
            }

            List<LichSuXe> dsLichSu = lichSuXeDAO.layXeDangDo();
            Log.d("DEBUG", "=== XE ĐANG ĐỖ ===");
            for (LichSuXe xe : dsLichSu) {
                Log.d("DEBUG", "ID: " + xe.getId() + " - Chỗ: " + xe.getChoDo() +
                        " - Biển: " + xe.getBienSoXe() + " - Trạng thái: " + xe.getTrangThai());
            }

        } catch (Exception e) {
            Log.e("DEBUG", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewChoDo.setLayoutManager(layoutManager);

        choDoAdapter = new ChoDoAdapter(danhSachChoDo, new ChoDoAdapter.OnChoDoClickListener() {
            @Override
            public void onChoDoClick(ChoDo choDo, int position) {
                hienThiThongTinChoDo(choDo, position);
            }
        });

        recyclerViewChoDo.setAdapter(choDoAdapter);
    }

    /**
     * Hiển thị thông tin
     */
    private void hienThiThongTin() {
        int tongXe = choDoDAO.demXeDangDo();
        int choTrong = choDoDAO.demChoTrong();
        long doanhThu = lichSuXeDAO.tinhDoanhThuHomNay();

        txtTongXeDangDo.setText(String.valueOf(tongXe));
        txtChoTrong.setText(String.valueOf(choTrong));

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        if (doanhThu >= 1000000) {
            txtDoanhThuHomNay.setText(String.format("%.1fM", doanhThu / 1000000.0));
        } else if (doanhThu >= 1000) {
            txtDoanhThuHomNay.setText(formatter.format(doanhThu / 1000) + "K");
        } else {
            txtDoanhThuHomNay.setText(doanhThu + "đ");
        }
    }

    /**
     * Thiết lập sự kiện
     */
    private void thietLapSuKien() {
        // ========== SỬA LẠI PHẦN NÀY ==========
        // Menu icon - MỞ NAVIGATION DRAWER
        imgIconMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        // ======================================

        // Nút Xe vào/ra
        btnXeVaoRa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, XeVaoRaActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // Nút Lịch sử
        btnLichSu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, LichSuActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // Nút Thống kê
        btnThongKe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ThongKeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // ========== THÊM ĐOẠN NÀY ==========
        // Nút Sơ đồ Realtime (nếu có trong layout)
        LinearLayout btnSoDoRealtime = findViewById(R.id.btnSoDoRealtime);
        if (btnSoDoRealtime != null) {
            btnSoDoRealtime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DashboardActivity.this, ChoDoRealtimeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
        // ===================================
    }

    /**
     * Hiển thị dialog thông tin chi tiết chỗ đỗ
     */
    private void hienThiThongTinChoDo(ChoDo choDo, int position) {
        if (choDo == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin chỗ đỗ", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỗ đỗ số " + choDo.getId());

        String thongTin = "";

        try {
            switch (choDo.getTrangThai()) {
                case "TRONG":
                    thongTin = "Trạng thái: TRỐNG\n\n" +
                            "Bạn có thể cho xe vào chỗ này.";
                    builder.setPositiveButton("Cho xe vào", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            choXeVao(choDo);
                        }
                    });
                    break;

                case "DANG_DO":
                    try {
                        LichSuXe lichSu = lichSuXeDAO.layTheoChoDoDangDo(choDo.getId());

                        if (lichSu != null) {
                            // ✅ DÙNG REALTIME METHOD
                            int phut = lichSu.getThoiGianDoPhutRealtime();
                            long tien = cauHinhDAO.tinhTienDoXe(phut);

                            int gio = phut / 60;
                            int phutConLai = phut % 60;
                            String thoiGianDo = gio > 0 ? gio + "h " + phutConLai + "p" : phut + " phút";

                            thongTin = "Trạng thái: ĐANG ĐỖ\n" +
                                    "Biển số: " + lichSu.getBienSoXe() + "\n" +
                                    "Thời gian vào: " + lichSu.getThoiGianVaoString() + "\n" +
                                    "Thời gian đỗ: " + thoiGianDo + "\n" +
                                    "Phí dự kiến: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(tien) + " VNĐ";

                            if (lichSu.getGhiChu() != null && !lichSu.getGhiChu().isEmpty()) {
                                thongTin += "\nGhi chú: " + lichSu.getGhiChu();
                            }
                        } else {
                            thongTin = "Trạng thái: ĐANG ĐỖ\n" +
                                    "Biển số: " + choDo.getBienSoXe() + "\n" +
                                    "(Không tìm thấy chi tiết trong database)";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        thongTin = "Trạng thái: ĐANG ĐỖ\n" +
                                "Biển số: " + choDo.getBienSoXe() + "\n" +
                                "Lỗi: " + e.getMessage();
                    }

                    builder.setPositiveButton("Cho xe ra", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            choXeRa(choDo);
                        }
                    });
                    builder.setNeutralButton("Mở Barrier", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moBarrier(choDo.getId());
                        }
                    });
                    break;

                case "DA_DAT":
                    thongTin = "Trạng thái: ĐÃ ĐẶT\n\n" +
                            "Chỗ này đã được đặt trước.";
                    builder.setPositiveButton("Hủy đặt chỗ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            huyDatCho(choDo);
                        }
                    });
                    break;

                case "BAO_TRI":
                    thongTin = "Trạng thái: BẢO TRÌ\n\n" +
                            "Chỗ này đang được bảo trì.";
                    break;

                default:
                    thongTin = "Trạng thái: KHÔNG XÁC ĐỊNH";
                    break;
            }

            builder.setMessage(thongTin);
            builder.setNegativeButton("Đóng", null);
            builder.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Cho xe vào
     */
    private void choXeVao(ChoDo choDo) {
        try {
            String bienSo = "51A-" + (100 + (int)(Math.random() * 900)) + "." +
                    (10 + (int)(Math.random() * 90));

            int ketQua = choDoDAO.datDangDo(choDo.getId(), bienSo);

            if (ketQua > 0) {
                LichSuXe lichSu = new LichSuXe(choDo.getId(), bienSo, "OTO", "");
                long idLichSu = lichSuXeDAO.themXeVao(lichSu);

                if (idLichSu > 0) {
                    taiDuLieuTuDatabase();
                    choDoAdapter.capNhatDanhSach(danhSachChoDo);
                    hienThiThongTin();

                    Toast.makeText(this, "✅ Xe " + bienSo + " vào chỗ " + choDo.getId(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "❌ Lỗi: Không thể thêm vào lịch sử", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "❌ Lỗi: Không thể cập nhật chỗ đỗ", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Cho xe ra
     */
    private void choXeRa(ChoDo choDo) {
        try {
            LichSuXe lichSu = lichSuXeDAO.layTheoChoDoDangDo(choDo.getId());

            if (lichSu == null) {
                Toast.makeText(this, "❌ Lỗi: Không tìm thấy thông tin xe!\n" +
                        "Có thể dữ liệu bị lỗi.", Toast.LENGTH_LONG).show();

                choDoDAO.datTrong(choDo.getId());
                taiDuLieuTuDatabase();
                choDoAdapter.capNhatDanhSach(danhSachChoDo);
                hienThiThongTin();
                return;
            }

            long thoiGianRa = System.currentTimeMillis();
            int phut = (int) ((thoiGianRa - lichSu.getThoiGianVao()) / 1000 / 60);
            long tien = cauHinhDAO.tinhTienDoXe(phut);

            String thongTin = "Xe: " + lichSu.getBienSoXe() + "\n" +
                    "Thời gian đỗ: " + phut + " phút\n" +
                    "Tổng tiền: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(tien) + " VNĐ";

            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận cho xe ra")
                    .setMessage(thongTin)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String[] phuongThuc = {"Tiền mặt", "Chuyển khoản", "Thẻ"};
                        String[] phuongThucCode = {"TIEN_MAT", "CHUYEN_KHOAN", "THE"};

                        new AlertDialog.Builder(this)
                                .setTitle("Phương thức thanh toán")
                                .setItems(phuongThuc, (d, i) -> {
                                    int ketQua = lichSuXeDAO.capNhatXeRa(
                                            lichSu.getId(),
                                            thoiGianRa,
                                            phut,
                                            tien,
                                            phuongThucCode[i]
                                    );

                                    if (ketQua > 0) {
                                        choDoDAO.datTrong(choDo.getId());

                                        taiDuLieuTuDatabase();
                                        choDoAdapter.capNhatDanhSach(danhSachChoDo);
                                        hienThiThongTin();

                                        Toast.makeText(this,
                                                "✅ Xe " + lichSu.getBienSoXe() + " ra\nThu: " +
                                                        NumberFormat.getInstance(new Locale("vi", "VN")).format(tien) + " VNĐ (" + phuongThuc[i] + ")",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(this, "❌ Lỗi: Không thể cập nhật!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Hủy đặt chỗ
     */
    private void huyDatCho(ChoDo choDo) {
        choDoDAO.datTrong(choDo.getId());

        taiDuLieuTuDatabase();
        choDoAdapter.capNhatDanhSach(danhSachChoDo);
        hienThiThongTin();

        Toast.makeText(this, "✅ Đã hủy đặt chỗ " + choDo.getId(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Mở barrier
     */
    private void moBarrier(int choDo) {
        Toast.makeText(this,
                "🚧 Gửi lệnh mở Barrier chỗ " + choDo + "\n(Sẽ tích hợp MQTT sau)",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Bắt đầu cập nhật real-time
     */
    private void batDauCapNhatRealTime() {
        handler = new Handler();
        capNhatRunnable = new Runnable() {
            @Override
            public void run() {
                taiDuLieuTuDatabase();
                choDoAdapter.capNhatDanhSach(danhSachChoDo);
                hienThiThongTin();
                handler.postDelayed(this, 10000);
            }
        };

        handler.postDelayed(capNhatRunnable, 10000);
    }

    /**
     * Xử lý nút Back
     */
    private void xuLyNutBack() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // ========== THÊM XỬ LÝ DRAWER ==========
                // Nếu drawer đang mở, đóng lại
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                // =======================================

                // Nếu không, hiển thị dialog xác nhận
                new AlertDialog.Builder(DashboardActivity.this)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có muốn đăng xuất?")
                        .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(DashboardActivity.this, DangNhapActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        taiDuLieuTuDatabase();
        choDoAdapter.capNhatDanhSach(danhSachChoDo);
        hienThiThongTin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Dừng handler
        if (handler != null && capNhatRunnable != null) {
            handler.removeCallbacks(capNhatRunnable);
        }

        // ✅ CLEANUP FIREBASE
        if (firebaseHelper != null) {
            firebaseHelper.destroy();
            firebaseHelper = null;
        }
    }
}