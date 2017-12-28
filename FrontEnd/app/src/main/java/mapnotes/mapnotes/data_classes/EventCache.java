package mapnotes.mapnotes.data_classes;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Thomas on 21/12/2017.
 */

/**
 * A wrapper for a collection, but allows for persistence by saving to a file.
 */
public class EventCache implements Collection<String> {
    private String filename = "event_cache";
    private HashMap<String, Long> cache;
    private Context context;

    /**
     * Load the map from the file specified by filename
     */
    public EventCache(Context context) {
        this.context = context;
        try {
            File file = new File(context.getFilesDir(), filename);
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            cache = (HashMap<String, Long>) inputStream.readObject();
            inputStream.close();
        } catch (IOException e) {
            Log.e("EventCache", "Creating empty hash map, IOException");
            cache = new HashMap<>();
        } catch (ClassNotFoundException e) {
            Log.e("EventCache", "Creating empty hash map, ClassNotFoundException");
            cache = new HashMap<>();
        }
    }

    /**
     * Saves the current map to disk
     * @throws IOException - User handles save failing, so they can
     *                       fix storage themselves or disregard changes.
     */
    public void save() throws IOException {
        File file = new File(context.getFilesDir(), filename);
        Log.d("EventCache", "Creating outputstream");
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        Log.d("EventCache", "Writing map");
        outputStream.writeObject(cache);
        Log.d("EventCacge", "Wrote Map");
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Purely for testing, to ensure old elements are removed
     * @param toAdd
     * @param time
     */
    public void add(String toAdd, Long time) {
        cache.put(toAdd, time);
    }

    /**
     * Remove any old values from the map (more than a day old)
     */
    public void clean() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long time = cal.getTimeInMillis();
        HashMap<String, Long> newCache = new HashMap<>();
        for (Map.Entry<String, Long> entry : cache.entrySet()) {
            if (entry.getValue() > time) {
                newCache.put(entry.getKey(), entry.getValue());
            }
        }
        cache = newCache;
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return cache.containsKey(o);
    }

    @NonNull
    @Override
    public Iterator<String> iterator() {
        return cache.keySet().iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return cache.keySet().toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] ts) {
        return cache.keySet().toArray(ts);
    }

    @Override
    public boolean add(String s) {
        Long result = cache.put(s, Calendar.getInstance().getTimeInMillis());
        return result != null;
    }

    @Override
    public boolean remove(Object o) {
        return cache.remove(o) != null;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return cache.keySet().containsAll(collection);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends String> collection) {
        boolean result = true;
        Long time = Calendar.getInstance().getTimeInMillis();
        for (String s : collection) {
            result &= cache.put(s, time) != null;
        }
        return result;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        boolean result = true;
        for (Object s : collection) {
            result &= cache.remove(s) != null;
        }
        return result;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

}
