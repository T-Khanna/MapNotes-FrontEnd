package mapnotes.mapnotes.data_classes;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

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

    public Note(String title, String description, LatLng location, DateAndTime time, DateAndTime endTime) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.time = time;
        this.endTime = endTime;
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

        parcel.writeBundle(bundle);
        parcel.writeParcelable(location, i);
    }

    public Note(Parcel in) {
        Bundle bundle = in.readBundle();
        title = bundle.getString("title");
        description = bundle.getString("description");
        time = (DateAndTime) bundle.getSerializable("time");
        endTime = (DateAndTime) bundle.getSerializable("endTime");

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
}
