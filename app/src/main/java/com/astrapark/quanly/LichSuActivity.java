package com.astrapark.quanly;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astrapark.quanly.adapter.LichSuXeAdapter;
import com.astrapark.quanly.database.LichSuXeDAO;
import com.astrapark.quanly.model.LichSuXe;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình lịch sử xe vào/ra
 */
public class LichSuActivity extends AppCompatActivity {

    // Khai báo view
    private ImageView btnBack, btnXuatBaoCao;
    private Button btnTatCa, btnHomNay, btnBayNgay, btnThangNay, btnTuyChon;
    private EditText edtTimKiem;
    private TextView txtSoLuot, txtTongDoanhThu;
    private RecyclerView recyclerViewLichSu;
    private LinearLayout layoutEmpty, layoutThongKe;

    // Adapter
    private LichSuXeAdapter adapter;

    // Danh sách lịch sử
    private List<LichSuXe> danhSachLichSu;
    private List<LichSuXe> danhSachGoc; // Dùng cho tìm kiếm

    // DAO
    private LichSuXeDAO lichSuXeDAO;

    // Handler cập nhật
    private Handler handler;
    private Runnable capNhatRunnable;

    // Biến lưu bộ lọc hiện tại
    private String boLocHienTai = "TAT_CA";
    private long tuNgay = 0;
    private long denNgay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su);

        // Ẩn status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Khởi tạo DAO
        lichSuXeDAO = new LichSuXeDAO(this);

        // Khởi tạo view
        khoiTaoView();

        // Tải dữ liệu
        taiTatCaLichSu();

        // Setup RecyclerView
        setupRecyclerView();

        // Cập nhật thống kê
        capNhatThongKe();

        // Thiết lập sự kiện
        thietLapSuKien();

        // Bắt đầu cập nhật real-time
        batDauCapNhatRealTime();

        // Xử lý nút Back
        xuLyNutBack();
    }

    /**
     * Khởi tạo view
     */
    private void khoiTaoView() {
        btnBack = findViewById(R.id.btnBack);
        btnXuatBaoCao = findViewById(R.id.btnXuatBaoCao);
        btnTatCa = findViewById(R.id.btnTatCa);
        btnHomNay = findViewById(R.id.btnHomNay);
        btnBayNgay = findViewById(R.id.btnBayNgay);
        btnThangNay = findViewById(R.id.btnThangNay);
        btnTuyChon = findViewById(R.id.btnTuyChon);
        edtTimKiem = findViewById(R.id.edtTimKiem);
        txtSoLuot = findViewById(R.id.txtSoLuot);
        txtTongDoanhThu = findViewById(R.id.txtTongDoanhThu);
        recyclerViewLichSu = findViewById(R.id.recyclerViewLichSu);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutThongKe = findViewById(R.id.layoutThongKe);
    }

    /**
     * Tải tất cả lịch sử từ database
     */
    private void taiTatCaLichSu() {
        danhSachGoc = lichSuXeDAO.layTatCaLichSu();
        danhSachLichSu = new ArrayList<>(danhSachGoc);
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewLichSu.setLayoutManager(layoutManager);

        adapter = new LichSuXeAdapter(danhSachLichSu, new LichSuXeAdapter.OnLichSuListener() {
            @Override
            public void onChiTietClick(LichSuXe lichSu, int position) {
                hienThiChiTiet(lichSu);
            }

            @Override
            public void onInHoaDonClick(LichSuXe lichSu, int position) {
                inHoaDon(lichSu);
            }
        });

        recyclerViewLichSu.setAdapter(adapter);

        kiemTraEmpty();
    }

    /**
     * Cập nhật thống kê
     */
    private void capNhatThongKe() {
        // Đếm số lượt
        int soLuot = danhSachLichSu.size();
        txtSoLuot.setText(String.valueOf(soLuot));

        // Tính tổng doanh thu
        long tongTien = 0;
        for (LichSuXe lichSu : danhSachLichSu) {
            if (lichSu.getTrangThai().equals("DA_RA")) {
                tongTien += lichSu.getTienPhi();
            }
        }

        // Format tiền
        if (tongTien >= 1000000) {
            txtTongDoanhThu.setText(String.format(Locale.getDefault(), "%.2fM", tongTien / 1000000.0));
        } else if (tongTien >= 1000) {
            txtTongDoanhThu.setText(String.format(Locale.getDefault(), "%.0fK", tongTien / 1000.0));
        } else {
            txtTongDoanhThu.setText(tongTien + "đ");
        }

        kiemTraEmpty();
    }

    /**
     * Kiểm tra empty state
     */
    private void kiemTraEmpty() {
        if (danhSachLichSu.isEmpty()) {
            recyclerViewLichSu.setVisibility(View.GONE);
            layoutThongKe.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewLichSu.setVisibility(View.VISIBLE);
            layoutThongKe.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Thiết lập sự kiện
     */
    private void thietLapSuKien() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Tất cả
        btnTatCa.setOnClickListener(v -> {
            boLocHienTai = "TAT_CA";
            capNhatButtonActive(btnTatCa);
            taiTatCaLichSu();
            adapter.capNhatDanhSach(danhSachLichSu);
            capNhatThongKe();
        });

        // Nút Hôm nay
        btnHomNay.setOnClickListener(v -> {
            boLocHienTai = "HOM_NAY";
            capNhatButtonActive(btnHomNay);
            locTheoKhoangThoiGian(layDauNgayHomNay(), layCuoiNgayHomNay());
        });

        // Nút 7 ngày
        btnBayNgay.setOnClickListener(v -> {
            boLocHienTai = "BAY_NGAY";
            capNhatButtonActive(btnBayNgay);

            Calendar cal = Calendar.getInstance();
            long denNgay = layCuoiNgayHomNay();
            cal.add(Calendar.DAY_OF_MONTH, -6);
            long tuNgay = layDauNgay(cal.getTimeInMillis());

            locTheoKhoangThoiGian(tuNgay, denNgay);
        });

        // Nút Tháng này
        btnThangNay.setOnClickListener(v -> {
            boLocHienTai = "THANG_NAY";
            capNhatButtonActive(btnThangNay);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            long tuNgay = layDauNgay(cal.getTimeInMillis());
            long denNgay = layCuoiNgayHomNay();

            locTheoKhoangThoiGian(tuNgay, denNgay);
        });

        // Nút Tùy chọn
        btnTuyChon.setOnClickListener(v -> {
            hienThiDialogChonNgay();
        });

        // Tìm kiếm
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                timKiem(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút Xuất báo cáo
        btnXuatBaoCao.setOnClickListener(v -> {
            xuatBaoCao();
        });
    }

    /**
     * Cập nhật button active
     */
    private void capNhatButtonActive(Button buttonActive) {
        // Reset tất cả về outline
        btnTatCa.setBackgroundResource(R.drawable.btn_outline_gold);
        btnTatCa.setTextColor(getColor(R.color.vang_gold));

        btnHomNay.setBackgroundResource(R.drawable.btn_outline_gold);
        btnHomNay.setTextColor(getColor(R.color.vang_gold));

        btnBayNgay.setBackgroundResource(R.drawable.btn_outline_gold);
        btnBayNgay.setTextColor(getColor(R.color.vang_gold));

        btnThangNay.setBackgroundResource(R.drawable.btn_outline_gold);
        btnThangNay.setTextColor(getColor(R.color.vang_gold));

        btnTuyChon.setBackgroundResource(R.drawable.btn_outline_gold);
        btnTuyChon.setTextColor(getColor(R.color.vang_gold));

        // Set button active
        buttonActive.setBackgroundResource(R.drawable.btn_luxury);
        buttonActive.setTextColor(getColor(R.color.den_sang_trong));
    }

    /**
     * Lọc theo khoảng thời gian
     */
    private void locTheoKhoangThoiGian(long tuNgay, long denNgay) {
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;

        danhSachLichSu = lichSuXeDAO.layTheoKhoangThoiGian(tuNgay, denNgay);
        adapter.capNhatDanhSach(danhSachLichSu);
        capNhatThongKe();
    }

    /**
     * Tìm kiếm theo biển số
     */
    private void timKiem(String keyword) {
        if (keyword.isEmpty()) {
            // Hiển thị lại danh sách gốc
            danhSachLichSu = new ArrayList<>(danhSachGoc);
        } else {
            // Tìm kiếm
            danhSachLichSu = new ArrayList<>();
            for (LichSuXe lichSu : danhSachGoc) {
                if (lichSu.getBienSoXe().toUpperCase().contains(keyword.toUpperCase())) {
                    danhSachLichSu.add(lichSu);
                }
            }
        }

        adapter.capNhatDanhSach(danhSachLichSu);
        capNhatThongKe();
    }

    /**
     * Hiển thị dialog chọn ngày tùy chọn
     */
    private void hienThiDialogChonNgay() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_chon_ngay, null);

        Button btnTuNgay = dialogView.findViewById(R.id.btnTuNgay);
        Button btnDenNgay = dialogView.findViewById(R.id.btnDenNgay);
        Button btnHuyBo = dialogView.findViewById(R.id.btnHuyBo);
        Button btnXacNhan = dialogView.findViewById(R.id.btnXacNhan);

        final long[] tuNgayChon = {layDauNgayHomNay()};
        final long[] denNgayChon = {layCuoiNgayHomNay()};

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnTuNgay.setText(sdf.format(new Date(tuNgayChon[0])));
        btnDenNgay.setText(sdf.format(new Date(denNgayChon[0])));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Chọn từ ngày
        btnTuNgay.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(tuNgayChon[0]);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        tuNgayChon[0] = layDauNgay(selected.getTimeInMillis());
                        btnTuNgay.setText(sdf.format(new Date(tuNgayChon[0])));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Chọn đến ngày
        btnDenNgay.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(denNgayChon[0]);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        denNgayChon[0] = layCuoiNgay(selected.getTimeInMillis());
                        btnDenNgay.setText(sdf.format(new Date(denNgayChon[0])));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Hủy
        btnHuyBo.setOnClickListener(v -> dialog.dismiss());

        // Xác nhận
        btnXacNhan.setOnClickListener(v -> {
            boLocHienTai = "TUY_CHON";
            capNhatButtonActive(btnTuyChon);
            locTheoKhoangThoiGian(tuNgayChon[0], denNgayChon[0]);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Hiển thị chi tiết lịch sử
     */
    private void hienThiChiTiet(LichSuXe lichSu) {
        String thongTin = "═══ CHI TIẾT LỊCH SỬ ═══\n\n";

        thongTin += "📋 Mã: #" + lichSu.getId() + "\n";
        thongTin += "🚗 Biển số: " + lichSu.getBienSoXe() + "\n";
        thongTin += "🅿️ Chỗ đỗ: " + lichSu.getChoDo() + "\n";
        thongTin += "📦 Loại xe: Ô tô\n\n";

        thongTin += "⏰ Thời gian vào:\n   " + lichSu.getThoiGianVaoString() + "\n\n";

        if (lichSu.getTrangThai().equals("DA_RA")) {
            thongTin += "⏰ Thời gian ra:\n   " + lichSu.getThoiGianRaString() + "\n\n";
            thongTin += "⌛ Thời gian đỗ: " + lichSu.getThoiGianDoString() +
                    " (" + lichSu.getThoiGianDoPhut() + " phút)\n\n";
            thongTin += "💰 Tiền: " + lichSu.getTienString() + "\n";
            thongTin += "💳 Phương thức: " + layTenPhuongThuc(lichSu.getPhuongThucThanhToan()) + "\n";
            thongTin += "✅ Trạng thái: Đã thanh toán\n";
        } else {
            thongTin += "⏱ Trạng thái: Đang đỗ\n";
            thongTin += "💰 Chưa thanh toán\n";
        }

        if (lichSu.getGhiChu() != null && !lichSu.getGhiChu().isEmpty()) {
            thongTin += "\n💬 Ghi chú: " + lichSu.getGhiChu();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết lịch sử")
                .setMessage(thongTin)
                .setPositiveButton("Đóng", null)
                .show();
    }

    /**
     * In hóa đơn
     */
    private void inHoaDon(LichSuXe lichSu) {
        if (!lichSu.getTrangThai().equals("DA_RA")) {
            Toast.makeText(this, "Xe chưa ra, không thể in hóa đơn!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo nội dung hóa đơn
        String hoaDon = "════════════════════════\n";
        hoaDon += "    ASTRAPARK PARKING\n";
        hoaDon += "     HÓA ĐƠN ĐỖ XE\n";
        hoaDon += "════════════════════════\n\n";

        hoaDon += "Mã HĐ: #" + lichSu.getId() + "\n";
        hoaDon += "Ngày: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(lichSu.getThoiGianRa())) + "\n\n";

        hoaDon += "Biển số: " + lichSu.getBienSoXe() + "\n";
        hoaDon += "Chỗ đỗ: " + lichSu.getChoDo() + "\n";
        hoaDon += "Loại xe: Ô tô\n\n";

        hoaDon += "Giờ vào: " + new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                .format(new Date(lichSu.getThoiGianVao())) + "\n";
        hoaDon += "Giờ ra:  " + new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                .format(new Date(lichSu.getThoiGianRa())) + "\n";
        hoaDon += "Thời gian: " + lichSu.getThoiGianDoString() + "\n\n";

        hoaDon += "────────────────────────\n";
        hoaDon += "TỔNG TIỀN: " + lichSu.getTienString() + "\n";
        hoaDon += "────────────────────────\n\n";

        hoaDon += "Phương thức: " + layTenPhuongThuc(lichSu.getPhuongThucThanhToan()) + "\n";
        hoaDon += "Trạng thái: Đã thanh toán\n\n";

        hoaDon += "════════════════════════\n";
        hoaDon += "  Cảm ơn quý khách!\n";
        hoaDon += "   Hẹn gặp lại!\n";
        hoaDon += "════════════════════════";

        // Hiển thị dialog với nội dung hóa đơn
        new AlertDialog.Builder(this)
                .setTitle("Hóa đơn #" + lichSu.getId())
                .setMessage(hoaDon)
                .setPositiveButton("In", (dialog, which) -> {
                    // TODO: Tích hợp in thật (Bluetooth printer, PDF...)
                    Toast.makeText(this, "Chức năng in đang phát triển", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * Xuất báo cáo
     */
    private void xuatBaoCao() {
        Toast.makeText(this, "Chức năng xuất báo cáo PDF/Excel\nSẽ làm ở phiên bản sau",
                Toast.LENGTH_SHORT).show();

        // TODO: Xuất PDF hoặc Excel
    }

    /**
     * Lấy tên phương thức thanh toán
     */
    private String layTenPhuongThuc(String code) {
        if (code == null || code.isEmpty()) return "Chưa thanh toán";

        switch (code) {
            case "TIEN_MAT": return "Tiền mặt";
            case "CHUYEN_KHOAN": return "Chuyển khoản";
            case "THE": return "Thẻ";
            default: return code;
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
                // Reload dữ liệu
                switch (boLocHienTai) {
                    case "TAT_CA":
                        taiTatCaLichSu();
                        break;
                    case "HOM_NAY":
                        locTheoKhoangThoiGian(layDauNgayHomNay(), layCuoiNgayHomNay());
                        break;
                    case "TUY_CHON":
                        if (tuNgay > 0 && denNgay > 0) {
                            locTheoKhoangThoiGian(tuNgay, denNgay);
                        }
                        break;
                }

                adapter.capNhatDanhSach(danhSachLichSu);
                capNhatThongKe();

                // Lặp lại sau 30 giây
                handler.postDelayed(this, 30000);
            }
        };

        handler.postDelayed(capNhatRunnable, 30000);
    }

    /**
     * Lấy timestamp đầu ngày
     */
    private long layDauNgay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Lấy timestamp cuối ngày
     */
    private long layCuoiNgay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    /**
     * Lấy timestamp đầu ngày hôm nay
     */
    private long layDauNgayHomNay() {
        return layDauNgay(System.currentTimeMillis());
    }

    /**
     * Lấy timestamp cuối ngày hôm nay
     */
    private long layCuoiNgayHomNay() {
        return layCuoiNgay(System.currentTimeMillis());
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
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && capNhatRunnable != null) {
            handler.removeCallbacks(capNhatRunnable);
        }
    }
}