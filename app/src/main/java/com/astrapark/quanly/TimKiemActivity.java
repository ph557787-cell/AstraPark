package com.astrapark.quanly;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrapark.quanly.adapter.TimKiemAdapter;
import com.astrapark.quanly.database.LichSuXeDAO;
import com.astrapark.quanly.model.LichSuXe;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Activity tìm kiếm và lọc xe
 */
public class TimKiemActivity extends AppCompatActivity {

    // Views
    private ImageView btnBack, btnReset, btnClearSearch, btnExport;
    private EditText edtTimKiem;
    private Chip chipTrangThai, chipThoiGian, chipThanhToan, chipSapXep;
    private RecyclerView recyclerKetQua;
    private TextView txtKetQua;
    private LinearLayout layoutEmpty, layoutActiveFilters;
    private View scrollActiveFilters;

    // Data
    private LichSuXeDAO lichSuXeDAO;
    private List<LichSuXe> danhSachGoc; // Dữ liệu gốc
    private List<LichSuXe> danhSachHienThi; // Dữ liệu sau khi filter
    private TimKiemAdapter adapter;

    // Filter states
    private String filterTrangThai = ""; // DANG_DO, DA_RA, ALL
    private String filterThoiGian = ""; // HOM_NAY, TUAN_NAY, THANG_NAY, TUY_CHON
    private String filterThanhToan = ""; // TIEN_MAT, CHUYEN_KHOAN, THE, ALL
    private String filterSapXep = "MOI_NHAT"; // MOI_NHAT, CU_NHAT, TIEN_NHIEU, TIEN_IT
    private long filterTuNgay = 0;
    private long filterDenNgay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tim_kiem);

        // Khởi tạo
        khoiTaoView();
        khoiTaoData();
        thietLapSuKien();

        // Load dữ liệu ban đầu
        loadData();
    }

    /**
     * Khởi tạo views
     */
    private void khoiTaoView() {
        btnBack = findViewById(R.id.btnBack);
        btnReset = findViewById(R.id.btnReset);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnExport = findViewById(R.id.btnExport);
        edtTimKiem = findViewById(R.id.edtTimKiem);
        chipTrangThai = findViewById(R.id.chipTrangThai);
        chipThoiGian = findViewById(R.id.chipThoiGian);
        chipThanhToan = findViewById(R.id.chipThanhToan);
        chipSapXep = findViewById(R.id.chipSapXep);
        recyclerKetQua = findViewById(R.id.recyclerKetQua);
        txtKetQua = findViewById(R.id.txtKetQua);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutActiveFilters = findViewById(R.id.layoutActiveFilters);
        scrollActiveFilters = findViewById(R.id.scrollActiveFilters);

        // Setup RecyclerView
        recyclerKetQua.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Khởi tạo data
     */
    private void khoiTaoData() {
        lichSuXeDAO = new LichSuXeDAO(this);
        danhSachGoc = new ArrayList<>();
        danhSachHienThi = new ArrayList<>();

        adapter = new TimKiemAdapter(danhSachHienThi, new TimKiemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LichSuXe lichSu) {
                hienThiChiTiet(lichSu);
            }
        });

        recyclerKetQua.setAdapter(adapter);
    }

    /**
     * Thiết lập sự kiện
     */
    private void thietLapSuKien() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Reset
        btnReset.setOnClickListener(v -> resetFilters());

        // Nút Clear Search
        btnClearSearch.setOnClickListener(v -> {
            edtTimKiem.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        // Nút Export
        btnExport.setOnClickListener(v -> exportKetQua());

        // Search bar
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiện/ẩn nút clear
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Tìm kiếm
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip Trạng thái
        chipTrangThai.setOnClickListener(v -> showDialogTrangThai());

        // Chip Thời gian
        chipThoiGian.setOnClickListener(v -> showDialogThoiGian());

        // Chip Thanh toán
        chipThanhToan.setOnClickListener(v -> showDialogThanhToan());

        // Chip Sắp xếp
        chipSapXep.setOnClickListener(v -> showDialogSapXep());
    }

    /**
     * Load dữ liệu từ database
     */
    private void loadData() {
        // Lấy tất cả lịch sử
        danhSachGoc = lichSuXeDAO.layTatCa();

        // Áp dụng filter
        applyFilters();
    }

    /**
     * Áp dụng tất cả filters
     */
    private void applyFilters() {
        danhSachHienThi.clear();

        String tuKhoa = edtTimKiem.getText().toString().toLowerCase().trim();

        for (LichSuXe xe : danhSachGoc) {
            boolean match = true;

            // Filter theo từ khóa
            if (!tuKhoa.isEmpty()) {
                if (!xe.getBienSoXe().toLowerCase().contains(tuKhoa)) {
                    match = false;
                }
            }

            // Filter theo trạng thái
            if (!filterTrangThai.isEmpty() && !filterTrangThai.equals("ALL")) {
                if (!xe.getTrangThai().equals(filterTrangThai)) {
                    match = false;
                }
            }

            // Filter theo thời gian
            if (!filterThoiGian.isEmpty()) {
                if (!matchThoiGian(xe)) {
                    match = false;
                }
            }

            // Filter theo phương thức thanh toán
            if (!filterThanhToan.isEmpty() && !filterThanhToan.equals("ALL")) {
                if (xe.getPhuongThucThanhToan() == null ||
                        !xe.getPhuongThucThanhToan().equals(filterThanhToan)) {
                    match = false;
                }
            }

            if (match) {
                danhSachHienThi.add(xe);
            }
        }

        // Sắp xếp
        sapXep();

        // Cập nhật UI
        capNhatUI();
    }

    /**
     * Kiểm tra match thời gian
     */
    private boolean matchThoiGian(LichSuXe xe) {
        long thoiGianVao = xe.getThoiGianVao();
        Calendar cal = Calendar.getInstance();

        switch (filterThoiGian) {
            case "HOM_NAY":
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                return thoiGianVao >= today.getTimeInMillis();

            case "TUAN_NAY":
                Calendar week = Calendar.getInstance();
                week.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                week.set(Calendar.HOUR_OF_DAY, 0);
                week.set(Calendar.MINUTE, 0);
                week.set(Calendar.SECOND, 0);
                return thoiGianVao >= week.getTimeInMillis();

            case "THANG_NAY":
                Calendar month = Calendar.getInstance();
                month.set(Calendar.DAY_OF_MONTH, 1);
                month.set(Calendar.HOUR_OF_DAY, 0);
                month.set(Calendar.MINUTE, 0);
                month.set(Calendar.SECOND, 0);
                return thoiGianVao >= month.getTimeInMillis();

            case "TUY_CHON":
                return thoiGianVao >= filterTuNgay && thoiGianVao <= filterDenNgay;

            default:
                return true;
        }
    }

    /**
     * Sắp xếp danh sách
     */
    private void sapXep() {
        switch (filterSapXep) {
            case "MOI_NHAT":
                Collections.sort(danhSachHienThi, new Comparator<LichSuXe>() {
                    @Override
                    public int compare(LichSuXe o1, LichSuXe o2) {
                        return Long.compare(o2.getThoiGianVao(), o1.getThoiGianVao());
                    }
                });
                break;

            case "CU_NHAT":
                Collections.sort(danhSachHienThi, new Comparator<LichSuXe>() {
                    @Override
                    public int compare(LichSuXe o1, LichSuXe o2) {
                        return Long.compare(o1.getThoiGianVao(), o2.getThoiGianVao());
                    }
                });
                break;

            case "TIEN_NHIEU":
                Collections.sort(danhSachHienThi, new Comparator<LichSuXe>() {
                    @Override
                    public int compare(LichSuXe o1, LichSuXe o2) {
                        return Long.compare(o2.getTienThanhToan(), o1.getTienThanhToan());
                    }
                });
                break;

            case "TIEN_IT":
                Collections.sort(danhSachHienThi, new Comparator<LichSuXe>() {
                    @Override
                    public int compare(LichSuXe o1, LichSuXe o2) {
                        return Long.compare(o1.getTienThanhToan(), o2.getTienThanhToan());
                    }
                });
                break;
        }
    }

    /**
     * Cập nhật UI
     */
    private void capNhatUI() {
        // Cập nhật số lượng
        int soLuong = danhSachHienThi.size();
        txtKetQua.setText("Tìm thấy " + soLuong + " kết quả");

        // Hiện/ẩn empty state
        if (soLuong == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerKetQua.setVisibility(View.GONE);
            btnExport.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerKetQua.setVisibility(View.VISIBLE);
            btnExport.setVisibility(View.VISIBLE);
        }

        // Cập nhật RecyclerView
        adapter.notifyDataSetChanged();

        // Cập nhật active filters
        capNhatActiveFilters();
    }

    /**
     * Cập nhật active filters chips
     */
    private void capNhatActiveFilters() {
        layoutActiveFilters.removeAllViews();
        boolean hasFilter = false;

        // Trạng thái
        if (!filterTrangThai.isEmpty() && !filterTrangThai.equals("ALL")) {
            addActiveFilterChip(getTenTrangThai(filterTrangThai), "trangthai");
            hasFilter = true;
        }

        // Thời gian
        if (!filterThoiGian.isEmpty()) {
            addActiveFilterChip(getTenThoiGian(filterThoiGian), "thoigian");
            hasFilter = true;
        }

        // Thanh toán
        if (!filterThanhToan.isEmpty() && !filterThanhToan.equals("ALL")) {
            addActiveFilterChip(getTenThanhToan(filterThanhToan), "thanhtoan");
            hasFilter = true;
        }

        scrollActiveFilters.setVisibility(hasFilter ? View.VISIBLE : View.GONE);
    }

    /**
     * Thêm active filter chip
     */
    private void addActiveFilterChip(String text, String type) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.vang_gold);
        chip.setTextColor(getResources().getColor(R.color.den_sang_trong));
        chip.setCloseIconTintResource(R.color.den_sang_trong);

        chip.setOnCloseIconClickListener(v -> {
            // Xóa filter
            switch (type) {
                case "trangthai":
                    filterTrangThai = "";
                    chipTrangThai.setChecked(false);
                    break;
                case "thoigian":
                    filterThoiGian = "";
                    chipThoiGian.setChecked(false);
                    break;
                case "thanhtoan":
                    filterThanhToan = "";
                    chipThanhToan.setChecked(false);
                    break;
            }
            applyFilters();
        });

        layoutActiveFilters.addView(chip);
    }

    // ========== DIALOGS ==========

    /**
     * Dialog lọc trạng thái
     */
    private void showDialogTrangThai() {
        String[] options = {"Tất cả", "Đang đỗ", "Đã ra"};
        String[] values = {"ALL", "DANG_DO", "DA_RA"};

        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(filterTrangThai)) {
                selected = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Lọc theo trạng thái")
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    filterTrangThai = values[which];
                    chipTrangThai.setChecked(!filterTrangThai.equals("ALL"));
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Dialog lọc thời gian
     */
    private void showDialogThoiGian() {
        String[] options = {"Hôm nay", "Tuần này", "Tháng này", "Tùy chọn..."};

        new AlertDialog.Builder(this)
                .setTitle("Lọc theo thời gian")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            filterThoiGian = "HOM_NAY";
                            chipThoiGian.setChecked(true);
                            applyFilters();
                            break;
                        case 1:
                            filterThoiGian = "TUAN_NAY";
                            chipThoiGian.setChecked(true);
                            applyFilters();
                            break;
                        case 2:
                            filterThoiGian = "THANG_NAY";
                            chipThoiGian.setChecked(true);
                            applyFilters();
                            break;
                        case 3:
                            showDialogTuyChonNgay();
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Dialog chọn ngày tùy chọn
     */
    private void showDialogTuyChonNgay() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_chon_ngay, null);

        TextView txtTuNgay = dialogView.findViewById(R.id.btnTuNgay);
        TextView txtDenNgay = dialogView.findViewById(R.id.btnDenNgay);

        final long[] tuNgay = {System.currentTimeMillis()};
        final long[] denNgay = {System.currentTimeMillis()};

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtTuNgay.setText(sdf.format(tuNgay[0]));
        txtDenNgay.setText(sdf.format(denNgay[0]));

        // Chọn từ ngày
        txtTuNgay.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(tuNgay[0]);

            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day, 0, 0, 0);
                tuNgay[0] = selected.getTimeInMillis();
                txtTuNgay.setText(sdf.format(tuNgay[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        // Chọn đến ngày
        txtDenNgay.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(denNgay[0]);

            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day, 23, 59, 59);
                denNgay[0] = selected.getTimeInMillis();
                txtDenNgay.setText(sdf.format(denNgay[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Chọn khoảng thời gian")
                .setView(dialogView)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    filterThoiGian = "TUY_CHON";
                    filterTuNgay = tuNgay[0];
                    filterDenNgay = denNgay[0];
                    chipThoiGian.setChecked(true);
                    applyFilters();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Dialog lọc thanh toán
     */
    private void showDialogThanhToan() {
        String[] options = {"Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ"};
        String[] values = {"ALL", "TIEN_MAT", "CHUYEN_KHOAN", "THE"};

        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(filterThanhToan)) {
                selected = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Lọc theo thanh toán")
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    filterThanhToan = values[which];
                    chipThanhToan.setChecked(!filterThanhToan.equals("ALL"));
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Dialog sắp xếp
     */
    private void showDialogSapXep() {
        String[] options = {"Mới nhất", "Cũ nhất", "Tiền nhiều → ít", "Tiền ít → nhiều"};
        String[] values = {"MOI_NHAT", "CU_NHAT", "TIEN_NHIEU", "TIEN_IT"};

        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(filterSapXep)) {
                selected = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Sắp xếp kết quả")
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    filterSapXep = values[which];
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Hiển thị chi tiết xe
     */
    private void hienThiChiTiet(LichSuXe lichSu) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

        String thongTin = "🚗 Biển số: " + lichSu.getBienSoXe() + "\n" +
                "📍 Chỗ đỗ: " + lichSu.getChoDo() + "\n" +
                "📅 Vào: " + sdf.format(lichSu.getThoiGianVao()) + "\n";

        if (lichSu.getTrangThai().equals("DA_RA")) {
            thongTin += "📅 Ra: " + sdf.format(lichSu.getThoiGianRa()) + "\n";
            thongTin += "⏱️ Thời gian: " + lichSu.getThoiGianDo() + " phút\n";
            thongTin += "💰 Tiền: " + String.format("%,d VNĐ", lichSu.getTienThanhToan()) + "\n";
            thongTin += "💳 Thanh toán: " + getTenThanhToan(lichSu.getPhuongThucThanhToan());
        } else {
            thongTin += "📌 Trạng thái: ĐANG ĐỖ";
        }

        if (lichSu.getGhiChu() != null && !lichSu.getGhiChu().isEmpty()) {
            thongTin += "\n📝 Ghi chú: " + lichSu.getGhiChu();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết")
                .setMessage(thongTin)
                .setPositiveButton("Đóng", null)
                .show();
    }

    /**
     * Reset tất cả filters
     */
    private void resetFilters() {
        edtTimKiem.setText("");
        filterTrangThai = "";
        filterThoiGian = "";
        filterThanhToan = "";
        filterSapXep = "MOI_NHAT";

        chipTrangThai.setChecked(false);
        chipThoiGian.setChecked(false);
        chipThanhToan.setChecked(false);
        chipSapXep.setChecked(false);

        applyFilters();

        Toast.makeText(this, "✅ Đã reset tất cả bộ lọc", Toast.LENGTH_SHORT).show();
    }

    /**
     * Export kết quả
     */
    private void exportKetQua() {
        if (danhSachHienThi.isEmpty()) {
            Toast.makeText(this, "⚠️ Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Excel (.xlsx)", "PDF (.pdf)", "CSV (.csv)"};

        new AlertDialog.Builder(this)
                .setTitle("Xuất kết quả tìm kiếm")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "📊 Xuất Excel - Đang phát triển", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(this, "📄 Xuất PDF - Đang phát triển", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(this, "📑 Xuất CSV - Đang phát triển", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ========== HELPER METHODS ==========

    private String getTenTrangThai(String trangThai) {
        switch (trangThai) {
            case "DANG_DO": return "Đang đỗ";
            case "DA_RA": return "Đã ra";
            default: return "Tất cả";
        }
    }

    private String getTenThoiGian(String thoiGian) {
        switch (thoiGian) {
            case "HOM_NAY": return "Hôm nay";
            case "TUAN_NAY": return "Tuần này";
            case "THANG_NAY": return "Tháng này";
            case "TUY_CHON":
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                return sdf.format(filterTuNgay) + " - " + sdf.format(filterDenNgay);
            default: return "";
        }
    }

    private String getTenThanhToan(String thanhToan) {
        if (thanhToan == null) return "Chưa thanh toán";
        switch (thanhToan) {
            case "TIEN_MAT": return "Tiền mặt";
            case "CHUYEN_KHOAN": return "Chuyển khoản";
            case "THE": return "Thẻ";
            default: return "Tất cả";
        }
    }
}