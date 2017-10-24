package mapnotes.mapnotes;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Date;

import mapnotes.mapnotes.data_classes.Function;

/**
 * Created by Thomas on 14/10/2017.
 */

public class DatePickerFragment extends DialogFragment
        implements android.app.DatePickerDialog.OnDateSetListener {

    Function<Date> callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker

        callback = (Function<Date>) getArguments().getSerializable("callback");

        // Create a new instance of TimePickerDialog and return it
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(getActivity());
        dialog.setOnDateSetListener(this);
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Date date = new Date(year - 1900, month, day);
        callback.run(date);
    }


}