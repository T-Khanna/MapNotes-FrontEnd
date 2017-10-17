package mapnotes.mapnotes.data_classes;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

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

    public Note(String title, String description, LatLng location, DateAndTime time, DateAndTime endTime, int id) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.time = time;
        this.endTime = endTime;
        this.id = id;
    }

    public Note() {}

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

    public boolean isValid() {
        return title != null &&
                !title.equals("") &&
                time != null &&
                endTime != null &&
                endTime.after(time) &&
                location != null;
    }

    //Parcelable section ---------------------------------------------------------------------------

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
        if (id != null) {
            bundle.putInt("id", id);
        }

        parcel.writeBundle(bundle);
        parcel.writeParcelable(location, i);
    }

    public Note(Parcel in) {
        Bundle bundle = in.readBundle();
        title = bundle.getString("title");
        description = bundle.getString("description");
        time = (DateAndTime) bundle.getSerializable("time");
        endTime = (DateAndTime) bundle.getSerializable("endTime");
        id = bundle.getInt("id");

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
