package mapnotes.mapnotes.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mapnotes.mapnotes.CustomClusterRenderer;
import mapnotes.mapnotes.DatePickerFragment;
import mapnotes.mapnotes.FilterDialog;
import mapnotes.mapnotes.LocationHistoryService;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.Server;
import mapnotes.mapnotes.data_classes.ClusteredMarker;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;
import mapnotes.mapnotes.data_classes.Time;
import mapnotes.mapnotes.data_classes.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback{

    private GoogleMap mMap;
    private ClusterManager<ClusteredMarker> clusterManager;
    private SeekBar timeSlider;
    private Server server;
    private TextView sliderText;
    private ImageView addNote;
    private TextView dateView;
    private final int REQUEST_ADD_NOTE = 34679;
    private final int REQUEST_EDIT_NOTE = 34680;
    private final int REQUEST_ACCESS_LOCATION = 0;
    private Marker lastMarker = null;
    private Map<Note, ClusteredMarker> notes = new HashMap<>();
    private DateAndTime selectedDate = null;
    private final boolean DEBUG = true;
    private SwipeRefreshLayout refresh;
    private GoogleSignInAccount login;
    private ImageView location;
    private List<String> filterTags = new LinkedList<>();
    private GoogleSignInClient googleSignInClient;
    private String TAG = MainActivity.class.getSimpleName();
    private int MAX_PROGRESS = 95;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
        FirebaseMessaging.getInstance().subscribeToTopic("HELLO");

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

        timeSlider.setMax(MAX_PROGRESS); //Number of 15 min intervals in a day
        Calendar cal = Calendar.getInstance();
        mapFragment.getMapAsync(this);

        selectedDate = new DateAndTime(cal);
        dateView = (TextView) findViewById(R.id.date_view);
        updateDateView();
        updateTimeView();

        ImageView currentTimeButton = findViewById(R.id.current_time_button);
        currentTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = new DateAndTime(Calendar.getInstance());
                getNotes(selectedDate);
                updateDateView();
                updateTimeView();
            }
        });

        View header = navigationView.getHeaderView(0);
        ImageView userPicture = header.findViewById(R.id.user_picture);
        Uri image = login.getPhotoUrl();
        if (image != null) {
            Picasso.with(this).load(image).into(userPicture);
        }
        TextView user_name = header.findViewById(R.id.user_name);
        user_name.setText(login.getDisplayName());

        //Send the current user to the FirebaseNotifications service
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEdit = pref.edit();
        prefEdit.putString("email", login.getEmail());
        prefEdit.commit();

        refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNotes(selectedDate);
            }
        });

        //Start background service
        startService(new Intent(this, LocationHistoryService.class));
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
            new FilterDialog(this, filterTags).setPositiveButton(new Function<List<String>>() {
                @Override
                public void run(List<String> input) {
                    filterTags = input;
                    filter();
                }
            }).show();
        } else if (id == R.id.nav_settings) {
            //Open the preferences activity
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_history) {
            String query = "api/notes/user/" + login.getEmail();
            server.getJSONRequest(query, null, new Function<JSONObject>() {
                @Override
                public void run(JSONObject input) {
                    try {
                        JSONArray notes = input.getJSONArray("Notes");
                        LinkedList<Note> noteList = new LinkedList<>();
                        for (int i = 0; i < notes.length(); i++) {
                            noteList.add(new Note(notes.getJSONObject(i)));
                        }

                        startHistoryIntent(noteList, "Your History");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Function<VolleyError>() {
                @Override
                public void run(VolleyError input) {
                    Toast toast = Toast.makeText(MainActivity.this, input.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        } else if (id == R.id.sign_out) {
            //Sign out button, sign out and return to login page
            Intent i = getIntent();
            GoogleSignInOptions gso = i.getParcelableExtra("googleSignInOptions");
            googleSignInClient = GoogleSignIn.getClient(this, gso);

            googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent i = new Intent(MainActivity.this, GoogleSignInActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startHistoryIntent(LinkedList<Note> noteList, String label) {
        //Open history activity
        Intent i = new Intent(MainActivity.this, HistoryActivity.class);
        i.putExtra("notes", noteList);
        i.putExtra("label", label);
        i.putExtra("loginEmail", login.getEmail());
        if (login.getPhotoUrl() != null) {
            i.putExtra("profile_picture", login.getPhotoUrl().toString());
        }
        i.putExtra("display_name", login.getDisplayName());
        i.putExtra("login_id", login.getIdToken());
        startActivity(i);
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

        //Get the notes and display them on the screen
        getNotes(selectedDate);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_ACCESS_LOCATION,
                    "Want location to allow you to easily see where you are");
        }

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        getLocation(new Function<Location>() {
            @Override
            public void run(Location input) {
                if (input != null) {
                    LatLng userLocation = new LatLng(input.getLatitude(), input.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            }
        });

        clusterManager = new ClusterManager<ClusteredMarker>(this, mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnInfoWindowClickListener(clusterManager);

        final CustomClusterRenderer renderer = new CustomClusterRenderer(this, mMap, clusterManager);

        clusterManager.setRenderer(renderer);

        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusteredMarker>() {
            @Override
            public void onClusterItemInfoWindowClick(ClusteredMarker marker) {
                Intent i = new Intent(MainActivity.this, NoteDisplayActivity.class);
                i.putExtra("loginEmail", login.getEmail());
                if (login.getPhotoUrl() != null) {
                    i.putExtra("profile_picture", login.getPhotoUrl().toString());
                }
                i.putExtra("display_name", login.getDisplayName());
                Note note = marker.getTag();
                i.putExtra("note", note);
                i.putExtra("login_id", login.getIdToken());
                clusterManager.removeItem(marker);
                startActivityForResult(i, REQUEST_EDIT_NOTE);
            }
        });

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusteredMarker>() {
            @Override
            public boolean onClusterClick(Cluster<ClusteredMarker> cluster) {
                float zoom = mMap.getCameraPosition().zoom;
                if (zoom >= 17.5) { //Between street and building level zoom
                    LinkedList<Note> noteList = new LinkedList<>();
                    for (ClusteredMarker marker : cluster.getItems()) {
                        noteList.add(marker.getTag());
                    }
                    startHistoryIntent(noteList, "Grouped Notes");
                }
                return false;
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

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng location = mMap.getCameraPosition().target;

                Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
                i.putExtra("location", location);
                startActivityForResult(i, REQUEST_ADD_NOTE);
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

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation(new Function<Location>() {
                    @Override
                    public void run(Location input) {
                        if (input != null) {
                            LatLng currentLoc = new LatLng(input.getLatitude(), input.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));
                        }
                    }
                });
            }
        });

        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) updateUI();
    }


    /**
     * Get the notes from the server available at a specified time period
     * @param date - The date and time (DateAndTime) to get the notes from the server
     */
    private void getNotes(DateAndTime date) {
        refresh.setRefreshing(true);
        try {
            String url = "api/notes/time/\"" + date.toString() + "\"";
            server.getJSONRequest(url, null, new Function<JSONObject>() {
                @Override
                public void run(JSONObject input) {
                    try {
                        if (input.has("Notes")) {
                            clusterManager.clearItems();
                            Map<Note, ClusteredMarker> newNotes = new HashMap<>();
                            JSONArray array = input.getJSONArray("Notes");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonNote = array.getJSONObject(i);
                                Note note = new Note(jsonNote);

                                boolean found = false;
                                if (filterTags == null || filterTags.size() == 0) found = true;
                                for (String tag : note.getTags()) {
                                    if (filterTags.contains(tag)) {
                                        found = true;
                                    }
                                }

                                if (found) {
                                    addNoteMarker(note, newNotes);
                                } else {
                                    newNotes.put(note, null);
                                }
                            }
                            clusterManager.cluster();
                            notes = newNotes;
                            refresh.setRefreshing(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Function<VolleyError>() {
                @Override
                public void run(VolleyError input) {
                    String message;
                    if (input.getMessage() == null || input.getMessage() == "") {
                        message = "Error connecting to server";
                    } else {
                        message = input.getMessage();
                    }
                    Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
                    refresh.setRefreshing(false);
                    toast.show();
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
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, request_code);
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
                HashSet<User> users = new HashSet<>();
                users.add(new User(login.getDisplayName(), login.getEmail(), 0));
                newNote.setUserEmail(users);
                JSONObject params = newNote.toJson();

                //Send new note to the server
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
                        if (input.has("Merge")) {
                            boolean hasAggregatedNotes = false;
                            try {
                                hasAggregatedNotes = input.getBoolean("Merge");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (hasAggregatedNotes) {
                                getNotes(selectedDate);
                            }
                        }

                        long startTime = newNote.getTime().toLong();
                        long currentTime = selectedDate.toLong();
                        if (startTime > currentTime + 60000 && startTime + 900000 > currentTime) { //If within a 15 minute slot after current selected time
                            int progress = timeSlider.getProgress();
                            if (progress < MAX_PROGRESS) {
                                timeSlider.setProgress(progress + 1);
                                Time selectedTime = new Time(getSelectedHour(timeSlider.getProgress()), getSelectedMinute(timeSlider.getProgress()));
                                selectedDate.setTime(selectedTime);
                                getNotes(selectedDate);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(newNote.getLocation()));
                            }
                        }

                        CharSequence text = "Event successfully added";
                        Toast toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });

                long startTime = newNote.getTime().toLong();
                long currentTime = selectedDate.toLong();
                long endTime = newNote.getEndTime().toLong();

                if (startTime <= currentTime + 60000 && currentTime <= endTime) {
                    addNoteMarker(newNote);
                    clusterManager.cluster();
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(newNote.getLocation()));
                }

                //Send a push notification to other users
                if (newNote.getTags().size() > 0) {
                    sendTopicUpdate(newNote.getTags(), newNote);
                }
            }
        } else if (requestCode == REQUEST_EDIT_NOTE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                final Note newNote = data.getParcelableExtra("note");
                addNoteMarker(newNote);
                clusterManager.cluster();
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

    private void updateTimeView() {
        int minute = selectedDate.getTime().getMinute() / 15;
        timeSlider.setProgress(selectedDate.getTime().getHourOfDay() * 4 + minute);
    }

    /**
     * Tell other users that there is a new note with a tag that they are subscribed to
     *
     * @param tags - the topics to send the message to
     * @param note - the note to send a notification for
     */
    private void sendTopicUpdate(Set<String> tags, Note note) {
        JSONObject obj = new JSONObject();
        try {
            String conditions = "";
            for (String tag : tags) {
                conditions += "\'" + tag + "\' in topics || ";
            }
            conditions = conditions.substring(0, conditions.length() - 4);
            obj.put("condition", conditions);
            JSONObject data = new JSONObject();
            data.put("latitude", note.getLocation().latitude + "");
            data.put("longitude", note.getLocation().longitude + "");
            data.put("user", login.getEmail());
            data.put("start_time", note.getTime().toLong() + "");
            data.put("end_time", note.getEndTime().toLong() + "");
            obj.put("data", data);
            server.postToTopic(obj, new Function<JSONObject>() {
                @Override
                public void run(JSONObject input) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Only show notes in filterTags.
     */
    private void filter() {
        getNotes(selectedDate);
    }

    private ClusteredMarker addNoteMarker(Note note) {
        return addNoteMarker(note, notes);
    }

    private ClusteredMarker addNoteMarker(Note note, Map<Note, ClusteredMarker> notes) {
        //Marker marker = mMap.addMarker(new MarkerOptions().position(note.getLocation()).title(note.getTitle()));
        //marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_note));
        ClusteredMarker newMarker = new ClusteredMarker(note.getLocation().latitude, note.getLocation().longitude, note.getTitle(), null);
        clusterManager.addItem(newMarker);
        newMarker.setTag(note);

        //Add note to class variable notes
        notes.put(note, newMarker);
        return newMarker;
    }
}
