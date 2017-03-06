package com.example.pau.busyalert;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener{

    private Button btnMonitoring, btnNotification, btnPremium;
    private boolean monitorEnabled = false, notificationsEnabled = false;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnMonitoring = (Button) root.findViewById(R.id.btnMonitoring);
        btnNotification = (Button) root.findViewById(R.id.btnNotifications);
        btnPremium = (Button) root.findViewById(R.id.btnPremiumNotifications);

        btnMonitoring.setOnClickListener(this);
        btnNotification.setOnClickListener(this);
        btnPremium.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnMonitoring:
                monitoringHandler();
                break;
            case R.id.btnNotifications:
                notificationsHandler();
                break;
            case R.id.btnPremiumNotifications:
                registerForContextMenu(view);
                getActivity().openContextMenu(view);
                break;
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.premium_notifications_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.optMeeting:
                Toast.makeText(getContext(), R.string.meeting_toast,Toast.LENGTH_SHORT).show();
                break;
            case  R.id.optSleeping:
                Toast.makeText(getContext(), R.string.sleeping_toast,Toast.LENGTH_SHORT).show();
                break;
            case R.id.optOther:
                Toast.makeText(getContext(), R.string.other_toast,Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onContextItemSelected(item);
    }

    private void monitoringHandler(){
        if(!monitorEnabled){
            Toast.makeText(getContext(), R.string.enable_monitoring,Toast.LENGTH_SHORT).show();
            monitorEnabled = true;
            btnMonitoring.setText(R.string.btn_monitoring_stop);
        }else{
            Toast.makeText(getContext(), R.string.disable_monitoring,Toast.LENGTH_SHORT).show();
            monitorEnabled = false;
            btnMonitoring.setText(R.string.btn_monitoring);
        }
    }

    private void notificationsHandler(){
        if(!notificationsEnabled){
            Toast.makeText(getContext(), R.string.enable_notifications,Toast.LENGTH_SHORT).show();
            notificationsEnabled = true;
            btnNotification.setText(R.string.btn_notifications_stop);
        }else{
            Toast.makeText(getContext(), R.string.disable_notifications,Toast.LENGTH_SHORT).show();
            notificationsEnabled = false;
            btnNotification.setText(R.string.btn_notifications);
        }
    }
}
