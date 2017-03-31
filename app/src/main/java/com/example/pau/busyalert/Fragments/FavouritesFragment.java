package com.example.pau.busyalert.Fragments;


import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class FavouritesFragment extends ListFragment implements AdapterView.OnItemLongClickListener{
    private UserAdapter mAdapter;
    private User[] contact_list = new User[0];
    private ListView listv;
    private EditText textSearch;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private boolean isSearrching = false; //Used for deleting in search
    private List<User> searchingList;
    private List<User> tmpList = new ArrayList<>();

    /**
     * FIREBASE
     **/
    private FirebaseAuth firebaseAuth;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
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
                    isSearrching = false;
                }else{
                    isSearrching = true;
                    searchItem(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);
        listv = (ListView) root.findViewById(android.R.id.list);
        textSearch = (EditText) root.findViewById(R.id.txtSearch);
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

            if(isSearrching)
                contact_list = remove_contact_searching((int)info.id);
            else
                contact_list = remove_contact((int)info.id);

            showContacts(contact_list);
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(),R.string.toast_deleted, Toast.LENGTH_LONG).show();
        }
        return super.onContextItemSelected(item);
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

    private void showContacts(User[] contact_list){
        mAdapter = new UserAdapter(getContext(), R.layout.contact_list, contact_list);
        listv.setAdapter(mAdapter);
    }

    private void searchItem(String text){
        searchingList = new ArrayList<>();
        for(User u: contact_list){
            if(u != null && u.getName().toLowerCase().startsWith(text.toLowerCase()))
                searchingList.add(u);
        }
        showContacts(searchingList.toArray(new User[searchingList.size()]));
    }

    private User[] remove_contact(int position){
        List<User> list = new ArrayList<>(Arrays.asList(contact_list));
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

    private User[] remove_contact_searching(int position){
        User removeUser = searchingList.get(position);

        List<User> list = new ArrayList<>(Arrays.asList(contact_list));
        Iterator<User> iterator = list.iterator();
        while(iterator.hasNext()){
            User actualUser = iterator.next();
            if(actualUser == removeUser)
                iterator.remove();
        }

        if(textSearch.isEnabled())
            textSearch.setText("");
        return list.toArray(new User[list.size()]);
    }
}