package mapnotes.mapnotes.data_classes;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Thomas on 14/10/2017.
 */

public class DateAndTime implements Serializable {
    private Time time;
    private long date;

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public DateAndTime(Time time, long date) {
        this.time = time;
        this.date = date;
    }

    public DateAndTime(Calendar cal) {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = new Time(hour, min);
        date = cal.getTimeInMillis();
    }



    public long getMillis() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.set(Calendar.HOUR_OF_DAY, time.getHourOfDay());
        cal.set(Calendar.MINUTE, time.getMinute());
        return cal.getTimeInMillis();
    }

    /**
     * Check if this dateAndTime is after a given dateAndTime
     * @param otherTime
     * @return
     */
    public boolean after(DateAndTime otherTime) {
        if (date > otherTime.getDate()) {
            return true;
        } else if (otherTime.getDate() == date){
            if (time.getHourOfDay() > otherTime.getTime().getHourOfDay()) {
                return true;
            } else if (otherTime.getTime().getHourOfDay() == time.getHourOfDay()){
                return time.getMinute() > otherTime.getTime().getMinute();
            }
        }
        return false;
    }

}
