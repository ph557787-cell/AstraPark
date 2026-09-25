package com.astrapark.quanly;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.astrapark.quanly.database.LichSuXeDAO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Màn hình thống kê & báo cáo
 */
public class ThongKeActivity extends AppCompatActivity {

    // Khai báo view
    private ImageView btnBack, btnXuatBaoCao, imgTrend;
    private Button btnNgay, btnTuan, btnThang, btnNam;
    private BarChart chartDoanhThu;
    private LineChart chartLuotXe;
    private PieChart chartPhuongThuc;
    private TextView txtTongDoanhThu, txtTongLuotXe, txtTrungBinhNgay, txtSoSanh;
    private Button btnXuatPDF, btnXuatExcel;

    // DAO
    private LichSuXeDAO lichSuXeDAO;

    // Biến lưu bộ lọc hiện tại
    private String boLocHienTai = "NGAY";

    // Màu sắc cho biểu đồ
    private int mauVangGold;
    private int mauTrang;
    private int mauXam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);

        // Ẩn status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Khởi tạo màu sắc
        mauVangGold = ContextCompat.getColor(this, R.color.vang_gold);
        mauTrang = ContextCompat.getColor(this, R.color.trang_tinh_khiet);
        mauXam = ContextCompat.getColor(this, R.color.xam_platinum);

        // Khởi tạo DAO
        lichSuXeDAO = new LichSuXeDAO(this);

        // Khởi tạo view
        khoiTaoView();

        // Thiết lập sự kiện
        thietLapSuKien();

        // Load dữ liệu mặc định (theo ngày)
        loadThongKeTheoNgay();

        // Xử lý nút Back
        xuLyNutBack();
    }

    /**
     * Khởi tạo view
     */
    private void khoiTaoView() {
        btnBack = findViewById(R.id.btnBack);
        btnXuatBaoCao = findViewById(R.id.btnXuatBaoCao);
        btnNgay = findViewById(R.id.btnNgay);
        btnTuan = findViewById(R.id.btnTuan);
        btnThang = findViewById(R.id.btnThang);
        btnNam = findViewById(R.id.btnNam);
        chartDoanhThu = findViewById(R.id.chartDoanhThu);
        chartLuotXe = findViewById(R.id.chartLuotXe);
        chartPhuongThuc = findViewById(R.id.chartPhuongThuc);
        txtTongDoanhThu = findViewById(R.id.txtTongDoanhThu);
        txtTongLuotXe = findViewById(R.id.txtTongLuotXe);
        txtTrungBinhNgay = findViewById(R.id.txtTrungBinhNgay);
        txtSoSanh = findViewById(R.id.txtSoSanh);
        imgTrend = findViewById(R.id.imgTrend);
        btnXuatPDF = findViewById(R.id.btnXuatPDF);
        btnXuatExcel = findViewById(R.id.btnXuatExcel);
    }

    /**
     * Thiết lập sự kiện
     */
    private void thietLapSuKien() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Ngày
        btnNgay.setOnClickListener(v -> {
            boLocHienTai = "NGAY";
            capNhatButtonActive(btnNgay);
            loadThongKeTheoNgay();
        });

        // Nút Tuần
        btnTuan.setOnClickListener(v -> {
            boLocHienTai = "TUAN";
            capNhatButtonActive(btnTuan);
            loadThongKeTheoTuan();
        });

        // Nút Tháng
        btnThang.setOnClickListener(v -> {
            boLocHienTai = "THANG";
            capNhatButtonActive(btnThang);
            loadThongKeTheoThang();
        });

        // Nút Năm
        btnNam.setOnClickListener(v -> {
            boLocHienTai = "NAM";
            capNhatButtonActive(btnNam);
            loadThongKeTheoNam();
        });

        // Nút Xuất PDF
        btnXuatPDF.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng xuất PDF\nSẽ làm ở phiên bản sau",
                    Toast.LENGTH_SHORT).show();
        });

        // Nút Xuất Excel
        btnXuatExcel.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng xuất Excel\nSẽ làm ở phiên bản sau",
                    Toast.LENGTH_SHORT).show();
        });

        // Nút Xuất báo cáo
        btnXuatBaoCao.setOnClickListener(v -> {
            Toast.makeText(this, "Xuất báo cáo tổng hợp\nĐang phát triển",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Cập nhật button active
     */
    private void capNhatButtonActive(Button buttonActive) {
        // Reset tất cả về outline
        btnNgay.setBackgroundResource(R.drawable.btn_outline_gold);
        btnNgay.setTextColor(mauVangGold);

        btnTuan.setBackgroundResource(R.drawable.btn_outline_gold);
        btnTuan.setTextColor(mauVangGold);

        btnThang.setBackgroundResource(R.drawable.btn_outline_gold);
        btnThang.setTextColor(mauVangGold);

        btnNam.setBackgroundResource(R.drawable.btn_outline_gold);
        btnNam.setTextColor(mauVangGold);

        // Set button active
        buttonActive.setBackgroundResource(R.drawable.btn_luxury);
        buttonActive.setTextColor(ContextCompat.getColor(this, R.color.den_sang_trong));
    }

    /**
     * Load thống kê theo NGÀY (30 ngày gần nhất)
     */
    private void loadThongKeTheoNgay() {
        Calendar cal = Calendar.getInstance();
        long denNgay = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, -29); // 30 ngày
        long tuNgay = cal.getTimeInMillis();

        // Lấy dữ liệu theo từng ngày
        Map<String, Long> doanhThuTheoNgay = new HashMap<>();
        Map<String, Integer> luotXeTheoNgay = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Khởi tạo 30 ngày với giá trị 0
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTimeInMillis(tuNgay);

        for (int i = 0; i < 30; i++) {
            String ngay = sdf.format(tempCal.getTime());
            doanhThuTheoNgay.put(ngay, 0L);
            luotXeTheoNgay.put(ngay, 0);
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Lấy dữ liệu từ database
        List<com.astrapark.quanly.model.LichSuXe> danhSach =
                lichSuXeDAO.layTheoKhoangThoiGian(tuNgay, denNgay);

        for (com.astrapark.quanly.model.LichSuXe lichSu : danhSach) {
            if (lichSu.getTrangThai().equals("DA_RA")) {
                String ngay = sdf.format(new java.util.Date(lichSu.getThoiGianVao()));

                // Cộng dồn doanh thu
                long doanhThuCu = doanhThuTheoNgay.getOrDefault(ngay, 0L);
                doanhThuTheoNgay.put(ngay, doanhThuCu + lichSu.getTienPhi());

                // Cộng dồn lượt xe
                int luotXeCu = luotXeTheoNgay.getOrDefault(ngay, 0);
                luotXeTheoNgay.put(ngay, luotXeCu + 1);
            }
        }

        // Vẽ biểu đồ
        veChartDoanhThu(doanhThuTheoNgay);
        veChartLuotXe(luotXeTheoNgay);
        veChartPhuongThuc(danhSach);

        // Cập nhật báo cáo
        capNhatBaoCao(danhSach, 30);
    }

    /**
     * Load thống kê theo TUẦN (12 tuần gần nhất)
     */
    private void loadThongKeTheoTuan() {
        Calendar cal = Calendar.getInstance();
        long denNgay = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -11); // 12 tuần
        long tuNgay = cal.getTimeInMillis();

        Map<String, Long> doanhThuTheoTuan = new HashMap<>();
        Map<String, Integer> luotXeTheoTuan = new HashMap<>();

        // Khởi tạo 12 tuần
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTimeInMillis(tuNgay);

        for (int i = 0; i < 12; i++) {
            String tuan = "T" + (i + 1);
            doanhThuTheoTuan.put(tuan, 0L);
            luotXeTheoTuan.put(tuan, 0);
        }

        // Lấy dữ liệu
        List<com.astrapark.quanly.model.LichSuXe> danhSach =
                lichSuXeDAO.layTheoKhoangThoiGian(tuNgay, denNgay);

        for (com.astrapark.quanly.model.LichSuXe lichSu : danhSach) {
            if (lichSu.getTrangThai().equals("DA_RA")) {
                // Tính tuần thứ mấy
                long khoangCach = lichSu.getThoiGianVao() - tuNgay;
                int soTuan = (int) (khoangCach / (7 * 24 * 60 * 60 * 1000));
                String tuan = "T" + (soTuan + 1);

                long doanhThuCu = doanhThuTheoTuan.getOrDefault(tuan, 0L);
                doanhThuTheoTuan.put(tuan, doanhThuCu + lichSu.getTienPhi());

                int luotXeCu = luotXeTheoTuan.getOrDefault(tuan, 0);
                luotXeTheoTuan.put(tuan, luotXeCu + 1);
            }
        }

        veChartDoanhThu(doanhThuTheoTuan);
        veChartLuotXe(luotXeTheoTuan);
        veChartPhuongThuc(danhSach);
        capNhatBaoCao(danhSach, 12 * 7);
    }

    /**
     * Load thống kê theo THÁNG (12 tháng gần nhất)
     */
    private void loadThongKeTheoThang() {
        Calendar cal = Calendar.getInstance();
        long denNgay = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -11); // 12 tháng
        long tuNgay = cal.getTimeInMillis();

        Map<String, Long> doanhThuTheoThang = new HashMap<>();
        Map<String, Integer> luotXeTheoThang = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/yy", Locale.getDefault());

        // Khởi tạo 12 tháng
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTimeInMillis(tuNgay);

        for (int i = 0; i < 12; i++) {
            String thang = sdf.format(tempCal.getTime());
            doanhThuTheoThang.put(thang, 0L);
            luotXeTheoThang.put(thang, 0);
            tempCal.add(Calendar.MONTH, 1);
        }

        // Lấy dữ liệu
        List<com.astrapark.quanly.model.LichSuXe> danhSach =
                lichSuXeDAO.layTheoKhoangThoiGian(tuNgay, denNgay);

        for (com.astrapark.quanly.model.LichSuXe lichSu : danhSach) {
            if (lichSu.getTrangThai().equals("DA_RA")) {
                String thang = sdf.format(new java.util.Date(lichSu.getThoiGianVao()));

                long doanhThuCu = doanhThuTheoThang.getOrDefault(thang, 0L);
                doanhThuTheoThang.put(thang, doanhThuCu + lichSu.getTienPhi());

                int luotXeCu = luotXeTheoThang.getOrDefault(thang, 0);
                luotXeTheoThang.put(thang, luotXeCu + 1);
            }
        }

        veChartDoanhThu(doanhThuTheoThang);
        veChartLuotXe(luotXeTheoThang);
        veChartPhuongThuc(danhSach);
        capNhatBaoCao(danhSach, 365);
    }

    /**
     * Load thống kê theo NĂM (5 năm gần nhất)
     */
    private void loadThongKeTheoNam() {
        Calendar cal = Calendar.getInstance();
        int namHienTai = cal.get(Calendar.YEAR);

        Map<String, Long> doanhThuTheoNam = new HashMap<>();
        Map<String, Integer> luotXeTheoNam = new HashMap<>();

        // Khởi tạo 5 năm
        for (int i = 4; i >= 0; i--) {
            String nam = String.valueOf(namHienTai - i);
            doanhThuTheoNam.put(nam, 0L);
            luotXeTheoNam.put(nam, 0);
        }

        // Lấy tất cả dữ liệu
        List<com.astrapark.quanly.model.LichSuXe> danhSach =
                lichSuXeDAO.layTatCaLichSu();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());

        for (com.astrapark.quanly.model.LichSuXe lichSu : danhSach) {
            if (lichSu.getTrangThai().equals("DA_RA")) {
                String nam = sdf.format(new java.util.Date(lichSu.getThoiGianVao()));

                if (doanhThuTheoNam.containsKey(nam)) {
                    long doanhThuCu = doanhThuTheoNam.get(nam);
                    doanhThuTheoNam.put(nam, doanhThuCu + lichSu.getTienPhi());

                    int luotXeCu = luotXeTheoNam.get(nam);
                    luotXeTheoNam.put(nam, luotXeCu + 1);
                }
            }
        }

        veChartDoanhThu(doanhThuTheoNam);
        veChartLuotXe(luotXeTheoNam);
        veChartPhuongThuc(danhSach);
        capNhatBaoCao(danhSach, 365 * 5);
    }

    /**
     * Vẽ biểu đồ cột doanh thu
     */
    private void veChartDoanhThu(Map<String, Long> duLieu) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Long> entry : duLieu.entrySet()) {
            labels.add(entry.getKey());
            entries.add(new BarEntry(index++, entry.getValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (VNĐ)");
        dataSet.setColor(mauVangGold);
        dataSet.setValueTextColor(mauTrang);
        dataSet.setValueTextSize(10f);

        // Format giá trị hiển thị
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format("%.1fM", value / 1000000);
                } else if (value >= 1000) {
                    return String.format("%.0fK", value / 1000);
                }
                return "";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        // Cấu hình chart
        chartDoanhThu.setData(barData);
        chartDoanhThu.getDescription().setEnabled(false);
        chartDoanhThu.setDrawGridBackground(false);
        chartDoanhThu.setDrawBarShadow(false);
        chartDoanhThu.setDrawValueAboveBar(true);
        chartDoanhThu.setPinchZoom(false);
        chartDoanhThu.setDoubleTapToZoomEnabled(false);

        // Trục X
        XAxis xAxis = chartDoanhThu.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(mauXam);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45);

        // Trục Y trái
        YAxis leftAxis = chartDoanhThu.getAxisLeft();
        leftAxis.setTextColor(mauXam);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#30FFFFFF"));
        leftAxis.setAxisMinimum(0f);

        // Trục Y phải
        chartDoanhThu.getAxisRight().setEnabled(false);

        // Legend
        Legend legend = chartDoanhThu.getLegend();
        legend.setTextColor(mauTrang);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        chartDoanhThu.animateY(1000);
        chartDoanhThu.invalidate();
    }

    /**
     * Vẽ biểu đồ đường lượt xe
     */
    private void veChartLuotXe(Map<String, Integer> duLieu) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : duLieu.entrySet()) {
            labels.add(entry.getKey());
            entries.add(new Entry(index++, entry.getValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Số lượt xe");
        dataSet.setColor(mauVangGold);
        dataSet.setCircleColor(mauVangGold);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(mauTrang);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(mauVangGold);
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);

        // Cấu hình chart
        chartLuotXe.setData(lineData);
        chartLuotXe.getDescription().setEnabled(false);
        chartLuotXe.setDrawGridBackground(false);
        chartLuotXe.setPinchZoom(false);
        chartLuotXe.setDoubleTapToZoomEnabled(false);

        // Trục X
        XAxis xAxis = chartLuotXe.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(mauXam);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45);

        // Trục Y
        YAxis leftAxis = chartLuotXe.getAxisLeft();
        leftAxis.setTextColor(mauXam);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#30FFFFFF"));
        leftAxis.setAxisMinimum(0f);

        chartLuotXe.getAxisRight().setEnabled(false);

        // Legend
        Legend legend = chartLuotXe.getLegend();
        legend.setTextColor(mauTrang);

        chartLuotXe.animateX(1000);
        chartLuotXe.invalidate();
    }

    /**
     * Vẽ biểu đồ tròn phương thức thanh toán
     */
    private void veChartPhuongThuc(List<com.astrapark.quanly.model.LichSuXe> danhSach) {
        Map<String, Integer> phuongThuc = new HashMap<>();
        phuongThuc.put("Tiền mặt", 0);
        phuongThuc.put("Chuyển khoản", 0);
        phuongThuc.put("Thẻ", 0);

        for (com.astrapark.quanly.model.LichSuXe lichSu : danhSach) {
            if (lichSu.getTrangThai().equals("DA_RA")) {
                String pt = lichSu.getPhuongThucThanhToan();
                if (pt != null) {
                    switch (pt) {
                        case "TIEN_MAT":
                            phuongThuc.put("Tiền mặt", phuongThuc.get("Tiền mặt") + 1);
                            break;
                        case "CHUYEN_KHOAN":
                            phuongThuc.put("Chuyển khoản", phuongThuc.get("Chuyển khoản") + 1);
                            break;
                        case "THE":
                            phuongThuc.put("Thẻ", phuongThuc.get("Thẻ") + 1);
                            break;
                    }
                }
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : phuongThuc.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "Chưa có dữ liệu"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Màu sắc
        int[] colors = {
                Color.parseColor("#D4AF37"), // Vàng gold
                Color.parseColor("#4CAF50"), // Xanh lá
                Color.parseColor("#2196F3")  // Xanh dương
        };
        dataSet.setColors(colors);

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f%%", value);
            }
        });

        // Cấu hình chart
        chartPhuongThuc.setData(pieData);
        chartPhuongThuc.getDescription().setEnabled(false);
        chartPhuongThuc.setDrawHoleEnabled(true);
        chartPhuongThuc.setHoleColor(Color.TRANSPARENT);
        chartPhuongThuc.setHoleRadius(40f);
        chartPhuongThuc.setTransparentCircleRadius(45f);
        chartPhuongThuc.setDrawCenterText(true);
        chartPhuongThuc.setCenterText("Phương thức\nthanh toán");
        chartPhuongThuc.setCenterTextColor(mauTrang);
        chartPhuongThuc.setCenterTextSize(12f);
        chartPhuongThuc.setUsePercentValues(true);

        // Legend
        Legend legend = chartPhuongThuc.getLegend();
        legend.setTextColor(mauTrang);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        chartPhuongThuc.animateY(1000);
        chartPhuongThuc.invalidate();
    }

    /**
     * Cập nhật báo cáo tổng hợp
     */
    private void capNhatBaoCao(List<com.astrapark.quanly.model.LichSuXe> danhSach, int soNgay) {
        long tongDoanhThu = 0;
        int tongLuotXe = 0;

        for (com.astrapark.quanly.model.LichSuXe lichSu : danhSach) {
            if (lichSu.getTrangThai().equals("DA_RA")) {
                tongDoanhThu += lichSu.getTienPhi();
                tongLuotXe++;
            }
        }

        // Format số
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Tổng doanh thu
        txtTongDoanhThu.setText(formatter.format(tongDoanhThu) + " VNĐ");

        // Tổng lượt xe
        txtTongLuotXe.setText(tongLuotXe + " lượt");

        // Trung bình/ngày
        long trungBinh = soNgay > 0 ? tongDoanhThu / soNgay : 0;
        txtTrungBinhNgay.setText(formatter.format(trungBinh) + " VNĐ");

        // So sánh với kỳ trước (giả lập - sẽ query thật từ DB)
        double phanTramThayDoi = (Math.random() - 0.5) * 40; // -20% đến +20%

        if (phanTramThayDoi > 0) {
            imgTrend.setImageResource(R.drawable.ic_trending_up);
            txtSoSanh.setText(String.format("Tăng %.1f%% so với kỳ trước", phanTramThayDoi));
        } else if (phanTramThayDoi < 0) {
            imgTrend.setImageResource(R.drawable.ic_trending_down);
            txtSoSanh.setText(String.format("Giảm %.1f%% so với kỳ trước", Math.abs(phanTramThayDoi)));
        } else {
            imgTrend.setVisibility(View.GONE);
            txtSoSanh.setText("Không thay đổi so với kỳ trước");
        }
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
}