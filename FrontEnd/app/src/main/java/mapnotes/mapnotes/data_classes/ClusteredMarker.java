package mapnotes.mapnotes.data_classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusteredMarker implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle = "";
    private String mSnippet = "";
    private Note note;

    public ClusteredMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public ClusteredMarker(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    public void setTag (Note note) {
        this.note = note;
    }

    public Note getTag () {
        return note;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}