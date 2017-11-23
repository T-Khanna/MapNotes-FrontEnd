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
    private int noteId;

    public Comment(String displayName, String commentText, Uri profile_picture, int noteId) {
        this.displayName = displayName;
        this.commentText = commentText;
        this.profile_picture = profile_picture;
        this.noteId = noteId;
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
            commentText = obj.getString("comment");
            JSONObject userobj = obj.getJSONObject("user");
            User user = new User(userobj);
            profile_picture = Uri.parse(userobj.getString("picture"));
            displayName = user.getDisplayname();
        } catch (JSONException e) {
            return;
        }
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("comment", commentText);
            obj.put("noteid", noteId);
            return obj;
        } catch (JSONException e) {
            return null;
        }
    }
}
