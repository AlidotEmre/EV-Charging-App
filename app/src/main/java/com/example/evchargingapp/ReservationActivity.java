package com.example.evchargingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ReservationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private DatePicker startDatePicker, endDatePicker;
    private TimePicker startTimePicker, endTimePicker;
    private Button btnConfirmReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // ── Mevcut kod: Firestore ve Intent verilerini alma
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String stationId    = intent.getStringExtra("stationId");
        String stationName  = intent.getStringExtra("stationName");
        double stationPrice = intent.getDoubleExtra("stationPrice", 0.0);
        String userId       = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ── Mevcut kod: UI öğelerini bağlama
        startDatePicker       = findViewById(R.id.startDatePicker);
        startTimePicker       = findViewById(R.id.startTimePicker);
        endDatePicker         = findViewById(R.id.endDatePicker);
        endTimePicker         = findViewById(R.id.endTimePicker);
        btnConfirmReservation = findViewById(R.id.btnConfirmReservation);

        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);

        // ── **Buraya** onClickListener bloğunu ekleyin:
        btnConfirmReservation.setOnClickListener(v -> {
            // Zaman hesaplama
            Calendar startCal = Calendar.getInstance();
            startCal.set(
                    startDatePicker.getYear(),
                    startDatePicker.getMonth(),
                    startDatePicker.getDayOfMonth(),
                    startTimePicker.getHour(),
                    startTimePicker.getMinute()
            );
            long startMillis = startCal.getTimeInMillis();

            Calendar endCal = Calendar.getInstance();
            endCal.set(
                    endDatePicker.getYear(),
                    endDatePicker.getMonth(),
                    endDatePicker.getDayOfMonth(),
                    endTimePicker.getHour(),
                    endTimePicker.getMinute()
            );
            long endMillis = endCal.getTimeInMillis();

            // Firestore kaydı
            DocumentReference docRef = db.collection("reservations").document();
            String reservationId = docRef.getId();
            Reservation reservation = new Reservation(
                    reservationId,
                    userId,
                    stationId,
                    startMillis,
                    endMillis
            );

            docRef.set(reservation)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Rezervasyon kaydedildi!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Kaydetme hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // ── onCreate metodunun kapanış parantezi burada
    }

}
