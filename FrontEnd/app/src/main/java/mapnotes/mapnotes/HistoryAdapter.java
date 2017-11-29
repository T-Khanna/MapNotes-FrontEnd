package mapnotes.mapnotes;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import mapnotes.mapnotes.activities.NoteDisplayActivity;
import mapnotes.mapnotes.data_classes.Note;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<Note> mDataset;
    private Context context;
    private String email;
    private String photoUrl;
    private String displayName;
    private String idToken;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView summary;
        public Note note;

        public ViewHolder(View v, final Context context, final String email, final String photoURL,
                final String displayName, final String idToken) {
            super(v);
            title = v.findViewById(R.id.title);
            summary = v.findViewById(R.id.summary);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, NoteDisplayActivity.class);
                    i.putExtra("note", note);
                    i.putExtra("loginEmail", email);
                    if (photoURL != null) {
                        i.putExtra("profile_picture", photoURL);
                    }
                    i.putExtra("display_name", displayName);
                    i.putExtra("login_id", idToken);
                    context.startActivity(i);
                }
            });
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(List<Note> myDataset, Context context, String email, String photoURL,
                          String idToken, String displayName) {
        this.context = context;
        mDataset = myDataset;
        this.email = email;
        this.photoUrl = photoURL;
        this.idToken = idToken;
        this.displayName = displayName;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters


        ViewHolder vh = new ViewHolder(v, context, email, photoUrl, displayName, idToken);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.title.setText(mDataset.get(position).getTitle());
        holder.summary.setText(mDataset.get(position).getDescription());
        holder.note = mDataset.get(position);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
