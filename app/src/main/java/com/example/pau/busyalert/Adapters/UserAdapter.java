package com.example.pau.busyalert.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.pau.busyalert.R;
import com.example.pau.busyalert.JavaClasses.User;

/**
 * Created by pau on 5/3/17.
 */

public class UserAdapter extends ArrayAdapter<User> {

    private Context context;
    private int layoutResourceId;
    private User users[];

    public UserAdapter(Context context, int layoutResourceId, User[] users){
        super(context, layoutResourceId, users);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserHolder userHolder;
        View row = convertView;

        if(row == null){
            LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
            row = layoutInflater.inflate(layoutResourceId, parent, false);

            userHolder = new UserHolder();
            userHolder.username = (TextView) row.findViewById(R.id.name);
            userHolder.userstatus = (TextView) row.findViewById(R.id.status);
            row.setTag(userHolder);
        }else
            userHolder = (UserHolder)row.getTag();

        User user = this.users[position];
        if (user != null){
            userHolder.username.setText(user.getName());
            userHolder.userstatus.setText(user.getStatus());
        }
        return row;
    }

    static class UserHolder{
        TextView username;
        TextView userstatus;
    }
}
