package mapnotes.mapnotes.data_classes;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Thomas on 22/11/2017.
 */

public class Comment {
    private String displayName;
    private String commentText;
    private Uri profile_picture;

    public Comment(String displayName, String commentText, Uri profile_picture) {
        this.displayName = displayName;
        this.commentText = commentText;
        this.profile_picture = profile_picture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Uri getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(Uri profile_picture) {
        this.profile_picture = profile_picture;
    }

    public Comment(JSONObject obj) {
        try {
            displayName = obj.getString("display_name");
            commentText = obj.getString("comment");
            profile_picture = Uri.parse(obj.getString("profile_picture"));
        } catch (JSONException e) {
            return;
        }
    }
}
