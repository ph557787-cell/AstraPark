package com.astrapark.quanly;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrapark.quanly.adapter.ChoDoAdapter;
import com.astrapark.quanly.database.ChoDoDAO;
import com.astrapark.quanly.database.LichSuXeDAO;
import com.astrapark.quanly.database.CauHinhDAO;
import com.astrapark.quanly.firebase.FirebaseHelper;
import com.astrapark.quanly.model.ChoDo;
import com.astrapark.quanly.model.LichSuXe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity hiển thị sơ đồ chỗ đỗ REALTIME
 * VERSION 4.2 FINAL - SYNC FIREBASE ↔ DATABASE
 */
public class ChoDoRealtimeActivity extends AppCompatActivity {

    private static final String TAG = "ChoDoRealtime";

    // Views
    private ImageView btnBack;
    private TextView tvTotalCars;
    private TextView tvAvailableSlots;
    private TextView tvTotalSlots;
    private TextView tvLastUpdate;
    private RecyclerView gridChoDo;

    // Data
    private List<ChoDo> danhSachChoDo;
    private ChoDoAdapter adapter;

    // Firebase
    private FirebaseHelper firebaseHelper;

    // DAO
    private ChoDoDAO choDoDAO;
    private LichSuXeDAO lichSuXeDAO;
    private CauHinhDAO cauHinhDAO;

    // Dialogs
    private DialogNhapBienSo dialogNhapBienSo;
    private DialogThanhToan dialogThanhToan;

    // AUTO REFRESH
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 30000; // 30 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cho_do_realtime);

        Log.d(TAG, "========== ACTIVITY CREATED V4.2 ==========");

        initViews();
        initData();
        setupGrid();
        initFirebase();
        startAutoRefresh();
    }

    /**
     * Khởi tạo views
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalCars = findViewById(R.id.tvTotalCars);
        tvAvailableSlots = findViewById(R.id.tvAvailableSlots);
        tvTotalSlots = findViewById(R.id.tvTotalSlots);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        gridChoDo = findViewById(R.id.gridChoDo);

        btnBack.setOnClickListener(v -> finish());
        tvTotalSlots.setText("6");

        Log.d(TAG, "✅ Views initialized");
    }

    /**
     * Khởi tạo dữ liệu
     */
    private void initData() {
        choDoDAO = new ChoDoDAO(this);
        lichSuXeDAO = new LichSuXeDAO(this);
        cauHinhDAO = new CauHinhDAO(this);

        taiDuLieuTuDatabase();

        Log.d(TAG, "✅ Data initialized");

        updateStats();
    }

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

        Log.d(TAG, "✅ Loaded from database: " + danhSachChoDo.size() + " slots");
    }

    /**
     * Setup RecyclerView
     */
    private void setupGrid() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        gridChoDo.setLayoutManager(layoutManager);

        adapter = new ChoDoAdapter(danhSachChoDo, (choDo, position) -> {
            Log.d(TAG, String.format("🖱️ Click chỗ %d | %s",
                    choDo.getId(), choDo.getTrangThai()));

            if (choDo.getId() == 1) {
                showDetailDialog(choDo);
            } else {
                showManualControlDialog(choDo);
            }
        });

        gridChoDo.setAdapter(adapter);

        Log.d(TAG, "✅ Grid & Adapter setup");
    }

    /**
     * ✅ KHỞI TẠO FIREBASE
     */
    private void initFirebase() {
        try {
            Log.d(TAG, "========== INIT FIREBASE ==========");

            firebaseHelper = new FirebaseHelper();

            // ✅ Listener chỗ đỗ - NHẬN TIMESTAMP
            firebaseHelper.setChoDoListener((choDoId, trangThai, bienSo, thoiGianVao) -> {
                Log.d(TAG, String.format("⚡⚡⚡ [CALLBACK] Chỗ %d | %s | %s | Time: %d",
                        choDoId, trangThai, bienSo, thoiGianVao));

                try {
                    // ✅ SYNC VÀO DATABASE
                    syncFirebaseToDatabase(choDoId, trangThai, bienSo, thoiGianVao);

                    // Update data trong list
                    updateChoDo(choDoId, trangThai, bienSo, thoiGianVao);

                    // Update adapter
                    if (adapter != null) {
                        adapter.updateFromFirebase(choDoId, trangThai, bienSo, thoiGianVao);
                    } else {
                        Log.e(TAG, "❌ Adapter NULL!");
                    }

                    updateStats();
                    updateLastSyncTime();

                } catch (Exception e) {
                    Log.e(TAG, "❌ Error: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // ✅ Listener events
            firebaseHelper.setEventListener((slotNum, eventType, timestamp) -> {
                try {
                    Log.d(TAG, String.format("🔔 [EVENT] Chỗ %d - %s", slotNum, eventType));
                    handleEvent(slotNum, eventType, timestamp);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error: " + e.getMessage());
                }
            });

            Log.d(TAG, "✅ Firebase listeners started");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ SYNC FIREBASE → DATABASE
     */
    private void syncFirebaseToDatabase(int choDoId, String trangThai, String bienSo, long thoiGianVao) {
        try {
            Log.d(TAG, String.format("🔄 [SYNC → DB] Chỗ %d | %s | %s | Time: %d",
                    choDoId, trangThai, bienSo, thoiGianVao));

            trangThai = trangThai.toUpperCase();

            if ("DANG_DO".equals(trangThai) || "DANG DO".equals(trangThai)) {
                // ✅ XE VÀO: Kiểm tra đã có trong database chưa
                LichSuXe lichSuExist = lichSuXeDAO.layTheoChoDoDangDo(choDoId);

                if (lichSuExist == null) {
                    // ✅ Chưa có → Tạo mới với constructor 6 tham số
                    LichSuXe lichSu = new LichSuXe(
                            0,              // id (auto)
                            choDoId,        // chỗ đỗ
                            bienSo,         // biển số
                            "OTO",          // loại xe
                            "",             // ghi chú
                            "DANG_DO"       // trạng thái
                    );

                    long idLichSu = lichSuXeDAO.themXeVao(lichSu);

                    if (idLichSu > 0) {
                        Log.d(TAG, String.format("  ✅ Đã tạo lịch sử mới ID: %d", idLichSu));
                    } else {
                        Log.e(TAG, "  ❌ Lỗi tạo lịch sử");
                    }
                } else {
                    Log.d(TAG, "  ℹ️ Lịch sử đã tồn tại ID: " + lichSuExist.getId());
                }

                // Cập nhật bảng cho_do
                choDoDAO.datDangDo(choDoId, bienSo);

            } else if ("TRONG".equals(trangThai)) {
                // ✅ XE RA: Cập nhật lịch sử
                LichSuXe lichSu = lichSuXeDAO.layTheoChoDoDangDo(choDoId);

                if (lichSu != null) {
                    long thoiGianRa = System.currentTimeMillis();
                    int phut = (int) ((thoiGianRa - lichSu.getThoiGianVao()) / 60000);
                    long tien = cauHinhDAO.tinhTienDoXe(phut);

                    int ketQua = lichSuXeDAO.capNhatXeRa(
                            lichSu.getId(),
                            thoiGianRa,
                            phut,
                            tien,
                            "CHUA_THANH_TOAN"
                    );

                    if (ketQua > 0) {
                        Log.d(TAG, String.format("  ✅ Đã cập nhật lịch sử ID: %d (ra)", lichSu.getId()));
                    } else {
                        Log.e(TAG, "  ❌ Lỗi cập nhật lịch sử");
                    }
                }

                // Cập nhật bảng cho_do
                choDoDAO.datTrong(choDoId);
            }

            // Reload từ database
            taiDuLieuTuDatabase();

            Log.d(TAG, "  ✅ Sync hoàn tất");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error syncFirebaseToDatabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ UPDATE CHỖ ĐỖ TRONG LIST
     */
    private void updateChoDo(int choDoId, String trangThai, String bienSo, long thoiGianVaoFirebase) {
        Log.d(TAG, "========================================");
        Log.d(TAG, String.format("📝 UPDATE: Chỗ %d | %s | %s | Time: %d",
                choDoId, trangThai, bienSo, thoiGianVaoFirebase));

        try {
            if (danhSachChoDo == null) {
                Log.e(TAG, "❌ danhSachChoDo NULL");
                return;
            }

            boolean found = false;

            for (ChoDo cho : danhSachChoDo) {
                if (cho.getId() == choDoId) {
                    found = true;

                    String oldTrangThai = cho.getTrangThai();

                    if (trangThai != null) {
                        trangThai = trangThai.trim().toUpperCase();
                    }

                    cho.setTrangThai(trangThai);
                    cho.setBienSoXe(bienSo != null ? bienSo : "");

                    // ✅ Set timestamp
                    if ("DANG_DO".equalsIgnoreCase(trangThai) || "DANG DO".equalsIgnoreCase(trangThai)) {
                        if (thoiGianVaoFirebase > 0) {
                            cho.setThoiGianVao(thoiGianVaoFirebase);
                            Log.d(TAG, "  🕐 SET timestamp from Firebase: " + thoiGianVaoFirebase);
                        } else {
                            if (!"DANG_DO".equalsIgnoreCase(oldTrangThai)) {
                                cho.setThoiGianVao(System.currentTimeMillis());
                                Log.d(TAG, "  🕐 SET new timestamp: " + cho.getThoiGianVao());
                            } else {
                                Log.d(TAG, "  🕐 KEEP old timestamp: " + cho.getThoiGianVao());
                            }
                        }
                    } else {
                        cho.setThoiGianVao(0);
                        Log.d(TAG, "  🕐 CLEAR timestamp");
                    }

                    Log.d(TAG, String.format("  ✅ %s → %s", oldTrangThai, trangThai));

                    break;
                }
            }

            if (!found) {
                Log.e(TAG, "❌ Không tìm thấy chỗ " + choDoId);
            }

            Log.d(TAG, "========================================");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ UPDATE STATS
     */
    private void updateStats() {
        try {
            if (danhSachChoDo == null) return;

            int totalCars = 0;
            for (ChoDo cho : danhSachChoDo) {
                if ("DANG_DO".equalsIgnoreCase(cho.getTrangThai()) ||
                        "DANG DO".equalsIgnoreCase(cho.getTrangThai())) {
                    totalCars++;
                }
            }

            int availableSlots = 6 - totalCars;

            if (tvTotalCars != null) {
                tvTotalCars.setText(String.valueOf(totalCars));
            }
            if (tvAvailableSlots != null) {
                tvAvailableSlots.setText(String.valueOf(availableSlots));

                if (availableSlots == 0) {
                    tvAvailableSlots.setTextColor(Color.parseColor("#F44336"));
                } else if (availableSlots <= 2) {
                    tvAvailableSlots.setTextColor(Color.parseColor("#FF9800"));
                } else {
                    tvAvailableSlots.setTextColor(Color.parseColor("#4CAF50"));
                }
            }

            // ✅ Cập nhật lên Firebase
            if (firebaseHelper != null) {
                firebaseHelper.updateStatusOverview(totalCars, availableSlots);
            }

            Log.d(TAG, String.format("📊 Stats: %d/%d", totalCars, availableSlots));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updateStats: " + e.getMessage());
        }
    }

    /**
     * ✅ AUTO REFRESH
     */
    private void startAutoRefresh() {
        if (refreshHandler != null) {
            stopAutoRefresh();
        }

        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (adapter == null || danhSachChoDo == null) {
                    refreshHandler.postDelayed(this, REFRESH_INTERVAL);
                    return;
                }

                boolean coXeDangDo = false;

                for (int i = 0; i < danhSachChoDo.size(); i++) {
                    ChoDo choDo = danhSachChoDo.get(i);
                    String trangThai = choDo.getTrangThai();

                    if ("DANG_DO".equalsIgnoreCase(trangThai) || "DANG DO".equalsIgnoreCase(trangThai)) {
                        coXeDangDo = true;
                        adapter.notifyItemChanged(i);
                    }
                }

                if (coXeDangDo) {
                    updateLastSyncTime();
                }

                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };

        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        Log.d(TAG, "✅ Auto-refresh started");
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            Log.d(TAG, "⛔ Auto-refresh stopped");
        }
    }

    /**
     * ✅ XỬ LÝ EVENT
     */
    private void handleEvent(int slotNum, String eventType, long timestamp) {
        switch (eventType) {
            case "NHAP_BIEN_SO":
                showDialogNhapBienSo(slotNum);
                break;

            case "THANH_TOAN":
                showDialogThanhToan(slotNum);
                break;

            default:
                Log.w(TAG, "⚠️ Unknown event: " + eventType);
                break;
        }
    }

    private void showDialogNhapBienSo(int slotNum) {
        if (isFinishing() || isDestroyed()) return;

        Log.d(TAG, "📱 Show dialog nhập biển số - Chỗ " + slotNum);

        try {
            if (dialogNhapBienSo != null && dialogNhapBienSo.isShowing()) {
                dialogNhapBienSo.dismiss();
            }

            dialogNhapBienSo = new DialogNhapBienSo(
                    this,
                    slotNum,
                    firebaseHelper,
                    (slot, bienSo) -> {
                        Log.d(TAG, String.format("✅ Biển số: %s - Chỗ %d", bienSo, slot));
                        firebaseHelper.clearEvent(slot);
                        Toast.makeText(this, "✅ Xe " + bienSo + " đã vào chỗ " + slot,
                                Toast.LENGTH_SHORT).show();
                    }
            );

            dialogNhapBienSo.show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDialogThanhToan(int slotNum) {
        Log.d(TAG, "📱 Show dialog thanh toán - Chỗ " + slotNum);

        ChoDo choDo = null;
        for (ChoDo cho : danhSachChoDo) {
            if (cho.getId() == slotNum) {
                choDo = cho;
                break;
            }
        }

        if (choDo == null || choDo.getBienSoXe() == null || choDo.getBienSoXe().isEmpty()) {
            Toast.makeText(this, "❌ Không tìm thấy thông tin xe", Toast.LENGTH_SHORT).show();
            firebaseHelper.clearEvent(slotNum);
            return;
        }

        int thoiGianDoPhut = calculateParkingTime(choDo.getThoiGianVao());

        if (dialogThanhToan != null && dialogThanhToan.isShowing()) {
            dialogThanhToan.dismiss();
        }

        try {
            dialogThanhToan = new DialogThanhToan(
                    this,
                    slotNum,
                    choDo.getBienSoXe(),
                    thoiGianDoPhut,
                    firebaseHelper,
                    (slot, bienSo, tienPhi, phuongThuc) -> {
                        Log.d(TAG, String.format("✅ Thanh toán: %s - %,d VNĐ - %s",
                                bienSo, tienPhi, phuongThuc));
                        firebaseHelper.clearEvent(slot);
                    }
            );

            dialogThanhToan.show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDialogThanhToanManual(int slotNum, String bienSo, long thoiGianVao) {
        int thoiGianDoPhut = calculateParkingTime(thoiGianVao);

        if (dialogThanhToan != null && dialogThanhToan.isShowing()) {
            dialogThanhToan.dismiss();
        }

        dialogThanhToan = new DialogThanhToan(
                this,
                slotNum,
                bienSo,
                thoiGianDoPhut,
                firebaseHelper,
                (slot, bs, tienPhi, phuongThuc) -> {
                    Log.d(TAG, String.format("✅ Thanh toán manual: %,d VNĐ - %s",
                            tienPhi, phuongThuc));
                    firebaseHelper.clearChoDo(slot);
                    Toast.makeText(this, "✅ Xe " + bs + " đã thanh toán",
                            Toast.LENGTH_SHORT).show();
                }
        );

        dialogThanhToan.show();
    }

    private int calculateParkingTime(long thoiGianVao) {
        if (thoiGianVao == 0) return 0;
        long diff = System.currentTimeMillis() - thoiGianVao;
        return (int) (diff / 60000);
    }

    /**
     * ✅ DIALOG ĐIỀU KHIỂN MANUAL
     */
    private void showManualControlDialog(ChoDo choDo) {
        String title = "Điều khiển chỗ " + choDo.getId();
        String trangThai = choDo.getTrangThai();

        if (trangThai != null) {
            trangThai = trangThai.trim().toUpperCase();
        }

        boolean isDangDo = "DANG_DO".equalsIgnoreCase(trangThai) || "DANG DO".equalsIgnoreCase(trangThai);

        if (isDangDo) {
            String bienSo = choDo.getBienSoXe();
            long thoiGianVao = choDo.getThoiGianVao();
            int thoiGianDoPhut = calculateParkingTime(thoiGianVao);
            int tienPhi = thoiGianDoPhut * 5000 / 60;
            if (tienPhi < 5000) tienPhi = 5000;

            String message = String.format(
                    "🚗 Biển số: %s\n⏱️ Thời gian đỗ: %d phút\n💰 Phí dự kiến: %,d VNĐ\n\nBạn muốn:",
                    bienSo, thoiGianDoPhut, tienPhi
            );

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("💳 Thanh toán", (dialog, which) -> {
                        showDialogThanhToanManual(choDo.getId(), bienSo, thoiGianVao);
                    })
                    .setNegativeButton("🚪 Cho ra (không thanh toán)", (dialog, which) -> {
                        new AlertDialog.Builder(this)
                                .setTitle("⚠️ Xác nhận")
                                .setMessage("Xe sẽ ra KHÔNG thanh toán. Bạn chắc chắn?")
                                .setPositiveButton("Đồng ý", (d, w) -> {
                                    firebaseHelper.clearChoDo(choDo.getId());
                                    Toast.makeText(this, "✅ Xe đã ra", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    })
                    .setNeutralButton("Hủy", null)
                    .show();

        } else {
            showDialogThemXeManual(choDo.getId());
        }
    }

    /**
     * ✅ DIALOG THÊM XE MANUAL
     */
    private void showDialogThemXeManual(int choDoId) {
        Log.d(TAG, "📱 [DIALOG THÊM XE] Chỗ " + choDoId);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_them_xe, null);

        EditText edtBienSoXe = dialogView.findViewById(R.id.edtBienSoXe);
        Button btnHuyBo = dialogView.findViewById(R.id.btnHuyBo);
        Button btnXacNhan = dialogView.findViewById(R.id.btnXacNhan);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnHuyBo.setOnClickListener(v -> {
            Log.d(TAG, "❌ User hủy thêm xe");
            dialog.dismiss();
        });

        btnXacNhan.setOnClickListener(v -> {
            String bienSo = edtBienSoXe.getText().toString().trim().toUpperCase();

            if (bienSo.isEmpty()) {
                edtBienSoXe.setError("Vui lòng nhập biển số");
                edtBienSoXe.requestFocus();
                return;
            }

            Log.d(TAG, String.format("🚗 [THÊM XE] Chỗ %d | Biển: %s", choDoId, bienSo));

            // ✅ Lưu vào database trước
            themXeVaoDatabase(choDoId, bienSo);

            // ✅ Sau đó sync lên Firebase
            long thoiGianVao = System.currentTimeMillis();
            firebaseHelper.setChoDoDangDo(choDoId, bienSo, thoiGianVao);

            Toast.makeText(this, "✅ Đang thêm xe " + bienSo + "...",
                    Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * ✅ THÊM XE VÀO DATABASE
     */
    private void themXeVaoDatabase(int choDoId, String bienSo) {
        try {
            // ✅ Tạo lịch sử với constructor 6 tham số
            LichSuXe lichSu = new LichSuXe(
                    0,              // id (auto)
                    choDoId,        // chỗ đỗ
                    bienSo,         // biển số
                    "OTO",          // loại xe
                    "",             // ghi chú
                    "DANG_DO"       // trạng thái
            );

            long idLichSu = lichSuXeDAO.themXeVao(lichSu);

            if (idLichSu > 0) {
                Log.d(TAG, String.format("✅ [DATABASE] Đã thêm lịch sử ID: %d", idLichSu));

                // Cập nhật bảng cho_do
                choDoDAO.datDangDo(choDoId, bienSo);
                Log.d(TAG, String.format("✅ [DATABASE] Đã cập nhật chỗ %d = DANG_DO", choDoId));

                // Reload data
                taiDuLieuTuDatabase();

            } else {
                Log.e(TAG, "❌ [DATABASE] Lỗi thêm lịch sử");
                Toast.makeText(this, "❌ Lỗi lưu vào database", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error themXeVaoDatabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDetailDialog(ChoDo choDo) {
        String title = "Chỗ số " + choDo.getId() + " (Tự động)";
        String trangThai = choDo.getTrangThai();

        if (trangThai != null) {
            trangThai = trangThai.trim().toUpperCase();
        }

        String message;

        if ("TRONG".equalsIgnoreCase(trangThai)) {
            message = "✅ Trạng thái: TRỐNG\n\nChỗ này do cảm biến IR tự động quản lý.";
        } else if ("DANG_DO".equalsIgnoreCase(trangThai) || "DANG DO".equalsIgnoreCase(trangThai)) {
            message = "🚗 Trạng thái: ĐANG ĐỖ\n\n" +
                    "Biển số: " + choDo.getBienSoXe() + "\n" +
                    "Thời gian vào: " + formatTime(choDo.getThoiGianVao()) + "\n\n" +
                    "Chỗ này do cảm biến IR tự động quản lý.";
        } else {
            message = "❓ Trạng thái: " + trangThai;
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void updateLastSyncTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date());
        if (tvLastUpdate != null) {
            tvLastUpdate.setText("Cập nhật: " + time);
        }
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🟢 Activity resumed");

        taiDuLieuTuDatabase();

        if (adapter != null) {
            adapter.capNhatDanhSach(danhSachChoDo);
        }

        updateLastSyncTime();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "🟡 Activity paused");
        stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔴 Activity destroying...");

        stopAutoRefresh();

        try {
            if (dialogNhapBienSo != null && dialogNhapBienSo.isShowing()) {
                dialogNhapBienSo.dismiss();
            }
            dialogNhapBienSo = null;

            if (dialogThanhToan != null && dialogThanhToan.isShowing()) {
                dialogThanhToan.dismiss();
            }
            dialogThanhToan = null;

        } catch (Exception e) {
            Log.e(TAG, "Error dismissing dialogs: " + e.getMessage());
        }

        if (firebaseHelper != null) {
            firebaseHelper.destroy();
            firebaseHelper = null;
        }

        if (adapter != null) {
            adapter = null;
        }

        if (danhSachChoDo != null) {
            danhSachChoDo.clear();
            danhSachChoDo = null;
        }

        choDoDAO = null;

        if (lichSuXeDAO != null) {
            lichSuXeDAO.close();
            lichSuXeDAO = null;
        }

        cauHinhDAO = null;
        refreshHandler = null;
        refreshRunnable = null;

        Log.d(TAG, "✅ Activity destroyed");
    }
}