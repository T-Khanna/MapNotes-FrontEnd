package mapnotes.mapnotes.data_classes;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 28/12/2017.
 */
public class EventCacheTest {

    Context context;
    EventCache eventCache;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        eventCache = new EventCache(context);
    }

    @Test
    public void clean() throws Exception {
        String newElem = "@@NEW_ELEMENT@@";
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -3);
        eventCache.add(newElem, cal.getTimeInMillis());
        eventCache.clean();
        if (eventCache.contains(newElem)) throw new Exception();
    }

    @Test
    public void clear() throws Exception {
        eventCache.clear();
        if (eventCache.size() > 0) throw new Exception();
    }

}