package mapnotes.mapnotes.data_classes;

import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 28/12/2017.
 */
public class NoteTestDevice {

    private String title = "title";
    private String description = "Description";
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private LatLng location = new LatLng(0,0);
    private HashSet<String> tags = new HashSet<>();
    private HashSet<User> users = new HashSet<>();
    private ArrayList<String> images = new ArrayList<>();
    private Note getNote;


    public NoteTestDevice() {
        endTime.add(Calendar.HOUR_OF_DAY, 1);
        tags.add("TEST_TAG");
        getNote = new Note(title, description, location, new DateAndTime(startTime), new DateAndTime(endTime), 0, tags, users, images);
    }

    @Test
    public void toJson() throws Exception {
        JSONObject jsonNote = getNote.toJson();
        Note newNote = new Note(jsonNote);

        if (!newNote.equals(getNote)) throw new Exception();
    }

}