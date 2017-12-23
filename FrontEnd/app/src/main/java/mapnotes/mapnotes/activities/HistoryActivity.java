package mapnotes.mapnotes.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import mapnotes.mapnotes.HistoryAdapter;
import mapnotes.mapnotes.R;
import mapnotes.mapnotes.Server;
import mapnotes.mapnotes.data_classes.Note;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String loginID;
    private String profilePicture;
    private String displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent i = getIntent();
        ArrayList<Note> noteList = (ArrayList<Note>) i.getSerializableExtra("notes");
        String email = i.getStringExtra("loginEmail");
        loginID = i.getStringExtra("login_id");

        setTitle(i.getStringExtra("label"));

        if (i.hasExtra("profile_picture")) {
            profilePicture = i.getStringExtra("profile_picture");
        }
        displayName = i.getStringExtra("display_name");

        mRecyclerView = (RecyclerView) findViewById(R.id.note_history_view);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new HistoryAdapter(noteList, this, email, profilePicture, loginID, displayName);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        long exp = sharedPref.getLong("exp", Long.MAX_VALUE);
        if (Calendar.getInstance().getTimeInMillis() > exp) {
            Intent i = new Intent(this, SplashScreenActivity.class);
            startActivity(i);
            finish();
        }
    }
}
