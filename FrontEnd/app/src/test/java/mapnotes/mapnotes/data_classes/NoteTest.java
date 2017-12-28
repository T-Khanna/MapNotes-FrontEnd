package mapnotes.mapnotes.data_classes;

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
public class NoteTest {
    private String title = "title";
    private String description = "Description";
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private LatLng location = new LatLng(0,0);
    private HashSet<String> tags = new HashSet<>();
    private HashSet<User> users = new HashSet<>();
    private ArrayList<String> images = new ArrayList<>();
    private Note getNote;
    private Note setNote = new Note();

    public NoteTest() {
        endTime.add(Calendar.HOUR_OF_DAY, 1);
        tags.add("TEST_TAG");
        getNote = new Note(title, description, location, new DateAndTime(startTime), new DateAndTime(endTime), 0, tags, users, images);
    }

    @Test
    public void getUserEmail() throws Exception {
        if (getNote.getUserEmail() != users) throw new Exception();
    }

    @Test
    public void setUserEmail() throws Exception {
        HashSet<User> users = new HashSet<>();
        setNote.setUserEmail(users);
        if (setNote.getUserEmail() != users) throw new Exception();
    }

    @Test
    public void setId() throws Exception {
        int id = 5;
        setNote.setId(id);
        if (setNote.getId() != id) throw new Exception();
    }

    @Test
    public void getId() throws Exception {
        if (getNote.getId() != 0) throw new Exception();
    }

    @Test
    public void getEndTime() throws Exception {
        if (getNote.getEndTime().toLong() != endTime.getTimeInMillis()) throw new Exception();
    }

    @Test
    public void setEndTime() throws Exception {
        setNote.setEndTime(new DateAndTime(endTime));
        if (setNote.getEndTime().toLong() != endTime.getTimeInMillis()) throw new Exception();
    }

    @Test
    public void getTitle() throws Exception {
        if (!getNote.getTitle().equals(title)) throw new Exception();
    }

    @Test
    public void setTitle() throws Exception {
        setNote.setTitle(title);
        if (!setNote.getTitle().equals(title)) throw new Exception();
    }

    @Test
    public void getDescription() throws Exception {
        if (!getNote.getDescription().equals(description)) throw new Exception();
    }

    @Test
    public void setDescription() throws Exception {
        setNote.setDescription(description);
        if (!setNote.getDescription().equals(description)) throw new Exception();
    }

    @Test
    public void getLocation() throws Exception {
        if (getNote.getLocation() != location) throw new Exception();
    }

    @Test
    public void setLocation() throws Exception {
        setNote.setLocation(location);
        if (setNote.getLocation() != location) throw new Exception();
    }

    @Test
    public void getTime() throws Exception {
        if (getNote.getTime().toLong() != startTime.getTimeInMillis()) throw new Exception();
    }

    @Test
    public void setTime() throws Exception {
        setNote.setTime(new DateAndTime(startTime));
        if (setNote.getTime().toLong() != startTime.getTimeInMillis()) throw new Exception();
    }

    @Test
    public void addTag() throws Exception {
        setNote.addTag("NEW_TAG");
        if (!setNote.getTags().contains("NEW_TAG")) throw new Exception();
    }


    @Test
    public void getImageURLs() throws Exception {
        if (getNote.getImageURLs() != images) throw new Exception();
    }

    @Test
    public void removeTag() throws Exception {
        setNote.addTag("REMOVE_TAG");
        setNote.removeTag("REMOVE_TAG");
        if (setNote.getTags().contains("REMOVE_TAG")) throw new Exception();
    }

    @Test
    public void getTags() throws Exception {
        if (getNote.getTags() != tags) throw new Exception();
    }

    @Test
    public void userIsMemberOf() throws Exception {
        HashSet<User> users = new HashSet<>();
        User user = new User("test_user", "test_user", 0);
        users.add(user);
        setNote.setUserEmail(users);
        if (!setNote.userIsMemberOf("test_user")) throw new Exception();
        if (setNote.userIsMemberOf("weird_user")) throw new Exception();

    }

    @Test
    public void hasTag() throws Exception {
        if (!getNote.hasTag("TEST_TAG")) throw new Exception();
        if (getNote.hasTag("NOT_TEST_TAG")) throw new Exception();
    }

    @Test
    public void addImageUrl() throws Exception {
        setNote.addImageUrl("IMAGE_URL");
        if (!setNote.getImageURLs().contains("IMAGE_URL")) throw new Exception();
    }

    @Test
    public void isValid() throws Exception {
        if (getNote.isValid() != Note.VALID) throw new Exception();
        if (new Note().isValid() == Note.VALID) throw new Exception();
    }
}