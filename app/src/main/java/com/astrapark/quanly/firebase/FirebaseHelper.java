package com.astrapark.quanly.firebase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Helper v4.2 FINAL - SYNC FIREBASE ↔ DATABASE
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    private FirebaseDatabase database;
    private DatabaseReference rootRef;
    private DatabaseReference choDoRef;
    private DatabaseReference barrierRef;
    private DatabaseReference statusRef;
    private DatabaseReference eventsRef;

    private ValueEventListener choDoValueListener;
    private ValueEventListener eventsValueListener;
    private ValueEventListener barrierValueListener;
    private ValueEventListener statusValueListener;

    private ChoDoChangeListener choDoListener;
    private BarrierStatusListener barrierListener;
    private StatusChangeListener statusListener;
    private EventListener eventListener;

    private volatile boolean isDestroyed = false;
    private Handler mainHandler;

    // ============= INTERFACES =============

    public interface ChoDoChangeListener {
        void onChoDoChanged(int choDoId, String trangThai, String bienSo, long thoiGianVao);
    }

    public interface BarrierStatusListener {
        void onBarrierStatusChanged(String barrier, String status);
    }

    public interface StatusChangeListener {
        void onStatusChanged(int totalCars, int availableSlots);
    }

    public interface EventListener {
        void onEventReceived(int slotNum, String eventType, long timestamp);
    }

    // ============= CONSTRUCTOR =============

    public FirebaseHelper() {
        try {
            Log.d(TAG, "========== FIREBASE V4.2 FINAL ==========");

            mainHandler = new Handler(Looper.getMainLooper());

            database = FirebaseDatabase.getInstance(
                    "https://astrapark-61ad4-default-rtdb.asia-southeast1.firebasedatabase.app/"
            );

            rootRef = database.getReference("astrapark");
            choDoRef = rootRef.child("cho_do");
            barrierRef = rootRef.child("barrier");
            statusRef = rootRef.child("status");
            eventsRef = rootRef.child("events");

            Log.d(TAG, "✅ Firebase initialized");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============= SET LISTENERS =============

    public void setChoDoListener(ChoDoChangeListener listener) {
        if (isDestroyed || choDoRef == null) {
            Log.w(TAG, "⚠️ Cannot set listener");
            return;
        }

        this.choDoListener = listener;

        if (choDoValueListener != null) {
            choDoRef.removeEventListener(choDoValueListener);
            Log.d(TAG, "🗑️ Removed old listener");
        }

        choDoValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isDestroyed) return;

                Log.d(TAG, "📩 [CHỖ ĐỖ] Data changed - Count: " + snapshot.getChildrenCount());

                for (DataSnapshot choSnapshot : snapshot.getChildren()) {
                    try {
                        String key = choSnapshot.getKey();
                        if (key == null) continue;

                        int choDoId = Integer.parseInt(key);
                        String trangThai = choSnapshot.child("trang_thai").getValue(String.class);
                        String bienSo = choSnapshot.child("bien_so").getValue(String.class);
                        Long thoiGianVao = choSnapshot.child("thoi_gian_vao").getValue(Long.class);

                        if (trangThai == null) trangThai = "trong";
                        if (bienSo == null) bienSo = "";
                        if (thoiGianVao == null) thoiGianVao = 0L;

                        final int finalId = choDoId;
                        final String finalTrangThai = trangThai.toUpperCase();
                        final String finalBienSo = bienSo;
                        final long finalThoiGianVao = thoiGianVao;

                        Log.d(TAG, String.format("  → Chỗ %d: %s | %s | Time: %d",
                                finalId, finalTrangThai, finalBienSo, finalThoiGianVao));

                        if (mainHandler != null && choDoListener != null) {
                            mainHandler.post(() -> {
                                if (!isDestroyed && choDoListener != null) {
                                    choDoListener.onChoDoChanged(finalId, finalTrangThai,
                                            finalBienSo, finalThoiGianVao);
                                }
                            });
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing slot: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Listener cancelled: " + error.getMessage());
            }
        };

        choDoRef.addValueEventListener(choDoValueListener);
        Log.d(TAG, "✅ Listener attached");
    }

    public void setEventListener(EventListener listener) {
        if (isDestroyed || eventsRef == null) return;

        this.eventListener = listener;

        if (eventsValueListener != null) {
            eventsRef.removeEventListener(eventsValueListener);
        }

        eventsValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isDestroyed) return;

                Log.d(TAG, "📩 [EVENTS] Data changed");

                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    try {
                        String key = eventSnapshot.getKey();
                        if (key == null) continue;

                        int slotNum;
                        if (key.startsWith("slot_")) {
                            slotNum = Integer.parseInt(key.substring(5));
                        } else {
                            slotNum = Integer.parseInt(key);
                        }

                        String eventType = eventSnapshot.child("type").getValue(String.class);
                        Long timestamp = eventSnapshot.child("timestamp").getValue(Long.class);
                        Boolean processed = eventSnapshot.child("processed").getValue(Boolean.class);

                        if (eventType == null || timestamp == null) continue;

                        if (processed == null || !processed) {
                            final int finalSlot = slotNum;
                            final String finalType = eventType;
                            final long finalTime = timestamp;

                            Log.d(TAG, String.format("🔔 [EVENT] Chỗ %d | %s", finalSlot, finalType));

                            if (mainHandler != null && eventListener != null) {
                                mainHandler.post(() -> {
                                    if (!isDestroyed && eventListener != null) {
                                        eventListener.onEventReceived(finalSlot, finalType, finalTime);
                                    }
                                });
                            }

                            eventSnapshot.getRef().child("processed").setValue(true);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing event: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Events cancelled: " + error.getMessage());
            }
        };

        eventsRef.addValueEventListener(eventsValueListener);
        Log.d(TAG, "✅ Events listener attached");
    }

    public void setStatusListener(StatusChangeListener listener) {
        if (isDestroyed || statusRef == null) return;

        this.statusListener = listener;

        if (statusValueListener != null) {
            statusRef.removeEventListener(statusValueListener);
        }

        statusValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isDestroyed) return;

                try {
                    Integer totalCars = snapshot.child("total_cars").getValue(Integer.class);
                    Integer availableSlots = snapshot.child("available_slots").getValue(Integer.class);

                    if (totalCars == null) totalCars = 0;
                    if (availableSlots == null) availableSlots = 6;

                    final int finalTotalCars = totalCars;
                    final int finalAvailableSlots = availableSlots;

                    Log.d(TAG, String.format("📊 [STATUS] %d/%d", finalTotalCars, finalAvailableSlots));

                    if (mainHandler != null && statusListener != null) {
                        mainHandler.post(() -> {
                            if (!isDestroyed && statusListener != null) {
                                statusListener.onStatusChanged(finalTotalCars, finalAvailableSlots);
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.e(TAG, "❌ Error: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Status cancelled: " + error.getMessage());
            }
        };

        statusRef.addValueEventListener(statusValueListener);
        Log.d(TAG, "✅ Status listener attached");
    }

    public void setBarrierListener(BarrierStatusListener listener) {
        if (isDestroyed || barrierRef == null) return;

        this.barrierListener = listener;

        if (barrierValueListener != null) {
            barrierRef.removeEventListener(barrierValueListener);
        }

        barrierValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isDestroyed) return;

                for (DataSnapshot barrierSnapshot : snapshot.getChildren()) {
                    try {
                        String barrier = barrierSnapshot.getKey();
                        String status = barrierSnapshot.child("status").getValue(String.class);
                        String command = barrierSnapshot.child("command").getValue(String.class);

                        String finalStatus = (command != null) ? command :
                                (status != null) ? status : "UNKNOWN";

                        final String finalBarrier = barrier;
                        final String finalStatusStr = finalStatus;

                        if (mainHandler != null && barrierListener != null) {
                            mainHandler.post(() -> {
                                if (!isDestroyed && barrierListener != null && finalBarrier != null) {
                                    barrierListener.onBarrierStatusChanged(finalBarrier, finalStatusStr);
                                }
                            });
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Barrier cancelled: " + error.getMessage());
            }
        };

        barrierRef.addValueEventListener(barrierValueListener);
    }

    // ============= CẬP NHẬT DỮ LIỆU =============

    public void updateChoDo(int choDoId, String trangThai, String bienSo) {
        if (isDestroyed || choDoId < 1 || choDoId > 6 || choDoRef == null) {
            Log.w(TAG, "⚠️ Cannot update");
            return;
        }

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("trang_thai", trangThai != null ? trangThai.toLowerCase() : "trong");
            updates.put("bien_so", bienSo != null ? bienSo : "");
            updates.put("timestamp", System.currentTimeMillis());

            if ("dang_do".equalsIgnoreCase(trangThai)) {
                updates.put("thoi_gian_vao", System.currentTimeMillis());
            } else {
                updates.put("thoi_gian_vao", 0);
            }

            choDoRef.child(String.valueOf(choDoId)).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, String.format("✅ Updated chỗ %d: %s | %s",
                                choDoId, trangThai, bienSo));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updateChoDo: " + e.getMessage());
        }
    }

    public void setChoDoDangDo(int choDoId, String bienSo, long thoiGianVao) {
        if (isDestroyed || choDoId < 1 || choDoId > 6 || choDoRef == null) return;

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("trang_thai", "dang_do");
            updates.put("bien_so", bienSo);
            updates.put("thoi_gian_vao", thoiGianVao);
            updates.put("timestamp", System.currentTimeMillis());

            choDoRef.child(String.valueOf(choDoId)).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, String.format("✅ Set chỗ %d ĐANG ĐỖ | %s | Time: %d",
                                choDoId, bienSo, thoiGianVao));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
        }
    }

    public void setChoDoDangDo(int choDoId, String bienSo) {
        setChoDoDangDo(choDoId, bienSo, System.currentTimeMillis());
    }

    public void clearChoDo(int choDoId) {
        Log.d(TAG, "➡️ clearChoDo: " + choDoId);
        updateChoDo(choDoId, "trong", "");
    }

    public void updateSlot1WithBienSo(String bienSo) {
        if (isDestroyed || choDoRef == null) return;

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("trang_thai", "dang_do");
            updates.put("bien_so", bienSo);
            updates.put("thoi_gian_vao", System.currentTimeMillis());
            updates.put("timestamp", System.currentTimeMillis());

            choDoRef.child("1").updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Slot 1 updated: " + bienSo);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
        }
    }

    public void clearEvent(int slotNum) {
        if (isDestroyed || eventsRef == null) return;

        try {
            eventsRef.child("slot_" + slotNum).removeValue();
            eventsRef.child(String.valueOf(slotNum)).removeValue();
            Log.d(TAG, "✅ Cleared event: " + slotNum);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
        }
    }

    public void updateStatusOverview(int totalCars, int availableSlots) {
        if (isDestroyed || statusRef == null) return;

        try {
            Map<String, Object> status = new HashMap<>();
            status.put("total_cars", totalCars);
            status.put("available_slots", availableSlots);
            status.put("total_slots", 6);
            status.put("last_sync", System.currentTimeMillis());

            statusRef.updateChildren(status)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, String.format("✅ Status: %d/%d", totalCars, availableSlots));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
        }
    }

    // ============= BARRIER =============

    public void controlBarrier(String barrier, String command) {
        if (isDestroyed || barrierRef == null) return;

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("command", command);
            updates.put("status", command);
            updates.put("last_update", System.currentTimeMillis());

            barrierRef.child(barrier).updateChildren(updates);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
        }
    }

    public void openBarrierIn() {
        controlBarrier("in", "OPEN");
    }

    public void closeBarrierIn() {
        controlBarrier("in", "CLOSE");
    }

    public void openBarrierOut() {
        controlBarrier("out", "OPEN");
    }

    public void closeBarrierOut() {
        controlBarrier("out", "CLOSE");
    }

    // ============= CLEANUP =============

    public void destroy() {
        Log.d(TAG, "🔴 Destroying...");

        isDestroyed = true;

        try {
            if (choDoRef != null && choDoValueListener != null) {
                choDoRef.removeEventListener(choDoValueListener);
            }

            if (eventsRef != null && eventsValueListener != null) {
                eventsRef.removeEventListener(eventsValueListener);
            }

            if (barrierRef != null && barrierValueListener != null) {
                barrierRef.removeEventListener(barrierValueListener);
            }

            if (statusRef != null && statusValueListener != null) {
                statusRef.removeEventListener(statusValueListener);
            }

            choDoValueListener = null;
            eventsValueListener = null;
            barrierValueListener = null;
            statusValueListener = null;

            choDoListener = null;
            eventListener = null;
            barrierListener = null;
            statusListener = null;

            if (mainHandler != null) {
                mainHandler.removeCallbacksAndMessages(null);
                mainHandler = null;
            }

            Log.d(TAG, "✅ Destroyed");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
        }
    }
}