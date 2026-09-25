package com.astrapark.quanly;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.astrapark.quanly.firebase.FirebaseHelper;

/**
 * Dialog nhập biển số xe
 * Hiển thị khi ESP32 phát hiện xe vào chỗ 1
 * VERSION 2.0 - ĐÃ FIX WIDTH
 */
public class DialogNhapBienSo extends Dialog {

    private static final String TAG = "DialogNhapBienSo";

    // Views
    private TextView tvTitle;
    private TextView tvSlotNumber;
    private EditText edtBienSo;
    private Button btnHuy;
    private Button btnXacNhan;

    // Data
    private int slotNumber;
    private FirebaseHelper firebaseHelper;
    private OnBienSoConfirmedListener listener;

    /**
     * Interface callback khi xác nhận biển số
     */
    public interface OnBienSoConfirmedListener {
        void onBienSoConfirmed(int slotNumber, String bienSo);
    }

    /**
     * Constructor
     */
    public DialogNhapBienSo(@NonNull Context context, int slotNumber,
                            FirebaseHelper firebaseHelper,
                            OnBienSoConfirmedListener listener) {
        super(context);
        this.slotNumber = slotNumber;
        this.firebaseHelper = firebaseHelper;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_nhap_bien_so);

        // ✅✅✅ SET WIDTH DIALOG (QUAN TRỌNG)
        setDialogWidth();

        initViews();
        setupListeners();
    }

    /**
     * ✅ SET WIDTH CHO DIALOG - ĐỂ HIỂN THỊ ĐẦY ĐỦ
     */
    private void setDialogWidth() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();

            // Cách 1: Set 90% màn hình (khuyến nghị)
            int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            params.width = (int) (screenWidth * 0.9); // 90% chiều rộng màn hình

            // Cách 2: Match parent (nếu muốn full width)
            // params.width = ViewGroup.LayoutParams.MATCH_PARENT;

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
        edtBienSo = findViewById(R.id.edtBienSo);
        btnHuy = findViewById(R.id.btnHuy);
        btnXacNhan = findViewById(R.id.btnXacNhan);

        // Set slot number
        tvSlotNumber.setText("Chỗ số " + slotNumber);

        // Focus vào EditText
        edtBienSo.requestFocus();

        // ✅ Hiển thị bàn phím tự động
        Window window = getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        // Không cho dismiss khi click bên ngoài
        setCancelable(false);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        // Nút HỦY
        btnHuy.setOnClickListener(v -> {
            Log.d(TAG, "User clicked HỦY");

            // Hủy → Xóa xe khỏi chỗ đỗ (vì không nhập biển số)
            if (firebaseHelper != null) {
                firebaseHelper.clearChoDo(slotNumber);
                Log.d(TAG, "Đã xóa xe khỏi chỗ " + slotNumber);
            }

            dismiss();
        });

        // Nút XÁC NHẬN
        btnXacNhan.setOnClickListener(v -> {
            String bienSo = edtBienSo.getText().toString().trim().toUpperCase();

            // Validate
            if (bienSo.isEmpty()) {
                edtBienSo.setError("Vui lòng nhập biển số");
                edtBienSo.requestFocus();
                Toast.makeText(getContext(),
                        "⚠️ Vui lòng nhập biển số xe",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (bienSo.length() < 5) {
                edtBienSo.setError("Biển số quá ngắn");
                edtBienSo.requestFocus();
                Toast.makeText(getContext(),
                        "⚠️ Biển số phải có ít nhất 5 ký tự",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Validate format biển số (tùy chọn)
            if (!isValidBienSo(bienSo)) {
                edtBienSo.setError("Biển số không hợp lệ");
                edtBienSo.requestFocus();
                Toast.makeText(getContext(),
                        "⚠️ Biển số không đúng định dạng",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Xác nhận
            Log.d(TAG, "✅ Xác nhận biển số: " + bienSo + " cho chỗ " + slotNumber);

            // Lưu vào Firebase
            if (firebaseHelper != null) {
                firebaseHelper.updateSlot1WithBienSo(bienSo);
            }

            // Callback
            if (listener != null) {
                listener.onBienSoConfirmed(slotNumber, bienSo);
            }

            Toast.makeText(getContext(),
                    "✅ Đã lưu biển số: " + bienSo,
                    Toast.LENGTH_SHORT).show();

            dismiss();
        });

        // Auto uppercase khi nhập
        edtBienSo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ✅ Xóa error khi user bắt đầu nhập
                if (s.length() > 0 && edtBienSo.getError() != null) {
                    edtBienSo.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.equals(input.toUpperCase())) {
                    edtBienSo.removeTextChangedListener(this);
                    edtBienSo.setText(input.toUpperCase());
                    edtBienSo.setSelection(edtBienSo.getText().length());
                    edtBienSo.addTextChangedListener(this);
                }
            }
        });
    }

    /**
     * ✅ VALIDATE FORMAT BIỂN SỐ (Tùy chọn)
     * Format Việt Nam: 29A-12345, 30H-123.45, 51D-111.11, etc.
     */
    private boolean isValidBienSo(String bienSo) {
        // Loại bỏ dấu - và . để kiểm tra
        String clean = bienSo.replace("-", "").replace(".", "");

        // Biển số phải có ít nhất 5 ký tự
        if (clean.length() < 5) {
            return false;
        }

        // Biển số phải chứa cả chữ và số
        boolean hasLetter = clean.matches(".*[A-Z].*");
        boolean hasDigit = clean.matches(".*\\d.*");

        return hasLetter && hasDigit;
    }

    @Override
    public void onBackPressed() {
        // Không cho dismiss bằng nút Back
        Toast.makeText(getContext(),
                "⚠️ Vui lòng nhập biển số hoặc nhấn HỦY",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void dismiss() {
        // ✅ Ẩn bàn phím trước khi dismiss
        Window window = getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        super.dismiss();
    }
}