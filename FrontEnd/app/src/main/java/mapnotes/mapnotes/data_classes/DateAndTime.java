package mapnotes.mapnotes.data_classes;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Thomas on 14/10/2017.
 */

public class DateAndTime implements Serializable {
    private Time time;
    private Date date;

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DateAndTime(Calendar cal) {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        time = new Time(hour, min);
        date = cal.getTime();
    }

    /**
     * Check if this dateAndTime is after a given dateAndTime
     * @param otherTime
     * @return
     */
    public boolean after(DateAndTime otherTime) {
        if (date.getDate() > otherTime.getDate().getDate()) {
            return true;
        } else if (otherTime.getDate().getDate() == date.getDate()){
            if (time.getHourOfDay() > otherTime.getTime().getHourOfDay()) {
                return true;
            } else if (otherTime.getTime().getHourOfDay() == time.getHourOfDay()){
                return time.getMinute() > otherTime.getTime().getMinute();
            }
        }
        return false;
    }

    public long toLong() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, time.getHourOfDay());
        cal.set(Calendar.MINUTE, time.getMinute());
        return cal.getTimeInMillis();
    }

    @Override
    public String toString() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, time.getHourOfDay());
        cal.set(Calendar.MINUTE, time.getMinute());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date time = cal.getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return outputFmt.format(time);
    }

    public static DateAndTime fromString(String utc) {
        SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d = inputFmt.parse(utc);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            return new DateAndTime(calendar);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toSimpleString() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, time.getHourOfDay());
        cal.set(Calendar.MINUTE, time.getMinute());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date time = cal.getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("MMM dd 'at' HH:mm");
        return outputFmt.format(time);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DateAndTime) {
            return ((DateAndTime) obj).toString().equals(toString());
        }
        return false;
    }

}
