package mapnotes.mapnotes;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Time;

/**
 * Created by Thomas on 13/10/2017.
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    Function<Time> callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker

        callback = (Function<Time>) getArguments().getSerializable("callback");

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, 0, 0,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        callback.run(new Time(hourOfDay, minute));
    }


}
