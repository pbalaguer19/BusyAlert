package com.example.pau.busyalert;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class OptionsFragment extends Fragment {

    SeekBar seekBar;
    TextView speed;
    View root;

    public OptionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_options, container, false);

        /*seekBar = (SeekBar) root.findViewById(R.id.seekBar);
        speed = (TextView) root.findViewById(R.id.speed);
        speed.setText("15");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i < 10){
                    speed.setText("10");
                    seekBar.setProgress(10);
                    Toast.makeText(root.getContext(), R.string.toast_seekbar, Toast.LENGTH_LONG).show();
                }else
                    speed.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

        return root;
    }

}
