package mapnotes.mapnotes;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import mapnotes.mapnotes.data_classes.Comment;

/**
 * Created by Thomas on 22/11/2017.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> mDataset;
    private Context context;
    private int size;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView displayName;
        public TextView comment;
        public ImageView profilePicture;

        public ViewHolder(View v) {
            super(v);
            displayName = v.findViewById(R.id.display_name);
            comment = v.findViewById(R.id.comment_text);
            profilePicture = v.findViewById(R.id.profile_picture);
        }
    }

    public void setmDataset(List<Comment> newComments) {
        mDataset = newComments;
        size = mDataset.size();
    }

    public void addElement() {
        size++;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CommentAdapter(List<Comment> myDataset, Context context) {
        mDataset = myDataset;
        this.context = context;
        size = myDataset.size();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters


        CommentAdapter.ViewHolder vh = new CommentAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CommentAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Comment comment = mDataset.get(position);
        holder.displayName.setText(comment.getDisplayName());
        holder.comment.setText(comment.getCommentText());
        Picasso.with(context).load(comment.getProfile_picture()).into(holder.profilePicture);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return size;
    }
}
