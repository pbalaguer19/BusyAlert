package com.example.pau.busyalert.Fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArraySet;
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

import com.example.pau.busyalert.Adapters.UserAdapter;
import com.example.pau.busyalert.R;
import com.example.pau.busyalert.JavaClasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SocialFragment extends ListFragment implements AdapterView.OnItemLongClickListener {
    private UserAdapter mAdapter;
    private User[] contact_list = new User[0];
    private ListView listv;
    private EditText textSearch;
    private Set<String> setUser = new ArraySet<>(); //Used for duplicated contacts
    private static final int CONTACT_ID = Menu.FIRST + 2;
    private boolean firstTime = true;
    private List<User> tmpList = new ArrayList<>();

    /**
     * FIREBASE
     **/
    private FirebaseAuth firebaseAuth;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firstTime){
            getContacts();
            firstTime = false;
        }
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

    private void getContacts(){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-contacts");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot friend : snapshot.getChildren()) {
                    String phoneNumber = friend.getKey();
                    getUidFromPhoneNumber(phoneNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveFriendInfo(String friendUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = "";
                    String status = "";
                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                        if (childSnapshot.getKey().equals("username"))
                            name = childSnapshot.getValue(String.class);
                        if (childSnapshot.getKey().equals("status"))
                            status = childSnapshot.getValue(String.class);
                    }
                    tmpList.add(new User(name, status));
                    contact_list = tmpList.toArray(new User[tmpList.size()]);
                    showContacts(contact_list);
                    mAdapter.notifyDataSetChanged();
                }
                else {}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getUidFromPhoneNumber(String phoneNumber) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-phone");
        ref.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                        saveFriendInfo(childSnapshot.getKey());
                    }
                }
                else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}