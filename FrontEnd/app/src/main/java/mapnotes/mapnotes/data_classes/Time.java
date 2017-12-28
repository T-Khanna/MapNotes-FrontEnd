package mapnotes.mapnotes.data_classes;

import java.io.Serializable;

/**
 * Created by Thomas on 14/10/2017.
 */

public class Time implements Serializable {
    private int hourOfDay;
    private int minute;

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public Time(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    @Override
    public String toString() {
        String hourText = hourOfDay < 10 ? "0" + hourOfDay : String.valueOf(hourOfDay);
        String minText = minute < 10 ? "0" + minute : String.valueOf(minute);
        return hourText + ":" + minText;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Time) {
            return ((Time) obj).getMinute() == minute && ((Time) obj).getHourOfDay() == hourOfDay;
        }
        return false;
    }

}
