package mapnotes.mapnotes.data_classes;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class to represent an individual note
 */
public class Note implements Parcelable {
    private String title = null;
    private String description = null;
    private LatLng location = null;
    private DateAndTime time = null;
    private DateAndTime endTime = null;
    private Integer id = null;
    private HashSet<String> tags = new HashSet<>();
    private HashSet<User> users = new HashSet<>();
    private ArrayList<String> imageUrls = new ArrayList<>();
    public static final int TITLE_ERROR = 1;
    public static final int VALID = 0;
    public static final int LOCATION_ERROR = 2;
    public static final int TIME_ERROR = 3;

    public Note(String title, String description, LatLng location, DateAndTime time,
                DateAndTime endTime, int id, HashSet<String> tags, HashSet<User> users, ArrayList<String> images) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.time = time;
        this.endTime = endTime;
        this.id = id;
        this.tags = tags;
        this.users = users;
        imageUrls = images;
    }

    public Set<User> getUserEmail() {
        return users;
    }

    public void setUserEmail(HashSet<User> userEmail) {
        this.users = userEmail;
    }

    public Note() {}

    public void setId(Integer id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public DateAndTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateAndTime endTime) {
        this.endTime = endTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public DateAndTime getTime() {
        return time;
    }

    public void setTime(DateAndTime time) {
        this.time = time;
    }

    public boolean addTag(String tag) {
        return tags.add(tag);
    }

    public ArrayList<String> getImageURLs() {
        return imageUrls;
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public Set<String> getTags() {
        return tags;
    }

    public boolean userIsMemberOf(String userEmail) {
        for (User user : users) {
            if (user.getEmail().equals(userEmail)) return true;
        }
        return false;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public boolean addImageUrl(String imageUrl) {
        return imageUrls.add(imageUrl);
    }

    public int isValid() {
        if (title == null || title.equals("")) return TITLE_ERROR;
        if (time == null || endTime == null || time.after(endTime)) return TIME_ERROR;
        if (location == null) return LOCATION_ERROR;
        return VALID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Note) {
            Note noteObj = (Note) obj;
            return (title.equals(noteObj.getTitle())
                    && description.equals(noteObj.getDescription())
                    && location.equals(noteObj.location)
                    && time.equals(noteObj.getTime())
                    && endTime.equals(noteObj.getEndTime())
                    && id.equals(noteObj.getId())
                    && tags.equals(noteObj.getTags())
                    && users.equals(noteObj.getUserEmail())
                    && imageUrls.equals(noteObj.getImageURLs()));
        }
        return false;
    }

    //Parcelable section --------------------------------------------------------------------------

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("time", time);
        bundle.putSerializable("endTime", endTime);
        bundle.putString("title", title);
        bundle.putString("description", description);
        bundle.putSerializable("users", users);

        if (id != null) {
            bundle.putInt("id", id);
        }
        bundle.putStringArray("tags", tags.toArray(new String[0]));
        bundle.putSerializable("images", imageUrls);
        parcel.writeBundle(bundle);
        parcel.writeParcelable(location, i);
    }

    public Note(Parcel in) {
        Bundle bundle = in.readBundle(getClass().getClassLoader());
        title = bundle.getString("title");
        description = bundle.getString("description");
        time = (DateAndTime) bundle.getSerializable("time");
        endTime = (DateAndTime) bundle.getSerializable("endTime");
        id = bundle.getInt("id");

        users = (HashSet<User>) bundle.getSerializable("users");

        String[] tags = bundle.getStringArray("tags");
        if (tags != null) {
            for (String tag : tags) {
                this.tags.add(tag);
            }
        }

        imageUrls = (ArrayList<String>) bundle.getSerializable("images");

        location = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    //JSON Section ---------------------------------------------------------------------------------

    public JSONObject toJson() {
        JSONObject jNote = new JSONObject();
        try {
            jNote.put("title", title);
            jNote.put("comment", description);
            jNote.put("id", id);
            jNote.put("start_time", time.toString());
            jNote.put("end_time", endTime.toString());
            jNote.put("latitude", location.latitude);
            jNote.put("longitude", location.longitude);

            JSONArray usersArr = new JSONArray();
            for (User user : users) {
                usersArr.put(user);
            }
            jNote.put("users", usersArr);

            JSONArray arr = new JSONArray();
            for (String tag : tags) {
                arr.put(tag);
            }
            jNote.put("tags", arr);

            JSONArray imagesArr = new JSONArray();
            for (String imageUrl : imageUrls) {
                imagesArr.put(imageUrl);
            }
            jNote.put("images", imagesArr);

            return jNote;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Note(JSONObject object) {
        try {
            this.id = object.getInt("id");
            this.title = object.getString("title");
            this.description = object.getString("comment");
            double latitude = object.getDouble("latitude");
            double longitude = object.getDouble("longitude");
            location = new LatLng(latitude, longitude);
            time = DateAndTime.fromString(object.getString("start_time"));
            endTime = DateAndTime.fromString(object.getString("end_time"));

            JSONArray userArray = object.getJSONArray("users");
            HashSet<User> newUsers = new HashSet<>();
            for (int i = 0; i < userArray.length(); i++) {
                newUsers.add(new User(userArray.getJSONObject(i)));
            }
            users = newUsers;

            JSONArray tagArray = object.getJSONArray("tags");
            HashSet<String> newTags = new HashSet<>();
            for (int i = 0; i < tagArray.length(); i++) {
                newTags.add(tagArray.getString(i));
            }
            tags = newTags;

//            JSONArray imagesArr = object.getJSONArray("images");
//            HashSet<String> newImageUrls = new HashSet<>();
//            for (int i = 0; i < imagesArr.length(); i++) {
//                newImageUrls.add(imagesArr.getString(i));
//            }
//            imageUrls = newImageUrls;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
