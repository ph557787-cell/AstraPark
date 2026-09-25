package com.astrapark.quanly;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.astrapark.quanly.adapter.XeDangDoAdapter;
import com.astrapark.quanly.model.XeDangDo;

// ===== DATABASE =====
import com.astrapark.quanly.database.ChoDoDAO;
import com.astrapark.quanly.database.LichSuXeDAO;
import com.astrapark.quanly.database.CauHinhDAO;
import com.astrapark.quanly.model.LichSuXe;
import com.astrapark.quanly.model.ChoDo;

// ===== FIREBASE =====
import com.astrapark.quanly.firebase.FirebaseHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình quản lý xe vào/ra
 * VERSION 3.0 - ĐÃ CẬP NHẬT:
 * - Chặn CHỖ 1 (do IR tự động quản lý)
 * - Tính tiền: 50k/giờ, miễn phí 15p đầu
 * - Sync Firebase realtime
 */
public class XeVaoRaActivity extends AppCompatActivity {

    private static final String TAG = "XeVaoRaActivity";

    // Khai báo view
    private ImageView btnBack, btnThemXe;
    private TextView txtTongXeDangDo, txtDoanhThuDuKien;
    private RecyclerView recyclerViewXeDangDo;
    private LinearLayout layoutEmpty;

    // Adapter
    private XeDangDoAdapter adapter;

    // Danh sách xe đang đỗ
    private List<XeDangDo> danhSachXeDangDo;

    // Handler cập nhật real-time
    private Handler handler;
    private Runnable capNhatRunnable;

    // ===== DAO =====
    private ChoDoDAO choDoDAO;
    private LichSuXeDAO lichSuXeDAO;
    private CauHinhDAO cauHinhDAO;

    // ===== FIREBASE =====
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xe_vao_ra);

        // Ẩn status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // ===== KHỞI TẠO DAO =====
        choDoDAO = new ChoDoDAO(this);
        lichSuXeDAO = new LichSuXeDAO(this);
        cauHinhDAO = new CauHinhDAO(this);

        // ===== KHỞI TẠO FIREBASE =====
        initFirebase();

        // Khởi tạo view
        khoiTaoView();

        // Tải dữ liệu từ database
        taiDuLieuTuDatabase();

        // Setup RecyclerView
        setupRecyclerView();

        // Hiển thị thông tin
        capNhatThongTin();

        // Thiết lập sự kiện
        thietLapSuKien();

        // Bắt đầu cập nhật real-time
        batDauCapNhatRealTime();

        // Xử lý nút Back
        xuLyNutBack();

        // ===== SYNC FIREBASE LẦN ĐẦU =====
        syncAllToFirebase();
    }

    /**
     * ✅ KHỞI TẠO FIREBASE - NHẬN TIMESTAMP
     */
    private void initFirebase() {
        try {
            Log.d(TAG, "========== KHỞI TẠO FIREBASE V3.0 ==========");

            firebaseHelper = new FirebaseHelper();

            // ✅ Lắng nghe thay đổi từ Firebase - NHẬN TIMESTAMP
            firebaseHelper.setChoDoListener((choDoId, trangThai, bienSo, thoiGianVao) -> {
                runOnUiThread(() -> {
                    Log.d(TAG, String.format("🔄 Firebase update: Chỗ %d = %s (%s) | Time: %d",
                            choDoId, trangThai, bienSo, thoiGianVao));

                    // ✅ RELOAD TỪ DATABASE (đã được sync bởi ChoDoRealtimeActivity)
                    taiDuLieuTuDatabase();
                    adapter.capNhatDanhSach(danhSachXeDangDo);
                    capNhatThongTin();
                });
            });

            // Lắng nghe thay đổi status
            firebaseHelper.setStatusListener((totalCars, availableSlots) -> {
                runOnUiThread(() -> {
                    Log.d(TAG, String.format("📊 Status update: %d xe, %d chỗ trống",
                            totalCars, availableSlots));
                });
            });

            Log.d(TAG, "✅ Firebase initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo Firebase: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Khởi tạo view
     */
    private void khoiTaoView() {
        btnBack = findViewById(R.id.btnBack);
        btnThemXe = findViewById(R.id.btnThemXe);
        txtTongXeDangDo = findViewById(R.id.txtTongXeDangDo);
        txtDoanhThuDuKien = findViewById(R.id.txtDoanhThuDuKien);
        recyclerViewXeDangDo = findViewById(R.id.recyclerViewXeDangDo);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    /**
     * Tải dữ liệu từ database
     */
    private void taiDuLieuTuDatabase() {
        danhSachXeDangDo = new ArrayList<>();

        // Lấy danh sách lịch sử xe đang đỗ từ database
        List<LichSuXe> danhSachLichSu = lichSuXeDAO.layXeDangDo();

        // Chuyển đổi từ LichSuXe sang XeDangDo
        for (LichSuXe lichSu : danhSachLichSu) {
            XeDangDo xe = new XeDangDo(
                    lichSu.getId(),
                    lichSu.getChoDo(),
                    lichSu.getBienSoXe(),
                    lichSu.getThoiGianVao(),
                    lichSu.getLoaiXe(),
                    lichSu.getGhiChu()
            );
            danhSachXeDangDo.add(xe);
        }
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewXeDangDo.setLayoutManager(layoutManager);

        adapter = new XeDangDoAdapter(danhSachXeDangDo, new XeDangDoAdapter.OnXeDangDoListener() {
            @Override
            public void onChiTietClick(XeDangDo xe, int position) {
                hienThiChiTiet(xe);
            }

            @Override
            public void onChoRaClick(XeDangDo xe, int position) {
                // ✅ KIỂM TRA: Chặn chỗ 1
                if (xe.getChoDo() == 1) {
                    new AlertDialog.Builder(XeVaoRaActivity.this)
                            .setTitle("⚠️ Chỗ 1 tự động")
                            .setMessage("Chỗ 1 do cảm biến IR tự động quản lý.\n\n" +
                                    "Vui lòng sử dụng màn hình 'Chỗ Đỗ Realtime' để xem chi tiết.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                hienThiDialogChoRa(xe, position);
            }
        });

        recyclerViewXeDangDo.setAdapter(adapter);

        // Hiển thị empty state nếu không có xe
        kiemTraEmpty();
    }

    /**
     * Cập nhật thông tin thống kê
     */
    private void capNhatThongTin() {
        // Tổng xe đang đỗ
        int tongXe = danhSachXeDangDo.size();
        txtTongXeDangDo.setText(String.valueOf(tongXe));

        // Tính doanh thu dự kiến
        long tongTien = 0;
        for (XeDangDo xe : danhSachXeDangDo) {
            // Tính thời gian đỗ
            int phut = (int) ((System.currentTimeMillis() - xe.getThoiGianVao()) / 1000 / 60);
            // ✅ Tính tiền từ CauHinhDAO (50k/giờ, 15p miễn phí)
            long tien = cauHinhDAO.tinhTienDoXe(phut);
            tongTien += tien;
        }

        // Format tiền
        if (tongTien >= 1000000) {
            txtDoanhThuDuKien.setText(String.format(Locale.getDefault(), "%.1fM", tongTien / 1000000.0));
        } else if (tongTien >= 1000) {
            txtDoanhThuDuKien.setText(String.format(Locale.getDefault(), "%.0fK", tongTien / 1000.0));
        } else {
            txtDoanhThuDuKien.setText(tongTien + "đ");
        }

        // Kiểm tra empty
        kiemTraEmpty();
    }

    /**
     * Kiểm tra và hiển thị empty state
     */
    private void kiemTraEmpty() {
        if (danhSachXeDangDo.isEmpty()) {
            recyclerViewXeDangDo.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewXeDangDo.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Thiết lập sự kiện
     */
    private void thietLapSuKien() {
        // Nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Nút Thêm xe
        btnThemXe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hienThiDialogThemXe();
            }
        });
    }

    /**
     * ✅ ĐÃ CẬP NHẬT: Hiển thị dialog thêm xe (CHỈ CHỖ 2-6)
     */
    private void hienThiDialogThemXe() {
        // Inflate layout dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_them_xe, null);

        // Ánh xạ view
        EditText edtBienSoXe = dialogView.findViewById(R.id.edtBienSoXe);
        Spinner spinnerChoDo = dialogView.findViewById(R.id.spinnerChoDo);
        EditText edtGhiChu = dialogView.findViewById(R.id.edtGhiChu);
        Button btnHuyBo = dialogView.findViewById(R.id.btnHuyBo);
        Button btnXacNhan = dialogView.findViewById(R.id.btnXacNhan);

        // ✅ LẤY DANH SÁCH CHỖ TRỐNG (LOẠI BỎ CHỖ 1)
        List<ChoDo> danhSachChoTrongAll = choDoDAO.layDanhSachChoTrong();
        List<ChoDo> danhSachChoTrong = new ArrayList<>();

        for (ChoDo cho : danhSachChoTrongAll) {
            if (cho.getId() != 1) {  // ✅ BỎ CHỖ 1
                danhSachChoTrong.add(cho);
            }
        }

        if (danhSachChoTrong.isEmpty()) {
            Toast.makeText(this,
                    "❌ Không còn chỗ trống!\n" +
                            "(Chỗ 1 do cảm biến IR tự động quản lý)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Setup Spinner (CHỈ CHỖ 2-6)
        List<String> danhSachHienThi = new ArrayList<>();
        for (ChoDo cho : danhSachChoTrong) {
            danhSachHienThi.add("Chỗ số " + cho.getId());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                danhSachHienThi
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChoDo.setAdapter(spinnerAdapter);

        // Tạo dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Sự kiện nút Hủy
        btnHuyBo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Sự kiện nút Xác nhận
        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bienSo = edtBienSoXe.getText().toString().trim().toUpperCase();
                String ghiChu = edtGhiChu.getText().toString().trim();

                // Validate
                if (bienSo.isEmpty()) {
                    edtBienSoXe.setError("Vui lòng nhập biển số");
                    edtBienSoXe.requestFocus();
                    return;
                }

                // Lấy chỗ đỗ đã chọn
                int viTriChon = spinnerChoDo.getSelectedItemPosition();
                ChoDo choDoChon = danhSachChoTrong.get(viTriChon);

                // Thêm xe vào database
                themXeVao(choDoChon.getId(), bienSo, ghiChu);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * ✅ THÊM XE VÀO BÃI - LƯU DATABASE + SYNC FIREBASE
     */
    private void themXeVao(int choDo, String bienSo, String ghiChu) {
        if (choDo == 1) {
            Toast.makeText(this, "❌ Chỗ 1 do cảm biến tự động quản lý!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo lịch sử mới
        LichSuXe lichSu = new LichSuXe(choDo, bienSo, "OTO", ghiChu);
        long thoiGianVao = System.currentTimeMillis();

        // Thêm vào database
        long idLichSu = lichSuXeDAO.themXeVao(lichSu);

        if (idLichSu > 0) {
            // Cập nhật trạng thái chỗ đỗ
            choDoDAO.datDangDo(choDo, bienSo);

            // ✅ SYNC LÊN FIREBASE VỚI TIMESTAMP
            firebaseHelper.setChoDoDangDo(choDo, bienSo, thoiGianVao);

            // Update status overview
            int tongXe = danhSachXeDangDo.size() + 1;
            int choTrong = 6 - tongXe;
            firebaseHelper.updateStatusOverview(tongXe, choTrong);

            Log.d(TAG, String.format("🔥 Synced to Firebase: Chỗ %d = dang_do (%s) | Time: %d",
                    choDo, bienSo, thoiGianVao));

            // Reload dữ liệu
            taiDuLieuTuDatabase();
            adapter.capNhatDanhSach(danhSachXeDangDo);
            capNhatThongTin();

            Toast.makeText(this, "✅ Đã thêm xe " + bienSo + " vào chỗ " + choDo,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Lỗi! Không thể thêm xe", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị chi tiết xe
     */
    private void hienThiChiTiet(XeDangDo xe) {
        try {
            // ✅ Tính tiền từ CauHinhDAO (50k/giờ, 15p miễn phí)
            int phut = (int) ((System.currentTimeMillis() - xe.getThoiGianVao()) / 1000 / 60);
            long tien = cauHinhDAO.tinhTienDoXe(phut);

            int gio = phut / 60;
            int phutConLai = phut % 60;
            String thoiGianDo = gio > 0 ? gio + "h " + phutConLai + "p" : phut + " phút";

            String thongTin = "THÔNG TIN CHI TIẾT\n\n" +
                    "Chỗ đỗ: " + xe.getChoDo() +
                    (xe.getChoDo() == 1 ? " (Tự động - IR)" : " (Thủ công)") + "\n" +
                    "Biển số: " + xe.getBienSoXe() + "\n" +
                    "Loại xe: Ô tô\n" +
                    "Thời gian vào: " + xe.getThoiGianVaoString() + "\n" +
                    "Thời gian đỗ: " + thoiGianDo + " (" + phut + " phút)\n" +
                    "Tiền dự kiến: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(tien) + " VNĐ";

            if (xe.getGhiChu() != null && !xe.getGhiChu().isEmpty()) {
                thongTin += "\nGhi chú: " + xe.getGhiChu();
            }

            // ✅ Hiển thị cấu hình giá
            thongTin += "\n\n[Cấu hình giá]\n" +
                    "- Giá: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(cauHinhDAO.layGiaOtoGio()) + " VNĐ/giờ\n" +
                    "- Miễn phí: " + cauHinhDAO.layThoiGianMienPhi() + " phút đầu";

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Chi tiết xe")
                    .setMessage(thongTin)
                    .setPositiveButton("Đóng", null);

            // ✅ CHỈ cho mở barrier với CHỖ 2-6
            if (xe.getChoDo() != 1) {
                builder.setNeutralButton("Mở Barrier", (dialog, which) -> {
                    moBarrier(xe.getChoDo());
                });
            }

            builder.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * ✅ ĐÃ CẬP NHẬT: Hiển thị dialog cho xe ra (CHẶN CHỖ 1)
     */
    private void hienThiDialogChoRa(XeDangDo xe, int position) {
        try {
            // ✅ CHẶN CHỖ 1
            if (xe.getChoDo() == 1) {
                new AlertDialog.Builder(this)
                        .setTitle("⚠️ Chỗ 1 tự động")
                        .setMessage("Chỗ 1 do cảm biến IR tự động quản lý.\n\n" +
                                "Xe sẽ tự động ra khi cảm biến phát hiện.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // ✅ Tính tiền từ CauHinhDAO (50k/giờ, 15p miễn phí)
            int phut = (int) ((System.currentTimeMillis() - xe.getThoiGianVao()) / 1000 / 60);
            long tien = cauHinhDAO.tinhTienDoXe(phut);
            String tienFormat = NumberFormat.getInstance(new Locale("vi", "VN")).format(tien);

            int gio = phut / 60;
            int phutConLai = phut % 60;
            String thoiGianDo = gio > 0 ? gio + "h " + phutConLai + "p" : phut + " phút";

            String thongTin = "Xe: " + xe.getBienSoXe() + "\n" +
                    "Chỗ đỗ: " + xe.getChoDo() + "\n" +
                    "Thời gian đỗ: " + thoiGianDo + " (" + phut + " phút)\n\n" +
                    "Tổng tiền: " + tienFormat + " VNĐ";

            // ✅ Hiển thị chi tiết tính tiền nếu > 15 phút
            if (phut > cauHinhDAO.layThoiGianMienPhi()) {
                int phutTinhTien = phut - cauHinhDAO.layThoiGianMienPhi();
                thongTin += "\n\n(Miễn phí " + cauHinhDAO.layThoiGianMienPhi() +
                        " phút, tính " + phutTinhTien + " phút)";
            } else {
                thongTin += "\n\n(Miễn phí " + cauHinhDAO.layThoiGianMienPhi() + " phút)";
            }

            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận cho xe ra")
                    .setMessage(thongTin)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        choXeRa(xe, position, phut, tien);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Cho xe ra - CẬP NHẬT DATABASE + FIREBASE
     */
    private void choXeRa(XeDangDo xe, int position, int phut, long tien) {
        // ✅ DOUBLE CHECK: Không cho xử lý chỗ 1
        if (xe.getChoDo() == 1) {
            Toast.makeText(this, "❌ Chỗ 1 do cảm biến tự động quản lý!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog chọn phương thức thanh toán
        String[] phuongThuc = {"Tiền mặt", "Chuyển khoản", "Thẻ"};
        String[] phuongThucCode = {"TIEN_MAT", "CHUYEN_KHOAN", "THE"};

        new AlertDialog.Builder(this)
                .setTitle("Phương thức thanh toán")
                .setItems(phuongThuc, (dialog, which) -> {
                    String phuongThucChon = phuongThuc[which];
                    String phuongThucMa = phuongThucCode[which];

                    // Cập nhật lịch sử trong database
                    long thoiGianRa = System.currentTimeMillis();
                    int ketQua = lichSuXeDAO.capNhatXeRa(
                            xe.getId(),
                            thoiGianRa,
                            phut,
                            tien,
                            phuongThucMa
                    );

                    if (ketQua > 0) {
                        // Cập nhật chỗ đỗ về trống
                        choDoDAO.datTrong(xe.getChoDo());

                        // ===== SYNC LÊN FIREBASE =====
                        firebaseHelper.clearChoDo(xe.getChoDo());

                        // Update status overview
                        int tongXe = danhSachXeDangDo.size() - 1;
                        int choTrong = 6 - tongXe;
                        firebaseHelper.updateStatusOverview(tongXe, choTrong);

                        Log.d(TAG, String.format("🔥 Synced to Firebase: Chỗ %d = trong", xe.getChoDo()));

                        // Xóa xe khỏi danh sách
                        adapter.xoaXe(position);

                        // Cập nhật thông tin
                        capNhatThongTin();

                        // Hiển thị thông báo
                        String tienFormat = NumberFormat.getInstance(new Locale("vi", "VN")).format(tien);
                        Toast.makeText(this,
                                "✅ Xe " + xe.getBienSoXe() + " đã ra\n" +
                                        "Thu " + tienFormat + " VNĐ (" + phuongThucChon + ")",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "❌ Lỗi! Không thể cập nhật", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Mở barrier
     */
    private void moBarrier(int choDo) {
        // ===== GỬI LỆNH FIREBASE =====
        firebaseHelper.openBarrierOut();

        Toast.makeText(this,
                "🔓 Đã gửi lệnh mở barrier cho chỗ " + choDo,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Sync tất cả chỗ đỗ lên Firebase (lần đầu)
     */
    private void syncAllToFirebase() {
        try {
            List<ChoDo> allChoDo = choDoDAO.layTatCa();

            for (ChoDo cho : allChoDo) {
                firebaseHelper.updateChoDo(
                        cho.getId(),
                        cho.getTrangThai(),
                        cho.getBienSoXe()
                );
            }

            // Sync status
            int tongXe = danhSachXeDangDo.size();
            int choTrong = 6 - tongXe;
            firebaseHelper.updateStatusOverview(tongXe, choTrong);

            Log.d(TAG, "✅ Synced all data to Firebase");

        } catch (Exception e) {
            Log.e(TAG, "Error syncing to Firebase: " + e.getMessage());
        }
    }

    /**
     * Bắt đầu cập nhật real-time
     */
    private void batDauCapNhatRealTime() {
        handler = new Handler();
        capNhatRunnable = new Runnable() {
            @Override
            public void run() {
                // Reload dữ liệu từ database
                taiDuLieuTuDatabase();

                // Cập nhật adapter
                adapter.capNhatDanhSach(danhSachXeDangDo);

                // Cập nhật thống kê
                capNhatThongTin();

                // Lặp lại sau 10 giây
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
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🟢 Activity resumed");

        // Reload dữ liệu khi quay lại
        taiDuLieuTuDatabase();
        adapter.capNhatDanhSach(danhSachXeDangDo);
        capNhatThongTin();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "🟡 Activity paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔴 Activity destroyed");

        // Dừng cập nhật
        if (handler != null && capNhatRunnable != null) {
            handler.removeCallbacks(capNhatRunnable);
        }

        // Cleanup Firebase
        if (firebaseHelper != null) {
            firebaseHelper.destroy();
        }
    }
}