package com.example.pau.busyalert;


import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.ArraySet;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class SocialFragment extends ListFragment implements AdapterView.OnItemLongClickListener {
    private UserAdapter mAdapter;
    private User[] contact_list = {new User(), new User()};
    private ListView listv;
    private EditText textSearch;
    private Set<String> setUser = new ArraySet<>(); //Used for duplicated contacts
    private static final int CONTACT_ID = Menu.FIRST + 2;

    /** Identifier for the permission request **/
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPermissionToReadUserContacts();
        getContacts();
        showContacts(contact_list);
        registerForContextMenu(listv);

        textSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals("")){
                    showContacts(contact_list);
                }else{
                    searchItem(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_social, container, false);
        listv = (ListView) root.findViewById(android.R.id.list);
        textSearch = (EditText) root.findViewById(R.id.txtSearchSocial);
        return root;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CONTACT_ID, 0, R.string.opt_add_favourites);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CONTACT_ID) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String name = contact_list[(int) info.id].getName();
            Toast.makeText(getContext(), getString(R.string.toast_added, name), Toast.LENGTH_LONG).show();
        }
        return super.onContextItemSelected(item);
    }

    public void getPermissionToReadUserContacts() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {}
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }


    private void showContacts(User[] contact_list){
        mAdapter = new UserAdapter(getContext(), R.layout.contact_list, contact_list);
        listv.setAdapter(mAdapter);
    }

    private void searchItem(String text){
        List<User> newList = new ArrayList<>();
        for(User u: contact_list){
            if(u != null && u.getName().toLowerCase().startsWith(text.toLowerCase()))
                newList.add(u);
        }
        showContacts(newList.toArray(new User[newList.size()]));
    }

    private void getContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = getContext().getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        contact_list = new User[people.getCount()];
        int i = 0;
        if (people.moveToFirst()) {
            do {
                String name = people.getString(indexName);
                if(!setUser.contains(name)){
                    //Random status, the real one will be in the cloud.
                    if((i % 2) == 0)
                        contact_list[i] = new User(name, "Available");
                    else
                        contact_list[i] = new User(name, "Busy");
                    i++;
                    setUser.add(name);
                }
            } while (people.moveToNext());
        }
    }
}