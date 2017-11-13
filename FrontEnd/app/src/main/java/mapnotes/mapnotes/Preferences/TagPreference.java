package mapnotes.mapnotes.Preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import mapnotes.mapnotes.R;

/**
 * Created by Thomas on 13/11/2017.
 */

public class TagPreference extends DialogPreference {

    private List<String> tags;
    private EditText tagToAdd;
    private Button addButton;
    private TagContainerLayout tagLayout;

    public TagPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TagPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.tag_preference_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    public Set<String> getTags() {
        return listToSet(tags);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        tagToAdd = view.findViewById(R.id.tag_to_add);

        addButton = view.findViewById(R.id.add_tag);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTag = tagToAdd.getText().toString().trim();
                if (!tags.contains(newTag)) {
                    tags.add(newTag);
                    tagLayout.setTags(tags);
                }
                tagToAdd.setText("");
            }
        });

        tagLayout = view.findViewById(R.id.filter_tags);

        tagLayout.setOnTagClickListener(new TagView.OnTagClickListener() {

            @Override
            public void onTagClick(int position, String text) {

            }

            @Override
            public void onTagLongClick(int position, String text) {

            }

            @Override
            public void onTagCrossClick(int position) {
                tags.remove(position);
                tagLayout.removeTag(position);
            }
        });

        tagLayout.setTags(tags);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistStringSet(listToSet(tags));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        for (String tag : tags) {
                            FirebaseMessaging.getInstance().subscribeToTopic(tag);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            tags = setToList(this.getPersistedStringSet(new HashSet<String>()));
        } else {
            // Set default state from the XML attribute
            tags = (List<String>) defaultValue;
            persistStringSet(listToSet(tags));
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return new HashSet<String>();
    }

    private Set<String> listToSet(List<String> list) {
        Set<String> newSet = new HashSet<>();
        for (String s : list) {
            newSet.add(s);
        }
        return newSet;
    }

    private List<String> setToList(Set<String> set) {
        List<String> newSet = new LinkedList<>();
        for (String s : set) {
            newSet.add(s);
        }
        return newSet;
    }
}
