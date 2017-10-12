package mapnotes.mapnotes.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;

import mapnotes.mapnotes.R;
import mapnotes.mapnotes.Server;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;

public class MainMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SeekBar timeSlider;
    private Server server;
    private TextView sliderText;
    private ImageView addNote;
    private final int REQUEST_ADD_NOTE = 34679;
    private final int REQUEST_ACCESS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        timeSlider = (SeekBar) findViewById(R.id.time_slider);
        sliderText = (TextView) findViewById(R.id.time_text);
        sliderText.setVisibility(View.GONE);
        addNote = findViewById(R.id.add_note);

        timeSlider.setMax(95); //Number of 15 min intervals in a day
        Calendar cal = Calendar.getInstance();
        timeSlider.setProgress(cal.get(Calendar.HOUR_OF_DAY) * 4);
        mapFragment.getMapAsync(this);

        server = new Server(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Initialise Map
        //Disable Map Toolbar:
        mMap.getUiSettings().setMapToolbarEnabled(false);

        timeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sliderText.setText(timeOf(i));
                //TODO: This is very hacky way of screen detection, find better way of doing this
                if (i <= 91 && i >= 3) {
                    int x = timeSlider.getThumb().getBounds().right;
                    int width = sliderText.getWidth() / 2;
                    sliderText.setX(x - width);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sliderText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Request new locations
                server.getStringRequest("", new Function<String>() {
                    @Override
                    public void run(String input) {
                        new AlertDialog.Builder(MainMapsActivity.this).setMessage("Got response from server: " + input).create().show();
                    }
                });
                sliderText.setVisibility(View.GONE);
            }
        });

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation(new Function<Location>() {
                    @Override
                    public void run(Location input) {
                        Intent i = new Intent(MainMapsActivity.this, AddNoteActivity.class);
                        i.putExtra("location", input);
                        startActivityForResult(i, REQUEST_ADD_NOTE);
                    }
                });
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private String timeOf(int i) {
        int hour = i / 4;
        int minute = (i % 4) * 15;
        String hourText = hour < 10 ? "0" + hour : String.valueOf(hour);
        String minText = minute < 10 ? "0" + minute : String.valueOf(minute);
        return hourText + ":" + minText;
    }

    /**
     * A function which passes the users last known location to its callback paramater.
     * This function does not currently request the permission at runtime.
     * @param callback - function which runs once location has or has not been found, needs to
     *                 perform its own null check and handle accordingly
     */
    public void getLocation(final Function<Location> callback) {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            callback.run(location);
                        }
                    });
        } else {
            callback.run(null);
        }

    }

    public void requestPermission(final String permission, final int request_code, final String rationale) {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission)) {

            // Show an explanation to the user *asynchronously* -- don't block


            new AlertDialog.Builder(this).setMessage(rationale)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermission(permission, request_code, rationale);
                        }
                    }).setTitle("Requesting Permission").create().show();

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    request_code);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ADD_NOTE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Note newNote = data.getParcelableExtra("note");

                mMap.addMarker(new MarkerOptions().position(newNote.getLocation()).title(newNote.getTitle()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newNote.getLocation()));
            }
        }
    }
}
