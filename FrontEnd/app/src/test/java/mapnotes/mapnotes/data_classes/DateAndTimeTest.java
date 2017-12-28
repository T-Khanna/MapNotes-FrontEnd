package mapnotes.mapnotes.data_classes;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 28/12/2017.
 */
public class DateAndTimeTest {

    Calendar cal = Calendar.getInstance();

    private DateAndTime getObj= new DateAndTime(cal);
    private DateAndTime setObj = new DateAndTime(cal);

    public DateAndTimeTest() {

    }

    @Test
    public void getTime() throws Exception {
        Time time = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        if (!getObj.getTime().equals(time)) throw new Exception();
    }

    @Test
    public void setTime() throws Exception {
        Time newTime  = new Time(15, 37);
        setObj.setTime(newTime);
        if (!setObj.getTime().equals(newTime)) throw new Exception();
    }

    @Test
    public void getDate() throws Exception {
        if (!getObj.getDate().equals(cal.getTime())) throw new Exception();
    }

    @Test
    public void setDate() throws Exception {
        Date date = Calendar.getInstance().getTime();
        setObj.setDate(date);
        if (!getObj.getDate().equals(date)) throw new Exception();
    }

    @Test
    public void after() throws Exception {
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.MINUTE, 1);
        DateAndTime newDT = new DateAndTime(cal2);
        if (!newDT.after(getObj)) throw new Exception();
        cal2.add(Calendar.HOUR_OF_DAY, 1);
        newDT = new DateAndTime(cal2);
        if (!newDT.after(getObj)) throw new Exception();
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        newDT = new DateAndTime(cal2);
        if (!newDT.after(getObj)) throw new Exception();
    }

    @Test
    public void toLong() throws Exception {
        if (getObj.toLong() != cal.getTimeInMillis()) throw new Exception();
    }

    @Test
    public void strings() throws Exception {
        String fromString = getObj.toString();
        if (!DateAndTime.fromString(fromString).equals(getObj)) throw new Exception();
    }

    @Test
    public void equals() throws Exception {
        if (!getObj.equals(getObj)) throw new Exception();
    }

}