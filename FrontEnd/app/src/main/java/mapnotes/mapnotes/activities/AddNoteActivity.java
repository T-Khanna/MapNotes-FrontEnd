package mapnotes.mapnotes.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import mapnotes.mapnotes.DatePickerFragment;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.TimePickerFragment;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;
import mapnotes.mapnotes.data_classes.Time;

public class AddNoteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Note thisNote = new Note();
    private TextView startTime;
    private TextView endTime;
    private TextView startDate;
    private TextView endDate;
    private List<String> tags = new LinkedList<>();

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

        //Initial UI settings
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //Try and find location to zoom into and set initial marker
        Intent i = getIntent();
        Location location = null;
        if (i.hasExtra("location")) {
            location = i.getParcelableExtra("location");
        }

        //Set up local variables
        Calendar cal = Calendar.getInstance();
        thisNote.setTime(new DateAndTime(cal));

        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        thisNote.setEndTime(new DateAndTime(cal));

        //Set UI listeners
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
                cal.add(Calendar.HOUR_OF_DAY, 1);

                if (thisNote.isValid()) {
                    Intent result = new Intent();
                    result.putExtra("note", thisNote);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            }
        });

        ImageView cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        startTime = findViewById(R.id.start_time);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                Bundle arguments = new Bundle();
                arguments.putSerializable("callback", new Function<Time>() {
                    @Override
                    public void run(Time input) {
                        thisNote.getTime().setTime(input);
                        updateTimes(startDate, startTime, thisNote.getTime());
                    }
                });
                newFragment.setArguments(arguments);
                newFragment.show(getFragmentManager(), "timepicker");
            }
        });

        endTime = findViewById(R.id.end_time);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                Bundle arguments = new Bundle();
                arguments.putSerializable("callback", new Function<Time>() {
                    @Override
                    public void run(Time input) {
                        thisNote.getEndTime().setTime(input);
                        updateTimes(endDate, endTime, thisNote.getEndTime());
                    }
                });
                newFragment.setArguments(arguments);
                newFragment.show(getFragmentManager(), "timepicker");
            }
        });

        startDate = findViewById(R.id.start_date);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arguments = new Bundle();
                arguments.putSerializable("callback", new Function<Date>() {
                    @Override
                    public void run(Date input) {
                        thisNote.getTime().setDate(input);
                        updateTimes(startDate, startTime, thisNote.getTime());
                    }
                });
                newFragment.setArguments(arguments);
                newFragment.show(getFragmentManager(), "timepicker");
            }
        });

        endDate = findViewById(R.id.end_date);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arguments = new Bundle();
                arguments.putSerializable("callback", new Function<Date>() {
                    @Override
                    public void run(Date input) {
                        thisNote.getEndTime().setDate(input);
                        updateTimes(endDate, endTime, thisNote.getEndTime());
                    }
                });
                newFragment.setArguments(arguments);
                newFragment.show(getFragmentManager(), "timepicker");
            }
        });


        //Tags
        final EditText tagText = findViewById(R.id.add_tag_text);
        Button addTag = findViewById(R.id.add_tag);
        final TagContainerLayout tagContainerLayout = findViewById(R.id.tag_view);

        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTag = tagText.getText().toString().trim();
                if (thisNote.addTag(newTag)) {
                    tags.add(newTag);
                }
                tagContainerLayout.setTags(tags);
            }
        });

        tagContainerLayout.setOnTagClickListener(new TagView.OnTagClickListener() {

            @Override
            public void onTagClick(int position, String text) {

            }

            @Override
            public void onTagLongClick(int position, String text) {

            }

            @Override
            public void onTagCrossClick(int position) {
                String tagToRemove = tags.get(position);
                thisNote.removeTag(tagToRemove);
                Log.d(AddNoteActivity.class.getSimpleName(), "Removing tag: " + tagToRemove);
                tags.remove(position);
                tagContainerLayout.removeTag(position);
            }
        });

        //Check if we are editing a note
        if (i.hasExtra("editNote")) {
            thisNote = i.getParcelableExtra("editNote");
            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(thisNote.getLocation().latitude);
            location.setLongitude(thisNote.getLocation().longitude);

            title.setText(thisNote.getTitle());
            description.setText(thisNote.getDescription());

            tags = new LinkedList<>(thisNote.getTags());
            tagContainerLayout.setTags(tags);
        }


        updateTimes(startDate, startTime, thisNote.getTime());
        updateTimes(endDate, endTime, thisNote.getEndTime());

        if (location != null) {
            LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(marker));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15));
            thisNote.setLocation(marker);
            updateUI();
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

    private void updateTimes(TextView dateView, TextView timeView, DateAndTime time) {
        Date d = time.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM dd, YYYY");
        String date = dateFormat.format(d);
        dateView.setText(date);

        timeView.setText(time.getTime().toString());
    }

}
