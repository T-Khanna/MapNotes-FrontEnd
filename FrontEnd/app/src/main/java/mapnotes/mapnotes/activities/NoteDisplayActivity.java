package mapnotes.mapnotes.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import co.lujun.androidtagview.TagContainerLayout;
import mapnotes.mapnotes.CommentAdapter;
import mapnotes.mapnotes.HistoryAdapter;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.data_classes.Comment;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;

public class NoteDisplayActivity extends FragmentActivity {

    private RecyclerView mRecyclerView;
    private CommentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Note thisNote;
    private static final int REQUEST_EDIT_NOTE = 234;
    private Uri profilePicture;
    private String displayName;
    private View overallView;
    private LinkedList<Comment> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        final Intent i = getIntent();
        thisNote = i.getParcelableExtra("note");
        String loginEmail = i.getStringExtra("loginEmail");

        ImageView editButton = findViewById(R.id.edit_button);
        if (thisNote.userIsMemberOf(loginEmail)) {
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

        overallView = findViewById(R.id.full_note_view);

        profilePicture =  Uri.parse(i.getStringExtra("profile_picture"));
        displayName = i.getStringExtra("display_name");

        //Create list of comments
        comments = new LinkedList<>();
        //TODO: Get comments from the server and populate when ready
        comments.add(new Comment("Thomas Allerton", "Testing the comments feature!", profilePicture));
        comments.add(new Comment("Tom Allerton", "This was a really cool event! I'm happy with how it went and the people were fantastic!", profilePicture));
        comments.add(new Comment("Tom Allerton", "This was a really cool event! I'm happy with how it went and the people were fantastic!", profilePicture));
        comments.add(new Comment("Tom Allerton", "This was a really cool event! I'm happy with how it went and the people were fantastic!", profilePicture));
        comments.add(new Comment("Tom Allerton", "This was a really cool event! I'm happy with how it went and the people were fantastic!", profilePicture));
        comments.add(new Comment("Tom Allerton", "This was a really cool event! I'm happy with how it went and the people were fantastic!", profilePicture));
        mRecyclerView = (RecyclerView) findViewById(R.id.comments);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        //Decide whether to show all comments or not
        if (comments.size() <= 5) {
            mAdapter = new CommentAdapter(comments, this);
        } else {
            mAdapter = new CommentAdapter(comments.subList(0, 5), this);
            TextView viewAll = findViewById(R.id.view_all_comments);
            viewAll.setVisibility(View.VISIBLE);
            viewAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup();
                }
            });
        }

        mRecyclerView.setAdapter(mAdapter);

        //Initialise adding comment
        final EditText comment = findViewById(R.id.edit_comment_text);
        final ImageView profilePictureView = findViewById(R.id.edit_profile_picture);
        ImageView addComment = findViewById(R.id.send_comment);
        initialiseAdd(comment, profilePictureView, addComment, false, comments, mAdapter);
    }

    private void initialiseAdd(final EditText comment, final ImageView profilePictureView,
                               ImageView addComment, final boolean showAll, final LinkedList<Comment> comments,
                               final CommentAdapter adapter) {
        Picasso.with(this).load(profilePicture).into(profilePictureView);
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = comment.getText().toString();
                if (text.length() > 0) {
                    text = text.trim();


                    comments.push(new Comment(displayName, text, profilePicture));
                    if (!showAll) {
                        List<Comment> newComments = comments.subList(0, 5);
                        adapter.setmDataset(newComments);
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.addElement();
                        adapter.notifyItemInserted(0);
                    }


                    //TODO: Send comment to server

                    comment.setText("");
                }
            }
        });
    }

    /**
     * Show the comments popup
     */
    private void showPopup() {
        RecyclerView.LayoutManager mOtherLayoutManager = new LinearLayoutManager(this);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View inflatedView = layoutInflater.inflate(R.layout.comment_popup_view, null, false);

        //Set up RecyclerView
        CommentAdapter adapter = new CommentAdapter(comments, NoteDisplayActivity.this);
        RecyclerView popupRecycler = inflatedView.findViewById(R.id.full_comment_list);
        popupRecycler.setLayoutManager(mOtherLayoutManager);
        popupRecycler.setAdapter(adapter);

        //Set up adding comments
        final EditText comment = inflatedView.findViewById(R.id.edit_comment_text);
        ImageView profilePictureView = inflatedView.findViewById(R.id.edit_profile_picture);
        ImageView addComment = inflatedView.findViewById(R.id.send_comment);
        initialiseAdd(comment, profilePictureView, addComment, true, comments, adapter);

        PopupWindow popupWindow = new PopupWindow(inflatedView, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, true);

        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        popupWindow.showAtLocation(overallView, Gravity.BOTTOM, 0, 100);


    }

    /**
     *
     * Initialise the UI
     */
    private void initialise() {
        //Initial UI settings

        //Try and find location to zoom into and set initial marker

        if (thisNote != null) {

            //Location
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
            RelativeLayout tagContainer = findViewById(R.id.tag_container);
            if (thisNote.getTags() == null || thisNote.getTags().size() == 0) {
                tagContainer.setVisibility(View.GONE);
            } else {
                tagContainer.setVisibility(View.VISIBLE);
            }
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
