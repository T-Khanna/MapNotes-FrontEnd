package mapnotes.mapnotes.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.URI;
import java.util.Calendar;

import mapnotes.mapnotes.R;
import mapnotes.mapnotes.data_classes.Note;

public class AddNoteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final Note thisNote = new Note();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent i = getIntent();
        Location location = i.getParcelableExtra("location");

        if (location != null) {
            LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(marker));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
            thisNote.setLocation(marker);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng));
                thisNote.setLocation(latLng);
            }
        });

        final EditText title = findViewById(R.id.title);
        final EditText description = findViewById(R.id.description);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisNote.setTitle(title.getText().toString());
                thisNote.setDescription(description.getText().toString());
                Calendar cal = Calendar.getInstance();
                thisNote.setTime(cal.getTimeInMillis());
                cal.add(Calendar.HOUR_OF_DAY, 1);
                thisNote.setEndTime(cal.getTimeInMillis());

                if (thisNote.isValid()) {
                    Intent result = new Intent();
                    result.putExtra("note", thisNote);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            }
        });

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

}
