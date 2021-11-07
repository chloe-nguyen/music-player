package group4.music_player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class TimerDialog extends DialogFragment {
     NumberPicker hourPicker;
     NumberPicker minPicker;
    private PlayerActivity activity;
    private NoticeDialogListener listener;
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(TimerDialog dialog);
        public void onDialogNegativeClick(TimerDialog dialog);
    }

    public TimerDialog(PlayerActivity activity) {
        this.activity = activity;
        this.listener = activity;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        listener =(NoticeDialogListener) activity;
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_timer_dialog, null);
        hourPicker = (NumberPicker) view.findViewById(R.id.npHour);
        minPicker = (NumberPicker) view.findViewById(R.id.npMin);
        setUpPicker();
        builder.setView(view).setPositiveButton(R.string.choose, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onDialogPositiveClick(TimerDialog.this);
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TimerDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public void setUpPicker() {
        // hour picker set up
        String[] degreesValuesHour = new String[26];

        for (int i = 0; i <= 25; i++) {
            degreesValuesHour[i] = String.valueOf(i);
        }
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(24);
        hourPicker.setDisplayedValues(degreesValuesHour);
        // min picker set up
        String[] degreesValuesMin = new String[60];

        for (int i = 0; i <= 59; i++) {
            degreesValuesMin[i] = String.valueOf(i);
        }
        minPicker.setMinValue(0);
        minPicker.setMaxValue(59);
        minPicker.setDisplayedValues(degreesValuesMin);
    }

}