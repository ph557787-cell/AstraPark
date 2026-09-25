package com.astrapark.quanly;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.astrapark.quanly.database.CauHinhDAO;
import com.astrapark.quanly.database.LichSuXeDAO;
import com.astrapark.quanly.firebase.FirebaseHelper;
import com.astrapark.quanly.model.LichSuXe;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Dialog thanh toán
 * VERSION 3.0 - FIX: Update thay vì tạo mới
 */
public class DialogThanhToan extends Dialog {

    private static final String TAG = "DialogThanhToan";

    // Views
    private TextView tvTitle;
    private TextView tvSlotNumber;
    private TextView tvBienSo;
    private TextView tvThoiGianDo;
    private TextView tvTongTien;
    private RadioGroup rgPhuongThuc;
    private RadioButton rbTienMat;
    private RadioButton rbChuyenKhoan;
    private RadioButton rbThe;
    private Button btnHuy;
    private Button btnThanhToan;

    // Data
    private int slotNumber;
    private String bienSo;
    private int thoiGianDoPhut;
    private long thoiGianVao;
    private long tienPhi;
    private FirebaseHelper firebaseHelper;
    private CauHinhDAO cauHinhDAO;
    private LichSuXeDAO lichSuXeDAO;
    private OnThanhToanListener listener;

    /**
     * Interface callback khi thanh toán
     */
    public interface OnThanhToanListener {
        void onThanhToanSuccess(int slotNumber, String bienSo, long tienPhi, String phuongThuc);
    }

    /**
     * Constructor CŨ (giữ để tương thích ngược)
     */
    public DialogThanhToan(@NonNull Context context,
                           int slotNumber,
                           String bienSo,
                           int thoiGianDoPhut,
                           FirebaseHelper firebaseHelper,
                           OnThanhToanListener listener) {
        super(context);
        this.slotNumber = slotNumber;
        this.bienSo = bienSo;
        this.thoiGianDoPhut = thoiGianDoPhut;
        this.thoiGianVao = System.currentTimeMillis() - (thoiGianDoPhut * 60000L);
        this.firebaseHelper = firebaseHelper;
        this.listener = listener;
        this.cauHinhDAO = new CauHinhDAO(context);
        this.lichSuXeDAO = new LichSuXeDAO(context);
    }

    /**
     * ✅ Constructor MỚI (nhận thoiGianVao chính xác)
     */
    public DialogThanhToan(@NonNull Context context,
                           int slotNumber,
                           String bienSo,
                           long thoiGianVao,
                           FirebaseHelper firebaseHelper,
                           OnThanhToanListener listener) {
        super(context);
        this.slotNumber = slotNumber;
        this.bienSo = bienSo;
        this.thoiGianVao = thoiGianVao;

        // Tính thời gian đỗ từ timestamp
        long diff = System.currentTimeMillis() - thoiGianVao;
        this.thoiGianDoPhut = (int) (diff / 60000);

        this.firebaseHelper = firebaseHelper;
        this.listener = listener;
        this.cauHinhDAO = new CauHinhDAO(context);
        this.lichSuXeDAO = new LichSuXeDAO(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_thanh_toan);

        setDialogWidth();
        initViews();
        calculateFee();
        setupListeners();
    }

    /**
     * SET WIDTH CHO DIALOG
     */
    private void setDialogWidth() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            params.width = (int) (screenWidth * 0.9);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
            Log.d(TAG, "✅ Dialog width set to: " + params.width + "px");
        }
    }

    /**
     * Khởi tạo views
     */
    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSlotNumber = findViewById(R.id.tvSlotNumber);
        tvBienSo = findViewById(R.id.tvBienSo);
        tvThoiGianDo = findViewById(R.id.tvThoiGianDo);
        tvTongTien = findViewById(R.id.tvTongTien);
        rgPhuongThuc = findViewById(R.id.rgPhuongThuc);
        rbTienMat = findViewById(R.id.rbTienMat);
        rbChuyenKhoan = findViewById(R.id.rbChuyenKhoan);
        rbThe = findViewById(R.id.rbThe);
        btnHuy = findViewById(R.id.btnHuy);
        btnThanhToan = findViewById(R.id.btnThanhToan);

        tvSlotNumber.setText("Chỗ số " + slotNumber);
        tvBienSo.setText(bienSo);

        int gio = thoiGianDoPhut / 60;
        int phut = thoiGianDoPhut % 60;
        String thoiGianStr;

        if (gio > 0 && phut > 0) {
            thoiGianStr = gio + " giờ " + phut + " phút";
        } else if (gio > 0) {
            thoiGianStr = gio + " giờ";
        } else {
            thoiGianStr = phut + " phút";
        }

        tvThoiGianDo.setText(thoiGianStr);
        rbTienMat.setChecked(true);
        setCancelable(false);

        Log.d(TAG, String.format("📊 Thông tin: Chỗ %d - %s - %d phút",
                slotNumber, bienSo, thoiGianDoPhut));
    }

    /**
     * Tính tiền đỗ xe
     */
    private void calculateFee() {
        tienPhi = cauHinhDAO.tinhTienDoXe(thoiGianDoPhut);
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String tienStr = formatter.format(tienPhi) + " VNĐ";
        tvTongTien.setText(tienStr);
        Log.d(TAG, String.format("💰 Tính tiền: %d phút = %,d VNĐ",
                thoiGianDoPhut, tienPhi));
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        btnHuy.setOnClickListener(v -> {
            Log.d(TAG, "❌ User clicked HỦY thanh toán");
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("⚠️ Xác nhận hủy")
                    .setMessage("Bạn chắc chắn muốn hủy thanh toán?\nXe vẫn đang ở chỗ đỗ.")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        Toast.makeText(getContext(), "❌ Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .setNegativeButton("Tiếp tục thanh toán", null)
                    .show();
        });

        btnThanhToan.setOnClickListener(v -> {
            int selectedId = rgPhuongThuc.getCheckedRadioButtonId();
            String phuongThucMa = "";
            String phuongThucTen = "";

            if (selectedId == R.id.rbTienMat) {
                phuongThucMa = "TIEN_MAT";
                phuongThucTen = "Tiền mặt";
            } else if (selectedId == R.id.rbChuyenKhoan) {
                phuongThucMa = "CHUYEN_KHOAN";
                phuongThucTen = "Chuyển khoản";
            } else if (selectedId == R.id.rbThe) {
                phuongThucMa = "THE";
                phuongThucTen = "Thẻ";
            } else {
                Toast.makeText(getContext(),
                        "⚠️ Vui lòng chọn phương thức thanh toán",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, String.format("💳 Thanh toán: %s - %,d VNĐ - %s",
                    bienSo, tienPhi, phuongThucTen));

            confirmPayment(phuongThucMa, phuongThucTen);
        });

        rgPhuongThuc.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d(TAG, "📱 Changed payment method: " + checkedId);
        });
    }

    /**
     * XÁC NHẬN THANH TOÁN
     */
    private void confirmPayment(String phuongThucMa, String phuongThucTen) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String tienStr = formatter.format(tienPhi);

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("💳 Xác nhận thanh toán")
                .setMessage(String.format(
                        "Biển số: %s\n" +
                                "Số tiền: %s VNĐ\n" +
                                "Phương thức: %s\n\n" +
                                "Xác nhận thanh toán?",
                        bienSo, tienStr, phuongThucTen))
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    processPayment(phuongThucMa, phuongThucTen);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * ✅ XỬ LÝ THANH TOÁN (ĐÃ SỬA)
     */
    private void processPayment(String phuongThucMa, String phuongThucTen) {
        // ✅ Lưu/Update lịch sử
        boolean success = updateLichSu(phuongThucMa);

        if (!success) {
            Toast.makeText(getContext(),
                    "❌ Lỗi cập nhật lịch sử. Vui lòng thử lại!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Xóa xe khỏi chỗ đỗ
        if (firebaseHelper != null) {
            firebaseHelper.clearChoDo(slotNumber);
            Log.d(TAG, "🚗 Đã xóa xe khỏi chỗ " + slotNumber);
        }

        // Callback
        if (listener != null) {
            listener.onThanhToanSuccess(slotNumber, bienSo, tienPhi, phuongThucMa);
        }

        // Hiển thị toast
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        Toast.makeText(getContext(),
                String.format("✅ Thanh toán thành công!\n%s VNĐ (%s)",
                        formatter.format(tienPhi), phuongThucTen),
                Toast.LENGTH_LONG).show();

        dismiss();
    }

    /**
     * ✅ UPDATE LỊCH SỬ (THAY VÌ TẠO MỚI)
     */
    private boolean updateLichSu(String phuongThuc) {
        try {
            // ✅ Tìm bản ghi đã có
            LichSuXe lichSuExist = lichSuXeDAO.layTheoChoDoDangDo(slotNumber);

            if (lichSuExist != null) {
                // ✅ Đã có → UPDATE
                Log.d(TAG, String.format("📝 UPDATE lịch sử ID: %d", lichSuExist.getId()));

                int result = lichSuXeDAO.capNhatXeRa(
                        lichSuExist.getId(),
                        System.currentTimeMillis(),
                        thoiGianDoPhut,
                        tienPhi,
                        phuongThuc  // ✅ Phương thức từ user chọn
                );

                if (result > 0) {
                    Log.d(TAG, "✅ Đã update lịch sử ID: " + lichSuExist.getId());
                    return true;
                } else {
                    Log.e(TAG, "❌ Lỗi update lịch sử");
                    return false;
                }

            } else {
                // ✅ Chưa có → TẠO MỚI
                Log.d(TAG, "📝 Tạo mới lịch sử");

                LichSuXe lichSuMoi = new LichSuXe(
                        0,
                        slotNumber,
                        bienSo,
                        "OTO",
                        thoiGianVao,
                        System.currentTimeMillis(),
                        thoiGianDoPhut,
                        tienPhi,
                        phuongThuc,
                        "",
                        "DA_RA"
                );

                long id = lichSuXeDAO.themLichSu(lichSuMoi);

                if (id > 0) {
                    Log.d(TAG, "✅ Đã tạo lịch sử mới ID: " + id);
                    return true;
                } else {
                    Log.e(TAG, "❌ Lỗi tạo lịch sử mới");
                    return false;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updateLichSu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getContext(),
                "⚠️ Vui lòng thanh toán hoặc nhấn HỦY",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void show() {
        super.show();
        Log.d(TAG, "🟢 Dialog shown");
    }

    @Override
    public void dismiss() {
        Log.d(TAG, "🔴 Dialog dismissed");
        super.dismiss();
    }
}