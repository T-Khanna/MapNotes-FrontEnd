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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mapnotes.mapnotes.NoteDisplayActivity;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.Server;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;
import mapnotes.mapnotes.data_classes.Time;

public class MainMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SeekBar timeSlider;
    private Server server;
    private TextView sliderText;
    private ImageView addNote;
    private final int REQUEST_ADD_NOTE = 34679;
    private final int REQUEST_ACCESS_LOCATION = 0;
    private Marker lastMarker = null;
    private Map<LatLng, Note> notes;
    private DateAndTime selectedDate = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        server = new Server(this);
        timeSlider = (SeekBar) findViewById(R.id.time_slider);
        sliderText = (TextView) findViewById(R.id.time_text);
        sliderText.setVisibility(View.GONE);
        addNote = findViewById(R.id.add_note);

        timeSlider.setMax(95); //Number of 15 min intervals in a day
        Calendar cal = Calendar.getInstance();
        timeSlider.setProgress(cal.get(Calendar.HOUR_OF_DAY) * 4);
        mapFragment.getMapAsync(this);

        selectedDate = new DateAndTime(cal);

        getNotes(selectedDate);

        generateNotes();
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

        getLocation(new Function<Location>() {
            @Override
            public void run(Location input) {
                if (input != null) {
                    LatLng userLocation = new LatLng(input.getLatitude(), input.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    updateUI();
                }
            }
        });

        timeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sliderText.setText(timeOf(i));
                int x = timeSlider.getThumb().getBounds().right;
                int width = sliderText.getWidth() / 4;
                sliderText.setX(x - width);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sliderText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Request new locations, sending the time required in UTC format
                Time selectedTime = new Time(getSelectedHour(seekBar.getProgress()), seekBar.getProgress());
                selectedDate.setTime(selectedTime);
                getNotes(selectedDate);
                sliderText.setVisibility(View.GONE);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(lastMarker)) {
                    Intent i = new Intent(MainMapsActivity.this, NoteDisplayActivity.class);
                    Note note = notes.get(marker.getPosition());
                    i.putExtra("note", note);
                    System.out.println("Starting note display");
                    startActivity(i);
                }
                lastMarker = marker;
                return false;
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
    }

    private  void getNotes(DateAndTime date) {
        try {
            String url = "api/notes/\"" + date.toString() + "\"";
            server.getJSONRequest(url, null, new Function<JSONObject>() {
                @Override
                public void run(JSONObject input) {
                    System.out.println(input);
                    try {
                        if (input.has("Notes")) {
                            mMap.clear();
                            Map<LatLng, Note> newNotes = new HashMap<>();
                            JSONArray array = input.getJSONArray("Notes");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonNote = array.getJSONObject(i);
                                Note note = new Note(jsonNote);
                                newNotes.put(note.getLocation(), note);
                                mMap.addMarker(new MarkerOptions().position(note.getLocation()).title(note.getTitle()));
                            }
                            notes = newNotes;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSelectedHour(int i) {
        return i / 4;
    }

    private int getSelectedMinute(int i) {
        return (i % 4) * 15;
    }

    private String timeOf(int i) {
        int hour = getSelectedHour(i);
        int minute = getSelectedMinute(i);
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

    /**
     * Request a permission via a dialog box, provide information as to why permission is needed
     *
     * @param permission - The permission string (usually from Manifest.permission.(permission))
     * @param request_code - A unique request code for a permission, so on receiving result the
     *                     program knows which permission was granted
     * @param rationale - A message to the user so that they know why the permission is needed
     */
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
                final Note newNote = data.getParcelableExtra("note");
                JSONObject params = newNote.toJson();
                server.postJSONRequest("api/notes", params, new Function<JSONObject>() {
                        @Override
                        public void run(JSONObject input) {
                            if (input.has("Id")) {
                                try {
                                    newNote.setId(input.getInt("Id"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                });
                //Add note to class variable notes
                notes.put(newNote.getLocation(), newNote);

                mMap.addMarker(new MarkerOptions().position(newNote.getLocation()).title(newNote.getTitle()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newNote.getLocation()));
            }
        }
    }

    /**
     * Check important permissions and update UI
     */
    private void updateUI() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    /**
     * To be expanded, will take response from server to generate a map of the notes.
     */
    private void generateNotes() {
        Map<LatLng, Note> newNotes = new HashMap<>();


        notes = newNotes;
    }
}
