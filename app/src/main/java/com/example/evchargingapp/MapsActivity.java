package com.example.evchargingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.evchargingapp.databinding.ActivityMapsBinding;
import com.example.evchargingapp.network.OpenChargeMapService;
import com.example.evchargingapp.network.POIResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Open Charge Map API
    private static final String OCM_BASE_URL = "https://api.openchargemap.io/";
    private static final String OCM_API_KEY  = "d41d2c61-ddb2-4630-b732-8ee5a4959a76";

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private OpenChargeMapService ocmService;
    private Map<Marker, ChargingStation> markerStationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrofit + GSON ile Open Charge Map servisini hazırla
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OCM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ocmService = retrofit.create(OpenChargeMapService.class);

        // Harita fragment’ini başlat
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Konum izni kontrolü
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // İzmir merkezinden 20 km içinde 50 istasyon çek
        ocmService.getPOIs(
                "json",
                38.423734,    // İzmir enlem
                27.142826,    // İzmir boylam
                20,           // km
                "KM",
                50,
                OCM_API_KEY
        ).enqueue(new Callback<List<POIResponse>>() {
            @Override
            public void onResponse(Call<List<POIResponse>> call, Response<List<POIResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "OCM response failure");
                    return;
                }
                List<POIResponse> pois = response.body();
                Log.d(TAG, "POI count: " + pois.size());

                // Marker’ları ekle
                for (POIResponse poi : pois) {
                    POIResponse.AddressInfo info = poi.getAddressInfo();
                    LatLng loc = new LatLng(info.getLatitude(), info.getLongitude());

                    ChargingStation station = new ChargingStation(
                            String.valueOf(poi.getId()),
                            info.getTitle(),
                            info.getLatitude(),
                            info.getLongitude(),
                            true,
                            0.0,
                            "kWh"
                    );

                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .title(station.getName())
                            .snippet("Rezervasyon için tıkla"));
                    markerStationMap.put(marker, station);
                }

                // Kamera tüm marker’ları görecek şekilde ayarla
                if (!markerStationMap.isEmpty()) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markerStationMap.keySet()) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                }

                // Marker’a tıklayınca Bottom Sheet göster
                mMap.setOnMarkerClickListener(marker -> {
                    ChargingStation st = markerStationMap.get(marker);
                    if (st != null) {
                        showStationBottomSheet(st);
                        return true;
                    }
                    return false;
                });
            }

            @Override
            public void onFailure(Call<List<POIResponse>> call, Throwable t) {
                Log.e(TAG, "OCM API error", t);
            }
        });
    }

    private void showStationBottomSheet(ChargingStation station) {
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_station, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheet);

        TextView tvName   = sheet.findViewById(R.id.tvStationName);
        TextView tvPrice  = sheet.findViewById(R.id.tvStationPrice);
        TextView tvStatus = sheet.findViewById(R.id.tvStationStatus);
        Button btnReserve = sheet.findViewById(R.id.btnReserve);

        tvName.setText(station.getName());
        tvPrice.setText("Fiyat: " + station.getPricePerKwh() + "₺/" + station.getPricingType());
        tvStatus.setText("Durum: " + (station.isAvailable() ? "Müsait" : "Dolu"));

        btnReserve.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, ReservationActivity.class);
            intent.putExtra("stationId", station.getId());
            intent.putExtra("stationName", station.getName());
            intent.putExtra("stationPrice", station.getPricePerKwh());
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this, "Konum izni reddedildi.", Toast.LENGTH_SHORT).show();
        }
    }
}
