package com.astrapark.quanly.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.astrapark.quanly.R;
import com.astrapark.quanly.model.LichSuXe;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách lịch sử xe
 */
public class LichSuXeAdapter extends RecyclerView.Adapter<LichSuXeAdapter.LichSuViewHolder> {

    private List<LichSuXe> danhSachLichSu;
    private OnLichSuListener listener;

    /**
     * Interface xử lý sự kiện
     */
    public interface OnLichSuListener {
        void onChiTietClick(LichSuXe lichSu, int position);
        void onInHoaDonClick(LichSuXe lichSu, int position);
    }

    /**
     * Constructor
     */
    public LichSuXeAdapter(List<LichSuXe> danhSachLichSu, OnLichSuListener listener) {
        this.danhSachLichSu = danhSachLichSu;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LichSuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lich_su_xe, parent, false);
        return new LichSuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichSuViewHolder holder, int position) {
        LichSuXe lichSu = danhSachLichSu.get(position);

        // Hiển thị biển số
        holder.txtBienSoXe.setText(lichSu.getBienSoXe());

        // Hiển thị chỗ đỗ
        holder.txtChoDo.setText(String.valueOf(lichSu.getChoDo()));

        // Hiển thị trạng thái
        hienThiTrangThai(holder, lichSu);

        // Hiển thị thời gian vào
        SimpleDateFormat sdfFull = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

        holder.txtThoiGianVao.setText(sdfFull.format(new Date(lichSu.getThoiGianVao())));

        // Hiển thị thời gian ra và thời gian đỗ
        if (lichSu.getTrangThai().equals("DA_RA")) {
            holder.txtThoiGianRa.setText(sdfTime.format(new Date(lichSu.getThoiGianRa())));
            holder.txtThoiGianDo.setText(" (" + lichSu.getThoiGianDoString() + ")");
            holder.txtThoiGianRa.setVisibility(View.VISIBLE);
            holder.txtThoiGianDo.setVisibility(View.VISIBLE);
        } else {
            // Đang đỗ
            holder.txtThoiGianRa.setText("Đang đỗ");
            holder.txtThoiGianRa.setTextColor(Color.parseColor("#FFB700"));
            holder.txtThoiGianDo.setVisibility(View.GONE);
        }

        // Hiển thị tiền
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.txtTienPhi.setText(formatter.format(lichSu.getTienPhi()));

        // Hiển thị phương thức thanh toán
        String phuongThuc = layTenPhuongThuc(lichSu.getPhuongThucThanhToan());
        holder.txtPhuongThuc.setText(phuongThuc);

        // Hiển thị ghi chú (nếu có)
        if (lichSu.getGhiChu() != null && !lichSu.getGhiChu().isEmpty()) {
            holder.txtGhiChu.setText("💬 Ghi chú: " + lichSu.getGhiChu());
            holder.txtGhiChu.setVisibility(View.VISIBLE);
        } else {
            holder.txtGhiChu.setVisibility(View.GONE);
        }

        // Sự kiện nút Chi tiết
        holder.btnChiTiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChiTietClick(lichSu, holder.getAdapterPosition());
                }
            }
        });

        // Sự kiện nút In hóa đơn
        holder.btnInHoaDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onInHoaDonClick(lichSu, holder.getAdapterPosition());
                }
            }
        });
    }

    /**
     * Hiển thị badge trạng thái
     */
    private void hienThiTrangThai(LichSuViewHolder holder, LichSuXe lichSu) {
        if (lichSu.getTrangThai().equals("DA_RA")) {
            holder.txtBadgeTrangThai.setText("✅ Đã ra");
            holder.txtBadgeTrangThai.setBackgroundResource(R.drawable.badge_success);
        } else {
            holder.txtBadgeTrangThai.setText("⏱ Đang đỗ");
            holder.txtBadgeTrangThai.setBackgroundResource(R.drawable.badge_warning);
        }
    }

    /**
     * Lấy tên phương thức thanh toán
     */
    private String layTenPhuongThuc(String code) {
        if (code == null || code.isEmpty()) {
            return "Chưa thanh toán";
        }

        switch (code) {
            case "TIEN_MAT":
                return "Tiền mặt";
            case "CHUYEN_KHOAN":
                return "Chuyển khoản";
            case "THE":
                return "Thẻ";
            default:
                return code;
        }
    }

    @Override
    public int getItemCount() {
        return danhSachLichSu != null ? danhSachLichSu.size() : 0;
    }

    /**
     * Cập nhật danh sách
     */
    public void capNhatDanhSach(List<LichSuXe> danhSachMoi) {
        this.danhSachLichSu = danhSachMoi;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder
     */
    static class LichSuViewHolder extends RecyclerView.ViewHolder {

        TextView txtBienSoXe;
        TextView txtChoDo;
        TextView txtBadgeTrangThai;
        TextView txtThoiGianVao;
        TextView txtThoiGianRa;
        TextView txtThoiGianDo;
        TextView txtTienPhi;
        TextView txtPhuongThuc;
        TextView txtGhiChu;
        Button btnChiTiet;
        Button btnInHoaDon;

        public LichSuViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBienSoXe = itemView.findViewById(R.id.txtBienSoXe);
            txtChoDo = itemView.findViewById(R.id.txtChoDo);
            txtBadgeTrangThai = itemView.findViewById(R.id.txtBadgeTrangThai);
            txtThoiGianVao = itemView.findViewById(R.id.txtThoiGianVao);
            txtThoiGianRa = itemView.findViewById(R.id.txtThoiGianRa);
            txtThoiGianDo = itemView.findViewById(R.id.txtThoiGianDo);
            txtTienPhi = itemView.findViewById(R.id.txtTienPhi);
            txtPhuongThuc = itemView.findViewById(R.id.txtPhuongThuc);
            txtGhiChu = itemView.findViewById(R.id.txtGhiChu);
            btnChiTiet = itemView.findViewById(R.id.btnChiTiet);
            btnInHoaDon = itemView.findViewById(R.id.btnInHoaDon);
        }
    }
}