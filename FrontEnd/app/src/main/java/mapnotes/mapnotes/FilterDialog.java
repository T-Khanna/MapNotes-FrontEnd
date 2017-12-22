package mapnotes.mapnotes;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import mapnotes.mapnotes.activities.AddNoteActivity;
import mapnotes.mapnotes.data_classes.Function;

/**
 * Created by Thomas on 10/11/2017.
 */

public class FilterDialog extends Dialog {

    TagContainerLayout tagLayout;
    private List<String> tags;

    public FilterDialog(@NonNull Context context, List<String> currentTags) {
        super(context);
        tags = currentTags;

        LayoutInflater inflater = getLayoutInflater();
        setContentView(inflater.inflate(R.layout.filter_dialog, null));

        tagLayout = findViewById(R.id.filter_tags);

        //Tags
        final EditText tagText = findViewById(R.id.tag_to_add);
        Button addTag = findViewById(R.id.add_tag);

        tagLayout.setTags(tags);

        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTag = tagText.getText().toString().trim().toLowerCase();
                if (!tags.contains(newTag)) {
                    tags.add(newTag);
                    tagLayout.setTags(tags);
                }
                tagText.setText("");
            }
        });

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

        Button negativeButton = findViewById(R.id.negative_button);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


    @Override
    public void show() {
        create();
        super.show();
    }

    public FilterDialog setPositiveButton(final Function<List<String>> callback) {
        Button positive = findViewById(R.id.positive_button);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.run(tags);
                dismiss();
            }
        });
        return this;
    }



}
