package com.example.pau.busyalert;


import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class FavouritesFragment extends ListFragment implements AdapterView.OnItemLongClickListener{
    private UserAdapter mAdapter;
    private User[] contact_list = {new User(), new User()};
    private ListView listv;
    private static final int DELETE_ID = Menu.FIRST + 1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new UserAdapter(getContext(), R.layout.contact_list, contact_list);
        listv.setAdapter(mAdapter);
        registerForContextMenu(listv);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);
        listv = (ListView) root.findViewById(android.R.id.list);
        return root;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.opt_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == DELETE_ID){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            contact_list = remove_contact((int)info.id);
            mAdapter = new UserAdapter(getContext(), R.layout.contact_list, contact_list);
            listv.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(),R.string.toast_deleted, Toast.LENGTH_LONG).show();
        }
        return super.onContextItemSelected(item);
    }

    private User[] remove_contact(int position){
        List<User> list = new ArrayList<User>(Arrays.asList(contact_list));
        Iterator<User> iterator = list.iterator();
        int counter = 0;
        while(iterator.hasNext()){
            iterator.next();
            if(counter == position)
                iterator.remove();
            counter++;
        }
        return list.toArray(new User[list.size()]);
    }
}