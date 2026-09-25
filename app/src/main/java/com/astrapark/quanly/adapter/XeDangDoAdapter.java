package com.astrapark.quanly.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.astrapark.quanly.R;
import com.astrapark.quanly.model.XeDangDo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách xe đang đỗ
 * VERSION 2.0 - ĐÃ TÍCH HỢP FIREBASE REALTIME UPDATE
 */
public class XeDangDoAdapter extends RecyclerView.Adapter<XeDangDoAdapter.XeDangDoViewHolder> {

    private List<XeDangDo> danhSachXe;
    private OnXeDangDoListener listener;

    /**
     * Interface xử lý sự kiện
     */
    public interface OnXeDangDoListener {
        void onChiTietClick(XeDangDo xe, int position);
        void onChoRaClick(XeDangDo xe, int position);
    }

    /**
     * Constructor
     */
    public XeDangDoAdapter(List<XeDangDo> danhSachXe, OnXeDangDoListener listener) {
        this.danhSachXe = danhSachXe;
        this.listener = listener;
    }

    @NonNull
    @Override
    public XeDangDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_xe_dang_do, parent, false);
        return new XeDangDoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull XeDangDoViewHolder holder, int position) {
        XeDangDo xe = danhSachXe.get(position);

        // Hiển thị thông tin
        holder.txtBadgeChoDo.setText(String.valueOf(xe.getChoDo()));
        holder.txtBienSoXe.setText(xe.getBienSoXe());

        // Thời gian vào (chỉ giờ:phút)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.txtThoiGianVao.setText(sdf.format(new Date(xe.getThoiGianVao())));

        // Thời gian đỗ
        holder.txtThoiGianDo.setText(xe.getThoiGianDoString());

        // Tiền dự kiến
        holder.txtTienDuKien.setText(xe.getTienString());

        // Ghi chú (nếu có)
        if (xe.getGhiChu() != null && !xe.getGhiChu().isEmpty()) {
            holder.txtGhiChu.setText("Ghi chú: " + xe.getGhiChu());
            holder.txtGhiChu.setVisibility(View.VISIBLE);
        } else {
            holder.txtGhiChu.setVisibility(View.GONE);
        }

        // Sự kiện nút Chi tiết
        holder.btnChiTiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChiTietClick(xe, holder.getAdapterPosition());
                }
            }
        });

        // Sự kiện nút Cho ra
        holder.btnChoRa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChoRaClick(xe, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSachXe != null ? danhSachXe.size() : 0;
    }

    /**
     * Cập nhật danh sách
     */
    public void capNhatDanhSach(List<XeDangDo> danhSachMoi) {
        this.danhSachXe = danhSachMoi;
        notifyDataSetChanged();
    }

    /**
     * Xóa 1 xe khỏi danh sách
     */
    public void xoaXe(int position) {
        if (position >= 0 && position < danhSachXe.size()) {
            danhSachXe.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, danhSachXe.size());
        }
    }

    /**
     * ===== METHOD MỚI: CẬP NHẬT TỪ FIREBASE =====
     * Update xe khi nhận data từ Firebase
     */
    public void updateFromFirebase(int choDoId, String bienSo) {
        // Tìm xe theo chỗ đỗ
        int position = -1;
        for (int i = 0; i < danhSachXe.size(); i++) {
            if (danhSachXe.get(i).getChoDo() == choDoId) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            // Cập nhật biển số (nếu thay đổi)
            XeDangDo xe = danhSachXe.get(position);
            if (!xe.getBienSoXe().equals(bienSo)) {
                xe.setBienSoXe(bienSo);
                notifyItemChanged(position);

                android.util.Log.d("XeDangDoAdapter",
                        String.format("🔄 Updated xe chỗ %d: %s", choDoId, bienSo));
            }
        }
    }

    /**
     * ===== METHOD MỚI: XÓA XE KHI FIREBASE BÁO RA =====
     */
    public void removeByChoDo(int choDoId) {
        int position = -1;
        for (int i = 0; i < danhSachXe.size(); i++) {
            if (danhSachXe.get(i).getChoDo() == choDoId) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            xoaXe(position);
            android.util.Log.d("XeDangDoAdapter",
                    String.format("🗑️ Removed xe chỗ %d (Firebase sync)", choDoId));
        }
    }

    /**
     * ===== METHOD MỚI: CẬP NHẬT VỚI ANIMATION =====
     */
    public void updateWithAnimation(int position) {
        if (position >= 0 && position < danhSachXe.size()) {
            notifyItemChanged(position, "FIREBASE_UPDATE");
        }
    }

    /**
     * ===== OVERRIDE: Xử lý partial update với payload =====
     */
    @Override
    public void onBindViewHolder(@NonNull XeDangDoViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Update với animation
            XeDangDo xe = danhSachXe.get(position);

            // Pulse animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 1f, 1.05f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1f, 1.05f, 1f);
            scaleX.setDuration(300);
            scaleY.setDuration(300);
            scaleX.start();
            scaleY.start();

            // Update data
            holder.txtThoiGianDo.setText(xe.getThoiGianDoString());
            holder.txtTienDuKien.setText(xe.getTienString());
        }
    }

    /**
     * ViewHolder
     */
    static class XeDangDoViewHolder extends RecyclerView.ViewHolder {

        TextView txtBadgeChoDo;
        TextView txtBienSoXe;
        TextView txtThoiGianVao;
        TextView txtThoiGianDo;
        TextView txtTienDuKien;
        TextView txtGhiChu;
        Button btnChiTiet;
        Button btnChoRa;

        public XeDangDoViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBadgeChoDo = itemView.findViewById(R.id.txtBadgeChoDo);
            txtBienSoXe = itemView.findViewById(R.id.txtBienSoXe);
            txtThoiGianVao = itemView.findViewById(R.id.txtThoiGianVao);
            txtThoiGianDo = itemView.findViewById(R.id.txtThoiGianDo);
            txtTienDuKien = itemView.findViewById(R.id.txtTienDuKien);
            txtGhiChu = itemView.findViewById(R.id.txtGhiChu);
            btnChiTiet = itemView.findViewById(R.id.btnChiTiet);
            btnChoRa = itemView.findViewById(R.id.btnChoRa);
        }
    }
}