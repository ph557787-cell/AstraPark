package com.astrapark.quanly;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.astrapark.quanly.firebase.FirebaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Firebase Control Activity - Điều khiển Firebase real-time
 * Version: 2.0 - Enhanced with timestamp display
 */
public class FirebaseControlActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseControl";

    // UI Components
    private TextView tvConnectionStatus;
    private TextView tvBarrierInStatus;
    private TextView tvBarrierOutStatus;
    private TextView tvBarrierInCommand;
    private TextView tvBarrierOutCommand;
    private TextView tvLastUpdate;
    private Button btnOpenBarrierIn;
    private Button btnCloseBarrierIn;
    private Button btnOpenBarrierOut;
    private Button btnCloseBarrierOut;
    private Button btnBack;

    // Firebase
    private FirebaseHelper firebaseHelper;

    // Date formatter
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_control);

        initViews();
        initFirebase();
        setupListeners();
    }

    /**
     * Khởi tạo views
     */
    private void initViews() {
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvBarrierInStatus = findViewById(R.id.tvBarrierInStatus);
        tvBarrierOutStatus = findViewById(R.id.tvBarrierOutStatus);
        btnOpenBarrierIn = findViewById(R.id.btnOpenBarrierIn);
        btnCloseBarrierIn = findViewById(R.id.btnCloseBarrierIn);
        btnOpenBarrierOut = findViewById(R.id.btnOpenBarrierOut);
        btnCloseBarrierOut = findViewById(R.id.btnCloseBarrierOut);
        btnBack = findViewById(R.id.btnBack);

        // Khởi tạo date formatter
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        // Set initial state
        tvConnectionStatus.setText("⏳ Đang kết nối Firebase...");
        tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        tvBarrierInStatus.setText("Trạng thái: Đang tải...");
        tvBarrierOutStatus.setText("Trạng thái: Đang tải...");
    }

    /**
     * Khởi tạo Firebase
     */
    private void initFirebase() {
        try {
            Log.d(TAG, "========== KHỞI TẠO FIREBASE ==========");

            firebaseHelper = new FirebaseHelper();

            // Lắng nghe thay đổi barrier
            firebaseHelper.setBarrierListener((barrier, status) -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "📊 Barrier update: " + barrier + " = " + status);

                    // Format status hiển thị
                    String displayStatus = formatStatus(status);
                    String emoji = getStatusEmoji(status);

                    if ("in".equals(barrier)) {
                        tvBarrierInStatus.setText(emoji + " Trạng thái: " + displayStatus);

                        // Đổi màu button theo trạng thái
                        updateButtonColors(status, btnOpenBarrierIn, btnCloseBarrierIn);

                    } else if ("out".equals(barrier)) {
                        tvBarrierOutStatus.setText(emoji + " Trạng thái: " + displayStatus);

                        // Đổi màu button theo trạng thái
                        updateButtonColors(status, btnOpenBarrierOut, btnCloseBarrierOut);
                    }
                });
            });

            // Kết nối thành công
            tvConnectionStatus.setText("✅ Đã kết nối Firebase");
            tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            Log.d(TAG, "✅ Firebase initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();

            tvConnectionStatus.setText("❌ Lỗi kết nối Firebase");
            tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            Toast.makeText(this, "Lỗi khởi tạo Firebase: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        // Barrier VÀO - MỞ
        btnOpenBarrierIn.setOnClickListener(v -> {
            Log.d(TAG, "🔓 User clicked: Mở barrier vào");
            firebaseHelper.openBarrierIn();

            Toast.makeText(this, "🔓 Đang mở barrier vào...", Toast.LENGTH_SHORT).show();

            // Hiệu ứng button (optional)
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });

        // Barrier VÀO - ĐÓNG
        btnCloseBarrierIn.setOnClickListener(v -> {
            Log.d(TAG, "🔒 User clicked: Đóng barrier vào");
            firebaseHelper.closeBarrierIn();

            Toast.makeText(this, "🔒 Đang đóng barrier vào...", Toast.LENGTH_SHORT).show();

            // Hiệu ứng button (optional)
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });

        // Barrier RA - MỞ
        btnOpenBarrierOut.setOnClickListener(v -> {
            Log.d(TAG, "🔓 User clicked: Mở barrier ra");
            firebaseHelper.openBarrierOut();

            Toast.makeText(this, "🔓 Đang mở barrier ra...", Toast.LENGTH_SHORT).show();

            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });

        // Barrier RA - ĐÓNG
        btnCloseBarrierOut.setOnClickListener(v -> {
            Log.d(TAG, "🔒 User clicked: Đóng barrier ra");
            firebaseHelper.closeBarrierOut();

            Toast.makeText(this, "🔒 Đang đóng barrier ra...", Toast.LENGTH_SHORT).show();

            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });

        // Nút Quay lại
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "⬅️ User clicked: Quay lại");
            finish();
        });
    }

    /**
     * Format status cho dễ đọc
     * ✅ CẬP NHẬT: Chuẩn hóa tất cả case
     */
    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "Không rõ";

        // Chuyển về chữ hoa để so sánh
        String statusUpper = status.toUpperCase().trim();

        switch (statusUpper) {
            case "OPEN":
            case "OPENED":
            case "OPENING":
                return "Đang mở";

            case "CLOSE":
            case "CLOSED":
            case "CLOSING":
                return "Đã đóng";

            case "UNKNOWN":
                return "Không rõ";

            default:
                // Hiển thị nguyên văn nếu không khớp
                return status;
        }
    }

    /**
     * Lấy emoji theo trạng thái
     */
    private String getStatusEmoji(String status) {
        if (status == null) return "❓";

        switch (status.toUpperCase()) {
            case "OPEN":
            case "OPENED":
            case "OPENING":
                return "🟢"; // Màu xanh - đang mở

            case "CLOSE":
            case "CLOSED":
            case "CLOSING":
                return "🔴"; // Màu đỏ - đã đóng

            default:
                return "⚪"; // Màu trắng - không rõ
        }
    }

    /**
     * Update màu button theo trạng thái
     */
    private void updateButtonColors(String status, Button btnOpen, Button btnClose) {
        if (status == null) return;

        switch (status.toUpperCase()) {
            case "OPEN":
            case "OPENED":
            case "OPENING":
                // Barrier đang mở → highlight nút ĐÓNG
                btnOpen.setAlpha(0.5f);
                btnClose.setAlpha(1.0f);
                break;

            case "CLOSE":
            case "CLOSED":
            case "CLOSING":
                // Barrier đang đóng → highlight nút MỞ
                btnOpen.setAlpha(1.0f);
                btnClose.setAlpha(0.5f);
                break;

            default:
                // Không rõ → cả 2 nút bình thường
                btnOpen.setAlpha(1.0f);
                btnClose.setAlpha(1.0f);
                break;
        }
    }

    /**
     * Format timestamp
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) return "Chưa cập nhật";

        try {
            Date date = new Date(timestamp);
            return dateFormatter.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting timestamp: " + e.getMessage());
            return "Lỗi định dạng";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔴 Activity destroyed");

        if (firebaseHelper != null) {
            firebaseHelper.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🟢 Activity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "🟡 Activity paused");
    }
}