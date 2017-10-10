package mapnotes.mapnotes;

import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SeekBar timeSlider;
    private Server server;
    private TextView sliderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        timeSlider = (SeekBar) findViewById(R.id.time_slider);
        sliderText = (TextView) findViewById(R.id.time_text);
        sliderText.setVisibility(View.GONE);

        timeSlider.setMax(23);
        Calendar cal = Calendar.getInstance();
        timeSlider.setProgress(cal.get(Calendar.HOUR_OF_DAY));
        mapFragment.getMapAsync(this);

        server = new Server(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialise Map
        //Disable Map Toolbar:
        mMap.getUiSettings().setMapToolbarEnabled(false);

        timeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String text = "";
                if (i < 10) text = "0";
                text += i + ":00";
                sliderText.setText(text);
                int x = timeSlider.getThumb().getBounds().right;
                int width = sliderText.getWidth() / 2;
                sliderText.setX(x - width);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sliderText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Request new locations
                server.getStringRequest("", new Function<String>() {
                    @Override
                    public void run(String input) {
                        new AlertDialog.Builder(MapsActivity.this).setMessage("Got response from server:" + input).create();
                    }
                });
                sliderText.setVisibility(View.GONE);
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
