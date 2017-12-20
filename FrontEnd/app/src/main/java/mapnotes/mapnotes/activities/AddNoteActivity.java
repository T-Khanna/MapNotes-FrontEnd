package mapnotes.mapnotes.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import mapnotes.mapnotes.DatePickerFragment;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.TimePickerFragment;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;
import mapnotes.mapnotes.data_classes.Time;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddNoteActivity extends FragmentActivity {

    private Note thisNote = new Note();
    private TextView startTime;
    private TextView endTime;
    private TextView startDate;
    private TextView endDate;
    private List<String> tags = new LinkedList<>();
    private LinearLayout addedImages;
    private final int REQUEST_LOCATION = 12786;
    private final int REQUEST_IMAGE = 1763;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        initialise();
    }


    public void initialise() {

        //Try and find location to zoom into and set initial marker
        Intent i = getIntent();
        LatLng location = null;
        if (i.hasExtra("location")) {
            location = i.getParcelableExtra("location");
            thisNote.setLocation(location);
        }

        //Set up local variables
        Calendar cal = Calendar.getInstance();
        thisNote.setTime(new DateAndTime(cal));

        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        thisNote.setEndTime(new DateAndTime(cal));

        final EditText title = findViewById(R.id.title);
        final EditText description = findViewById(R.id.description);
        startTime = findViewById(R.id.start_time);
        endTime = findViewById(R.id.end_time);
        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisNote.setTitle(title.getText().toString().trim());
                thisNote.setDescription(description.getText().toString().trim());
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR_OF_DAY, 1);
                ColorStateList colorStateList = ColorStateList.valueOf(Color.RED);


                int noteValid = thisNote.isValid();
                switch (noteValid) {
                    case Note.VALID:
                        Intent result = new Intent();
                        result.putExtra("note", thisNote);
                        setResult(Activity.RESULT_OK, result);
                        finish();
                        break;
                    case Note.TITLE_ERROR:
                        title.setBackgroundTintList(colorStateList);
                        break;
                    case Note.TIME_ERROR:
                        startTime.setTextColor(Color.RED);
                        endTime.setTextColor(Color.RED);
                        startDate.setTextColor(Color.RED);
                        endDate.setTextColor(Color.RED);
                        break;
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
                tagText.setText("");
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

        final ImageButton addImage = findViewById(R.id.add_image);
        addedImages = findViewById(R.id.image_scroll);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), REQUEST_IMAGE);
            }
        });


        final TextView locationText = findViewById(R.id.location_text);

        //Check if we are editing a note
        if (i.hasExtra("editNote")) {
            thisNote = i.getParcelableExtra("editNote");
            location = thisNote.getLocation();

            try {
                Geocoder geocoder = new Geocoder(getApplicationContext());
                List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    locationText.setText(address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            title.setText(thisNote.getTitle());
            description.setText(thisNote.getDescription());

            tags = new LinkedList<>(thisNote.getTags());
            tagContainerLayout.setTags(tags);
        } else {
            locationText.setText(getAddress(location));
        }


        final LatLng locationCopy = location;
        locationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AddNoteActivity.this, SelectLocationActivity.class);
                i.putExtra("location", locationCopy);
                startActivityForResult(i, REQUEST_LOCATION);
            }
        });

        updateTimes(startDate, startTime, thisNote.getTime());
        updateTimes(endDate, endTime, thisNote.getEndTime());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_LOCATION) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                LatLng location = data.getParcelableExtra("location");
                thisNote.setLocation(location);
                String address = data.getStringExtra("address");
                TextView locationText = findViewById(R.id.location_text);
                locationText.setText(address);
            }
        }
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                Log.d("Intent", data.toString());
                // Check if multiple images were sent. If multiple images were sent
                // the data INTENT object is null
                if (data.getData() == null) {
                    // Retrieve the collection of selected images from the clip field
                    // in the intent
                    ClipData images = data.getClipData();
                    for (int i = 0; i < images.getItemCount(); i++) {
                        Uri uri = images.getItemAt(i).getUri();
                        addAndUploadImage(uri);
                    }
                } else {
                    // We are only dealing with one image sent through the data field
                    // in the intent
                    Uri selectedImageUri = data.getData();
                    addAndUploadImage(selectedImageUri);
                }
            }
        }
    }

    private void addAndUploadImage(Uri selectedImageUri) {
        final ImageView imageToAdd = new ImageView(this);
        imageToAdd.setPadding(2, 2, 2, 2);
        imageToAdd.setAdjustViewBounds(true);
        imageToAdd.setMaxWidth(300);
        imageToAdd.setMaxHeight(300);
        Glide.with(this)
                .load(selectedImageUri)
                .into(imageToAdd);
        addedImages.addView(imageToAdd);

        String absolutePath = this.getFilesDir().getAbsolutePath();
        File tempFile = new File(absolutePath, "temp_image");
        try {
            tempFile.createNewFile();
            copyAndClose(this.getContentResolver().openInputStream(selectedImageUri), new FileOutputStream(tempFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri newUri = Uri.fromFile(tempFile);
        Log.d("New Uri Path", newUri.getPath());
        File imageFile = new File(newUri.getPath());
        Log.d("File path", imageFile.getAbsolutePath());
        // Post Image to Imgur API and add it to list of image urls for this note.
        new ImgurAPIAccess().execute(encodeFileToBase64Binary(imageFile));
    }

    private class ImgurAPIAccess extends AsyncTask<String, Void, Void> {

        private final String clientId = "e6d087c58c469c5";
        private final String clientSecret = "afd171d304e6f7216ba38381a560d3b298d4f23b";
        private final String imgurURL = "https://api.imgur.com/3/image";

        @Override
        protected Void doInBackground(String... strings) {
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", strings[0])
                    .build();
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Client-ID " + clientId)
                    .addHeader("Content-type", "multipart/form-data")
                    .url(imgurURL)
                    .post(body)
                    .build();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.d("Imgur POST request", "Response Code: " + response.code());
                    return null;
                }
                String responseBody = response.body().string();
                if (responseBody != null) {
                    JSONObject responseJSON = new JSONObject(responseBody);
                    String link =  responseJSON.getJSONObject("data").getString("link");
                    thisNote.addImageUrl(link);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private String encodeFileToBase64Binary(File file) {
        byte[] bytes = new byte[0];
        try {
            bytes = loadFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] encoded = Base64.encodeBase64(bytes);

        return new String(encoded);
    }

    private static byte[] loadFile(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int)length];
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Image file is too large to load");
        }

        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }

    private void copyAndClose(InputStream inputStream, FileOutputStream fileOutputStream) {
        try {
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTimes(TextView dateView, TextView timeView, DateAndTime time) {
        Date d = time.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM dd, YYYY");
        String date = dateFormat.format(d);
        dateView.setText(date);

        timeView.setText(time.getTime().toString());
    }

    private String getAddress(LatLng location) {
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                return address;
            } else {
                return location.latitude + ", " + location.longitude;
            }
        } catch (IOException e) {
            return location.latitude + ", " + location.longitude;
        }
    }

}
