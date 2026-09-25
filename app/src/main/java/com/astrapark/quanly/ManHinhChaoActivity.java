package com.astrapark.quanly;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ManHinhChaoActivity extends AppCompatActivity {

    // Khai báo các view
    private RelativeLayout layoutChinh;
    private LinearLayout layoutNoiDung;
    private ImageView imgLogo, imgCarIcon;
    private TextView txtTenApp, txtSlogan, txtPhienBan;
    private ProgressBar progressBar;

    // Thời gian hiển thị màn hình chào (3 giây)
    private static final int THOI_GIAN_HIEN_THI = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_hinh_chao);

        // Ẩn thanh status bar để full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Khởi tạo các view
        khoiTaoView();

        // Chạy animation
        chayHieuUng();

        // Chuyển sang màn hình đăng nhập sau 3 giây
        chuyenManHinh();
    }

    /**
     * Khởi tạo các view từ layout
     */
    private void khoiTaoView() {
        layoutChinh = findViewById(R.id.layoutChinh);
        layoutNoiDung = findViewById(R.id.layoutNoiDung);
        imgLogo = findViewById(R.id.imgLogo);
        imgCarIcon = findViewById(R.id.imgCarIcon);
        txtTenApp = findViewById(R.id.txtTenApp);
        txtSlogan = findViewById(R.id.txtSlogan);
        txtPhienBan = findViewById(R.id.txtPhienBan);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Chạy hiệu ứng animation cho các phần tử
     */
    private void chayHieuUng() {
        // Animation fade in cho toàn bộ layout
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        layoutChinh.startAnimation(fadeIn);

        // Animation slide up cho nội dung
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        layoutNoiDung.startAnimation(slideUp);

        // Hiệu ứng xoay nhẹ cho logo (tạo bằng code)
        imgLogo.setAlpha(0f);
        imgLogo.setScaleX(0.5f);
        imgLogo.setScaleY(0.5f);
        imgLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1500)
                .setStartDelay(200)
                .start();

        // Hiệu ứng cho icon xe
        imgCarIcon.setAlpha(0f);
        imgCarIcon.setTranslationY(50f);
        imgCarIcon.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(800)
                .start();
    }

    /**
     * Chuyển sang màn hình đăng nhập sau thời gian delay
     */
    private void chuyenManHinh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Tạo intent chuyển sang màn hình đăng nhập
                Intent intent = new Intent(ManHinhChaoActivity.this, DangNhapActivity.class);
                startActivity(intent);

                // Thêm hiệu ứng chuyển màn hình mượt mà
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                // Kết thúc activity này
                finish();
            }
        }, THOI_GIAN_HIEN_THI);
    }
}