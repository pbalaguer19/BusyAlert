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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pau.busyalert.Adapters.UserAdapter;
import com.example.pau.busyalert.Interfaces.HerokuEndpointInterface;
import com.example.pau.busyalert.JavaClasses.ApiUtils;
import com.example.pau.busyalert.JavaClasses.HerokuLog;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FavouritesFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
        View.OnClickListener {

    private UserAdapter mAdapter;
    private User[] contact_list = new User[0];
    private ListView listv;
    private EditText textSearch;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private boolean isSearrching = false; //Used for deleting in search
    private List<User> searchingList;
    private List<User> tmpList = new ArrayList<>();
    private Button emptyBtn;

    /**
     * FIREBASE
     **/
    private FirebaseAuth firebaseAuth;

    /**
     * HEROKU
     */
    private HerokuEndpointInterface apiService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        apiService = ApiUtils.getAPIService();

        getContacts();
        showContacts(contact_list);
        registerForContextMenu(listv);
        listv.setEmptyView(getView().findViewById( R.id.empty_list_view_fav ));

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

        /* Update Favourite list on ScrollUp */
        listv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;


            }

            private void isScrollCompleted() {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && this.currentScrollState == SCROLL_STATE_IDLE) {
                    contact_list = new User[0];
                    tmpList = new ArrayList<>();
                    getContacts();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);
        listv = (ListView) root.findViewById(android.R.id.list);
        textSearch = (EditText) root.findViewById(R.id.txtSearch);
        emptyBtn = (Button) root.findViewById(R.id.empty_list_view_fav);
        emptyBtn.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
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

        /* Update Favourite list on ScrollUp */
        listv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;


            }

            private void isScrollCompleted() {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && this.currentScrollState == SCROLL_STATE_IDLE) {
                    contact_list = new User[0];
                    tmpList = new ArrayList<>();
                    getContacts();
                }
            }
        });
    }

    private void getContacts(){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-favourites");
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
        ref.child(friendUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = "";
                    String status = "";
                    String phone = "";
                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                        if (childSnapshot.getKey().equals("username"))
                            name = childSnapshot.getValue(String.class);
                        if (childSnapshot.getKey().equals("status"))
                            status = childSnapshot.getValue(String.class);
                        if (childSnapshot.getKey().equals("phone"))
                            phone = childSnapshot.getValue(String.class);
                    }
                    tmpList.add(new User(name, status, phone));
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
        User removeUser;
        int counter = 0;
        while(iterator.hasNext()){
            removeUser = iterator.next();
            if(counter == position){
                iterator.remove();
                removeFromFirebase(removeUser);
            }
            counter++;
        }
        return list.toArray(new User[list.size()]);
    }

    private User[] remove_contact_searching(int position){
        User removeUser = searchingList.get(position);
        removeFromFirebase(removeUser);

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

    private void removeFromFirebase(User user){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-favourites");
        ref.child(uid).child(user.getPhone()).removeValue();

        ref = FirebaseDatabase.getInstance().getReference("users-phone");
        ref.child(user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                        String friendUid = childSnapshot.getKey();

                        String extra = "FriendUid: " + friendUid;
                        apiService.createLog(uid, "FAVOURITE_REMOVED", extra).enqueue(new Callback<HerokuLog>() {
                            @Override
                            public void onResponse(Call<HerokuLog> call, Response<HerokuLog> response) {
                            }

                            @Override
                            public void onFailure(Call<HerokuLog> call, Throwable t) {

                            }
                        });
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