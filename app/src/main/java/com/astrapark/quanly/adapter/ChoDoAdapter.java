package com.astrapark.quanly.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrapark.quanly.R;
import com.astrapark.quanly.model.ChoDo;

import java.util.List;

/**
 * ChoDoAdapter v4.2 FINAL - SYNC DATABASE
 */
public class ChoDoAdapter extends RecyclerView.Adapter<ChoDoAdapter.ChoDoViewHolder> {

    private static final String TAG = "ChoDoAdapter";

    private List<ChoDo> danhSachChoDo;
    private OnChoDoClickListener listener;

    public interface OnChoDoClickListener {
        void onChoDoClick(ChoDo choDo, int position);
    }

    public ChoDoAdapter(List<ChoDo> danhSachChoDo, OnChoDoClickListener listener) {
        this.danhSachChoDo = danhSachChoDo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChoDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cho_do, parent, false);
        return new ChoDoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoDoViewHolder holder, int position) {
        ChoDo choDo = danhSachChoDo.get(position);

        holder.txtSoChoDo.setText(String.valueOf(choDo.getId()));
        hienThiTheoTrangThai(holder, choDo);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChoDoClick(choDo, holder.getAdapterPosition());
            }
        });
    }

    private void hienThiTheoTrangThai(ChoDoViewHolder holder, ChoDo choDo) {
        String trangThai = choDo.getTrangThai();

        if (trangThai != null) {
            trangThai = trangThai.trim().toUpperCase();
        } else {
            trangThai = "TRONG";
        }

        Log.d(TAG, String.format("🎨 Display chỗ %d | '%s' | %s",
                choDo.getId(), trangThai, choDo.getBienSoXe()));

        switch (trangThai) {
            case "TRONG":
                holder.layoutChoDo.setBackgroundResource(R.drawable.bg_cho_do_trong);
                holder.imgTrangThaiChoDo.setImageResource(R.drawable.ic_parking_empty);
                holder.txtBienSoXe.setText("TRỐNG");
                holder.txtBienSoXe.setTextColor(Color.WHITE);
                holder.txtThoiGianDo.setVisibility(View.GONE);
                break;

            case "DANG_DO":
            case "DANG DO":
                holder.layoutChoDo.setBackgroundResource(R.drawable.bg_cho_do_dang_do);
                holder.imgTrangThaiChoDo.setImageResource(R.drawable.ic_car_parked);

                String bienSo = choDo.getBienSoXe();
                holder.txtBienSoXe.setText(bienSo != null && !bienSo.isEmpty() ? bienSo : "XE ĐỖ");
                holder.txtBienSoXe.setTextColor(Color.WHITE);

                if (choDo.getThoiGianVao() > 0) {
                    String thoiGian = tinhThoiGianDo(choDo.getThoiGianVao());
                    holder.txtThoiGianDo.setText(thoiGian);
                    holder.txtThoiGianDo.setVisibility(View.VISIBLE);
                } else {
                    holder.txtThoiGianDo.setVisibility(View.GONE);
                }
                break;

            case "DA_DAT":
            case "DA DAT":
                holder.layoutChoDo.setBackgroundResource(R.drawable.bg_cho_do_da_dat);
                holder.imgTrangThaiChoDo.setImageResource(R.drawable.ic_booking);
                holder.txtBienSoXe.setText("ĐÃ ĐẶT");
                holder.txtBienSoXe.setTextColor(Color.BLACK);
                holder.txtThoiGianDo.setVisibility(View.GONE);
                break;

            case "BAO_TRI":
            case "BAO TRI":
                holder.layoutChoDo.setBackgroundColor(Color.parseColor("#808080"));
                holder.imgTrangThaiChoDo.setImageResource(R.drawable.ic_maintenance);
                holder.txtBienSoXe.setText("BẢO TRÌ");
                holder.txtBienSoXe.setTextColor(Color.WHITE);
                holder.txtThoiGianDo.setVisibility(View.GONE);
                break;

            default:
                Log.w(TAG, "  ⚠️ Unknown state: '" + trangThai + "'");
                holder.layoutChoDo.setBackgroundResource(R.drawable.bg_cho_do_trong);
                holder.imgTrangThaiChoDo.setImageResource(R.drawable.ic_parking_empty);
                holder.txtBienSoXe.setText("TRỐNG");
                holder.txtBienSoXe.setTextColor(Color.WHITE);
                holder.txtThoiGianDo.setVisibility(View.GONE);
                break;
        }
    }

    private String tinhThoiGianDo(long thoiGianVao) {
        if (thoiGianVao == 0) return "N/A";

        long hienTai = System.currentTimeMillis();
        long thoiGianDoMs = hienTai - thoiGianVao;

        if (thoiGianDoMs < 0) return "0 phút";

        long phut = (thoiGianDoMs / 1000) / 60;
        long gio = phut / 60;

        if (gio > 0) {
            return gio + " giờ " + (phut % 60) + " phút";
        } else {
            return phut + " phút";
        }
    }

    @Override
    public int getItemCount() {
        return danhSachChoDo != null ? danhSachChoDo.size() : 0;
    }

    public void updateFromFirebase(int choDoId, String trangThai, String bienSo, long thoiGianVao) {
        Log.d(TAG, "========================================");
        Log.d(TAG, String.format("📥 UPDATE: Chỗ %d | %s | %s | Time: %d",
                choDoId, trangThai, bienSo, thoiGianVao));

        int position = -1;

        for (int i = 0; i < danhSachChoDo.size(); i++) {
            if (danhSachChoDo.get(i).getId() == choDoId) {
                position = i;
                break;
            }
        }

        if (position == -1) {
            Log.e(TAG, "❌ Không tìm thấy chỗ " + choDoId);
            Log.d(TAG, "========================================");
            return;
        }

        ChoDo choDo = danhSachChoDo.get(position);
        String oldTrangThai = choDo.getTrangThai();

        if (trangThai != null) {
            trangThai = trangThai.trim().toUpperCase();
        }

        choDo.setTrangThai(trangThai);
        choDo.setBienSoXe(bienSo != null ? bienSo : "");

        if ("DANG_DO".equalsIgnoreCase(trangThai) || "DANG DO".equalsIgnoreCase(trangThai)) {
            if (thoiGianVao > 0) {
                choDo.setThoiGianVao(thoiGianVao);
                Log.d(TAG, "  ⏰ SET timestamp from Firebase: " + thoiGianVao);
            } else {
                if (!"DANG_DO".equalsIgnoreCase(oldTrangThai)) {
                    choDo.setThoiGianVao(System.currentTimeMillis());
                    Log.d(TAG, "  ⏰ SET new timestamp: " + choDo.getThoiGianVao());
                } else {
                    Log.d(TAG, "  ⏰ KEEP old timestamp: " + choDo.getThoiGianVao());
                }
            }
        } else {
            choDo.setThoiGianVao(0);
            Log.d(TAG, "  ⏰ CLEAR timestamp");
        }

        notifyItemChanged(position);

        Log.d(TAG, String.format("✅ Updated: %s → %s", oldTrangThai, trangThai));
        Log.d(TAG, "========================================");
    }

    public void updateFromFirebase(int choDoId, String trangThai, String bienSo) {
        updateFromFirebase(choDoId, trangThai, bienSo, 0);
    }

    public void capNhatDanhSach(List<ChoDo> danhSachMoi) {
        this.danhSachChoDo = danhSachMoi;
        notifyDataSetChanged();
    }

    static class ChoDoViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout layoutChoDo;
        TextView txtSoChoDo;
        ImageView imgTrangThaiChoDo;
        TextView txtBienSoXe;
        TextView txtThoiGianDo;

        public ChoDoViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutChoDo = itemView.findViewById(R.id.layoutChoDo);
            txtSoChoDo = itemView.findViewById(R.id.txtSoChoDo);
            imgTrangThaiChoDo = itemView.findViewById(R.id.imgTrangThaiChoDo);
            txtBienSoXe = itemView.findViewById(R.id.txtBienSoXe);
            txtThoiGianDo = itemView.findViewById(R.id.txtThoiGianDo);
        }
    }
}