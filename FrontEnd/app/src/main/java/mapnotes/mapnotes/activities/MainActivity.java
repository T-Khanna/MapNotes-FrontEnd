package mapnotes.mapnotes.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mapnotes.mapnotes.DatePickerFragment;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.Server;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;
import mapnotes.mapnotes.data_classes.Time;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private SeekBar timeSlider;
    private Server server;
    private TextView sliderText;
    private ImageView addNote;
    private TextView dateView;
    private final int REQUEST_ADD_NOTE = 34679;
    private final int REQUEST_EDIT_NOTE = 34680;
    private final int REQUEST_ACCESS_LOCATION = 0;
    private Marker lastMarker = null;
    private Map<Note, Marker> notes = new HashMap<>();
    private DateAndTime selectedDate = null;
    private final boolean DEBUG = true;
    private SwipeRefreshLayout refresh;
    private GoogleSignInAccount login;
    private ImageView location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent i = getIntent();
        login = i.getParcelableExtra("googleSignIn");
        server = new Server(this, login.getIdToken());
        timeSlider = (SeekBar) findViewById(R.id.time_slider);
        sliderText = (TextView) findViewById(R.id.time_text);
        sliderText.setVisibility(View.GONE);
        addNote = (ImageView) findViewById(R.id.add_note);
        location = (ImageView) findViewById(R.id.location_icon);

        timeSlider.setMax(95); //Number of 15 min intervals in a day
        Calendar cal = Calendar.getInstance();
        timeSlider.setProgress(cal.get(Calendar.HOUR_OF_DAY) * 4);
        mapFragment.getMapAsync(this);

        selectedDate = new DateAndTime(cal);
        dateView = (TextView) findViewById(R.id.date_view);
        updateDateView();

        View header = navigationView.getHeaderView(0);
        ImageView userPicture = header.findViewById(R.id.user_picture);
        Uri image = login.getPhotoUrl();
        if (image != null) {
            Picasso.with(this).load(image).into(userPicture);
        }
        TextView user_name = header.findViewById(R.id.user_name);
        user_name.setText(login.getDisplayName());

        getNotes(selectedDate);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_filter) {
            // Handle the tag filtering
        } else if (id == R.id.nav_settings) {
            //Open the preferences activity
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

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
                Time selectedTime = new Time(getSelectedHour(seekBar.getProgress()), getSelectedMinute(seekBar.getProgress()));
                selectedDate.setTime(selectedTime);
                getNotes(selectedDate);
                sliderText.setVisibility(View.GONE);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(lastMarker)) {
                    Intent i = new Intent(MainActivity.this, NoteDisplayActivity.class);
                    i.putExtra("loginEmail", login.getEmail());
                    Note note = (Note) marker.getTag();
                    i.putExtra("note", note);
                    if (DEBUG) Log.d(MainMapsActivity.class.getSimpleName(), "Starting note display");
                    marker.remove();
                    startActivityForResult(i, REQUEST_EDIT_NOTE);
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
                        Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
                        i.putExtra("location", input);
                        startActivityForResult(i, REQUEST_ADD_NOTE);
                    }
                });
            }
        });

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arguments = new Bundle();
                arguments.putSerializable("callback", new Function<Date>() {
                    @Override
                    public void run(Date input) {
                        selectedDate.setDate(input);
                        getNotes(selectedDate);
                        updateDateView();
                    }
                });
                newFragment.setArguments(arguments);
                newFragment.show(getFragmentManager(), "timepicker");
            }
        });

        refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNotes(selectedDate);
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation(new Function<Location>() {
                    @Override
                    public void run(Location input) {
                        LatLng currentLoc = new LatLng(input.getLatitude(), input.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));
                    }
                });
            }
        });

    }


    /**
     * Get the notes from the server available at a specified time period
     * @param date - The date and time (DateAndTime) to get the notes from the server
     */
    private void getNotes(DateAndTime date) {
        try {
            String url = "api/notes/\"" + date.toString() + "\"";
            if (DEBUG) Log.d(MainMapsActivity.class.getSimpleName(), "Getting notes at: " + selectedDate.toString());
            server.getJSONRequest(url, null, new Function<JSONObject>() {
                @Override
                public void run(JSONObject input) {
                    try {
                        if (input.has("Notes")) {
                            mMap.clear();
                            Map<Note, Marker> newNotes = new HashMap<>();
                            JSONArray array = input.getJSONArray("Notes");
                            if (DEBUG) Log.d(MainMapsActivity.class.getSimpleName(), "Received " + array.length() + " notes from server");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonNote = array.getJSONObject(i);
                                Note note = new Note(jsonNote);
                                Marker marker = mMap.addMarker(new MarkerOptions().position(note.getLocation()).title(note.getTitle()));
                                marker.setTag(note);
                                newNotes.put(note, marker);
                            }
                            notes = newNotes;
                            refresh.setRefreshing(false);
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
                newNote.setUserEmail(login.getEmail());
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

                Marker marker = mMap.addMarker(new MarkerOptions().position(newNote.getLocation()).title(newNote.getTitle()));
                marker.setTag(newNote);
                notes.put(newNote, marker);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newNote.getLocation()));
            }
        } else if (requestCode == REQUEST_EDIT_NOTE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                final Note newNote = data.getParcelableExtra("note");

                Marker marker = mMap.addMarker(new MarkerOptions().position(newNote.getLocation()).title(newNote.getTitle()));
                marker.setTag(newNote);

                //Add note to class variable notes
                notes.put(newNote, marker);
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
            location.setVisibility(View.VISIBLE);
        } else {
            mMap.setMyLocationEnabled(false);
            location.setVisibility(View.INVISIBLE);
        }
    }

    private void updateDateView() {
        Date d = selectedDate.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM dd, YYYY");
        String date = dateFormat.format(d);
        dateView.setText(date);
    }

    /**
     * Given a list of tags to filter by, only show notes that have those tags
     * @param filterTags
     */
    private void filter(List<String> filterTags) {
        Map<Note, Marker> newNotes = new HashMap<>();
        for (Map.Entry entry : notes.entrySet()) {
            Note note = (Note) entry.getKey();
            boolean found = false;
            for (String tag : filterTags) {
                if (note.hasTag(tag)) {
                    found = true;
                    break;
                }
            }
            Marker marker = (Marker) entry.getValue();
            if (!found) {
                //If we didn't find a valid tag for filtering, remove marker
                if (marker != null) {
                    marker.remove();
                }
                newNotes.put(note, null);
            } else {
                //If we should show this note, make sure the marker for it exists
                if (marker == null) {
                    marker = mMap.addMarker(new MarkerOptions().position(note.getLocation()).title(note.getTitle()));
                    marker.setTag(note);
                }
                newNotes.put(note, marker);
            }
        }
        notes = newNotes;
    }

}
