package mapnotes.mapnotes.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import mapnotes.mapnotes.R;

public class SelectLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng eventLocation;
    private TextView locationText;
    private RelativeLayout bottomLayout;
    private String actualLocation = null;
    private LatLng latLng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        eventLocation = intent.getParcelableExtra("location");

        locationText = (TextView) findViewById(R.id.LocationText);
        bottomLayout = (RelativeLayout) findViewById(R.id.bottomLayout);
        bottomLayout.setVisibility(View.GONE);

        Button useButton = (Button) findViewById(R.id.UseButton);
        useButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                if (actualLocation != null) {
                    data.putExtra("location", latLng);
                    data.putExtra("address", actualLocation);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));

        }

        if (eventLocation != null) {
            mMap.addMarker(new MarkerOptions().position(eventLocation));
            getAddress(eventLocation);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng));
                getAddress(latLng);
            }
        });

        updateUI();
    }

    private void getAddress(LatLng latLng) {
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        this.latLng = latLng;
        if (latitude != 0 && longitude != 0) {
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    actualLocation = address;
                    locationText.setText(address);
                    bottomLayout.setVisibility(View.VISIBLE);
                } else {
                    actualLocation = latLng.latitude + ", " + latLng.longitude;
                    locationText.setText(actualLocation);
                    bottomLayout.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                actualLocation = latLng.latitude + ", " + latLng.longitude;
                locationText.setText(actualLocation);
                bottomLayout.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(getApplicationContext(), "latitude and longitude are null", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            updateUI();
        }
    }

    private void updateUI() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }
}
