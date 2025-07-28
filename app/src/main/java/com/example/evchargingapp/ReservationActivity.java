package com.example.evchargingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReservationActivity extends AppCompatActivity {
    private static final String TAG = "ReservationActivity";

    private FirebaseFirestore db;
    private String userId;
    private String stationId;
    private String stationName;

    private RecyclerView rvDatePicker;
    private RecyclerView rvTimeSlots;

    private DateAdapter dateAdapter;
    private TimeSlotAdapter slotAdapter;

    private List<Long> dateList     = new ArrayList<>();
    private List<TimeSlot> allSlots = new ArrayList<>();
    private List<TimeSlot> filtered = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // Firestore ve kullanıcı
        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Intent’ten istasyon bilgilerini al
        Intent intent = getIntent();
        stationId   = intent.getStringExtra("stationId");
        stationName = intent.getStringExtra("stationName");

        // 1) Yatay gün seçici RecyclerView
        rvDatePicker = findViewById(R.id.rvDatePicker);
        rvDatePicker.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        generateDateList();
        dateAdapter = new DateAdapter(dateList, dateMillis -> {
            filterSlotsForDate(dateMillis);
        });
        rvDatePicker.setAdapter(dateAdapter);

        // 2) Zaman dilimleri RecyclerView
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(this));

        // 3) Tüm slotları oluştur
        allSlots = generateTimeSlots(7, 15);

        // 4) Mevcut rezervasyonları çek, slot’ları işaretle, ardından bugünün slot’larını göster
        db.collection("reservations")
                .whereEqualTo("stationId", stationId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Reservation r = doc.toObject(Reservation.class);
                        long rs = r.getStartTime();
                        long re = r.getEndTime();
                        for (TimeSlot s : allSlots) {
                            if (rs < s.getEndMillis() && re > s.getStartMillis()) {
                                s.setAvailable(false);
                            }
                        }
                    }
                    // Başlangıçta ilk günü göster
                    filterSlotsForDate(dateList.get(0));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Rezervasyonlar yüklenemedi", e);
                    // Hata olsa bile bugünün slotlarını göster
                    filterSlotsForDate(dateList.get(0));
                });
    }

    /** 7 gün için tarih listesi oluştur */
    private void generateDateList() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < 7; i++) {
            dateList.add(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /** Seçilen güne göre filtered listesi oluşturup RecyclerView’e ata */
    private void filterSlotsForDate(long dateMillis) {
        filtered.clear();
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(dateMillis);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE,      0);
        start.set(Calendar.SECOND,      0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 1);

        for (TimeSlot s : allSlots) {
            if (s.getStartMillis() >= start.getTimeInMillis()
                    && s.getEndMillis()   <= end.getTimeInMillis()) {
                filtered.add(s);
            }
        }

        slotAdapter = new TimeSlotAdapter(filtered, stationId, userId, db);
        rvTimeSlots.setAdapter(slotAdapter);
    }

    /**
     * Gelecek `days` gün için `slotLengthMinutes` dakikalık
     * bloklar oluşturan yardımcı metod.
     */
    private List<TimeSlot> generateTimeSlots(int days, int slotLengthMinutes) {
        List<TimeSlot> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);

        // Dakikayı slotLengthMinutes’e yuvarla
        int minute = cal.get(Calendar.MINUTE);
        int mod    = minute % slotLengthMinutes;
        cal.add(Calendar.MINUTE, -mod);

        int totalSlots = days * (24 * 60 / slotLengthMinutes);
        for (int i = 0; i < totalSlots; i++) {
            long start = cal.getTimeInMillis();
            cal.add(Calendar.MINUTE, slotLengthMinutes);
            long end   = cal.getTimeInMillis();
            list.add(new TimeSlot(start, end, true));
        }
        return list;
    }
}
