package com.astrapark.quanly.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrapark.quanly.R;
import com.astrapark.quanly.model.LichSuXe;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView kết quả tìm kiếm
 */
public class TimKiemAdapter extends RecyclerView.Adapter<TimKiemAdapter.ViewHolder> {

    private List<LichSuXe> danhSach;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LichSuXe lichSu);
    }

    public TimKiemAdapter(List<LichSuXe> danhSach, OnItemClickListener listener) {
        this.danhSach = danhSach;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ket_qua_tim_kiem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichSuXe lichSu = danhSach.get(position);
        holder.bind(lichSu);
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtBienSo, txtTrangThai, txtThoiGianVao, txtThoiGianRa;
        TextView txtThoiGianDo, txtChoDo, txtTien, txtPhuongThuc;
        LinearLayout layoutThoiGianRa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBienSo = itemView.findViewById(R.id.txtBienSo);
            txtTrangThai = itemView.findViewById(R.id.txtTrangThai);
            txtThoiGianVao = itemView.findViewById(R.id.txtThoiGianVao);
            txtThoiGianRa = itemView.findViewById(R.id.txtThoiGianRa);
            txtThoiGianDo = itemView.findViewById(R.id.txtThoiGianDo);
            txtChoDo = itemView.findViewById(R.id.txtChoDo);
            txtTien = itemView.findViewById(R.id.txtTien);
            txtPhuongThuc = itemView.findViewById(R.id.txtPhuongThuc);
            layoutThoiGianRa = itemView.findViewById(R.id.layoutThoiGianRa);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(danhSach.get(getAdapterPosition()));
                }
            });
        }

        public void bind(LichSuXe lichSu) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

            // Biển số
            txtBienSo.setText(lichSu.getBienSoXe());

            // Trạng thái
            if (lichSu.getTrangThai().equals("DANG_DO")) {
                txtTrangThai.setText("ĐANG ĐỖ");
                txtTrangThai.setBackgroundResource(R.drawable.bg_badge_dang_do);
            } else {
                txtTrangThai.setText("ĐÃ RA");
                txtTrangThai.setBackgroundResource(R.drawable.bg_badge_da_ra);
            }

            // Thời gian vào
            txtThoiGianVao.setText(sdf.format(lichSu.getThoiGianVao()));

            // Thời gian ra
            if (lichSu.getTrangThai().equals("DA_RA")) {
                layoutThoiGianRa.setVisibility(View.VISIBLE);
                txtThoiGianRa.setText(sdf.format(lichSu.getThoiGianRa()));

                // Thời gian đỗ
                int phut = lichSu.getThoiGianDoPhut();
                int gio = phut / 60;
                int phutConLai = phut % 60;
                String thoiGianDo = gio > 0 ? gio + "h " + phutConLai + "p" : phut + " phút";
                txtThoiGianDo.setText(thoiGianDo);

                // Tiền
                txtTien.setText(nf.format(lichSu.getTienThanhToan()) + " VNĐ");

                // Phương thức
                String phuongThuc = "💵 ";
                switch (lichSu.getPhuongThucThanhToan()) {
                    case "TIEN_MAT":
                        phuongThuc += "Tiền mặt";
                        break;
                    case "CHUYEN_KHOAN":
                        phuongThuc = "🏦 Chuyển khoản";
                        break;
                    case "THE":
                        phuongThuc = "💳 Thẻ";
                        break;
                }
                txtPhuongThuc.setText(phuongThuc);
                txtPhuongThuc.setVisibility(View.VISIBLE);

            } else {
                layoutThoiGianRa.setVisibility(View.GONE);

                // Tính thời gian đỗ hiện tại
                long thoiGianDoMs = System.currentTimeMillis() - lichSu.getThoiGianVao();
                int phut = (int) ((thoiGianDoMs / 1000) / 60);
                int gio = phut / 60;
                int phutConLai = phut % 60;
                String thoiGianDo = gio > 0 ? gio + "h " + phutConLai + "p" : phut + " phút";
                txtThoiGianDo.setText(thoiGianDo + " (đang đỗ)");

                // Ẩn tiền và phương thức
                txtTien.setText("---");
                txtPhuongThuc.setVisibility(View.GONE);
            }

            // Chỗ đỗ
            txtChoDo.setText(String.valueOf(lichSu.getChoDo()));
        }
    }
}