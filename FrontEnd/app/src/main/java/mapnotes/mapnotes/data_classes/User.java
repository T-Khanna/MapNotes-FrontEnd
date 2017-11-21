package mapnotes.mapnotes.data_classes;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Thomas on 21/11/2017.
 */

public class User implements Parcelable, Serializable {
    private String displayname;
    private String email;
    private int id;

    public User(String displayname, String email, int id) {
        this.displayname = displayname;
        this.email = email;
        this.id = id;
    }

    public User(JSONObject obj) {
        try {
            this.displayname = obj.getString("Name");
            this.id = obj.getInt("Id");
            this.email = obj.getString("Email");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle b = new Bundle();
        b.putString("display_name", displayname);
        b.putString("email", email);
        b.putInt("id", id);
        parcel.writeBundle(b);
    }

    public User(Parcel parcel) {
        Bundle b = parcel.readBundle();
        displayname = b.getString("display_name");
        email = b.getString("email");
        id = b.getInt("id");
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
