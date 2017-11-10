package mapnotes.mapnotes.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import co.lujun.androidtagview.TagContainerLayout;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;

public class NoteDisplayActivity extends FragmentActivity {

    private Note thisNote;
    private static final int REQUEST_EDIT_NOTE = 234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Intent i = getIntent();
        thisNote = i.getParcelableExtra("note");
        String loginEmail = i.getStringExtra("loginEmail");

        ImageView editButton = findViewById(R.id.edit_button);
        if (thisNote.getUserEmail().equals(loginEmail)) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(NoteDisplayActivity.this, AddNoteActivity.class);
                    i.putExtra("editNote", thisNote);
                    startActivityForResult(i, REQUEST_EDIT_NOTE);
                }
            });
        } else {
            editButton.setVisibility(View.GONE);
        }

        initialise();
    }


    /**
     *
     * Initialise the UI
     */
    public void initialise() {
        //Initial UI settings

        //Try and find location to zoom into and set initial marker

        if (thisNote != null) {
            Geocoder geocoder;
            final LatLng location = thisNote.getLocation();
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                TextView locationText = findViewById(R.id.location_text);
                if (addresses != null) {
                    final String address;
                    if (addresses.size() > 0) {
                        Address returnedAddress = addresses.get(0);
                        address = returnedAddress.getAddressLine(0);
                    } else {
                        address = location.latitude + ", " + location.longitude;
                    }
                    locationText.setText(address);
                    locationText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Create a Uri from an intent string. Use the result to create an Intent.
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address);

                            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            // Make the Intent explicit by setting the Google Maps package
                            mapIntent.setPackage("com.google.android.apps.maps");

                            // Attempt to start an activity that can handle the Intent
                            startActivity(mapIntent);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Tags
            TagContainerLayout tagContainerLayout = findViewById(R.id.tag_view);
            tagContainerLayout.setTags(new LinkedList<String>(thisNote.getTags()));
        } else {
            finish();
        }

        TextView title = findViewById(R.id.title);
        title.setText(thisNote.getTitle());

        TextView description = findViewById(R.id.description);
        description.setText(thisNote.getDescription());

        ImageView cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent result = new Intent();
                result.putExtra("note", thisNote);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        //Display the times
        TextView startDate = findViewById(R.id.start_date);
        TextView startTime = findViewById(R.id.start_time);

        TextView endDate = findViewById(R.id.end_date);
        TextView endTime = findViewById(R.id.end_time);

        updateTimes(startDate, startTime, thisNote.getTime());
        updateTimes(endDate, endTime, thisNote.getEndTime());


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateTimes(TextView dateView, TextView timeView, DateAndTime time) {
        Date d = time.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM dd, YYYY");
        String date = dateFormat.format(d);
        dateView.setText(date);

        timeView.setText(time.getTime().toString());
    }

    @Override
    public void onPause() {
        Intent result = new Intent();
        result.putExtra("note", thisNote);
        setResult(Activity.RESULT_OK, result);
        super.onPause();
    }

    @Override
    public void onBackPressed()
    {
        Intent result = new Intent();
        result.putExtra("note", thisNote);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_EDIT_NOTE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                thisNote = data.getParcelableExtra("note");
                initialise();

                //TODO: Add server update
            }
        }
    }
}
