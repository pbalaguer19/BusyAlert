package com.example.pau.busyalert.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pau.busyalert.Interfaces.HerokuEndpointInterface;
import com.example.pau.busyalert.JavaClasses.ApiUtils;
import com.example.pau.busyalert.JavaClasses.HerokuLog;
import com.example.pau.busyalert.JavaClasses.UserInfo;
import com.example.pau.busyalert.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener{

    EditText email, password1, password2, phone;
    Button btn;

    private boolean isSureToContinue = false;

    /** Identifier for the permission request **/
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;

    /** PHONE NUMBERS **/
    private Set<String> phones;

    /**
     * FIREBASE
     **/
    private FirebaseAuth firebaseAuth;

    /**
     * HEROKU
     */
    private HerokuEndpointInterface apiService;

    /*
     * Registration activity handles the following steps:
     *  1. Create User
     *  2. Save user Information in Firebase Database
     *  3. Save all the phone-contacts with the app installed
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn = (Button) findViewById(R.id.create_account_button);
        email = (EditText) findViewById(R.id.userEmail);
        password1 = (EditText) findViewById(R.id.userPassword1);
        password2 = (EditText) findViewById(R.id.userPassword2);
        phone = (EditText) findViewById(R.id.phone);

        btn.setOnClickListener(this);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        apiService = ApiUtils.getAPIService();
    }

    //ONCLICK VIEW//
    @Override
    public void onClick(View v) {
        final String pass1 = password1.getText().toString();
        final String pass2 = password2.getText().toString();
        final String mail = email.getText().toString();
        final String phoneNumber = phone.getText().toString();

        checkNetwork();

        if(this.isSureToContinue){
            if(!mail.isEmpty()){
                if(!phoneNumber.isEmpty()){
                    if(pass1.length() < 6)
                        Toast.makeText(getApplicationContext(), getString(R.string.bad_password), Toast.LENGTH_SHORT).show();
                    else if(pass1.equals(pass2)){
                        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), pass1)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(!task.isSuccessful())
                                            Toast.makeText(RegisterActivity.this, getString(R.string.registration_no), Toast.LENGTH_SHORT).show();
                                        else {
                                            saveInfo(mail, phoneNumber);
                                            Toast.makeText(RegisterActivity.this, getString(R.string.registration_ok), Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                });
                    }else
                        Toast.makeText(getApplicationContext(), getString(R.string.no_password), Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getApplicationContext(), R.string.no_phone, Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(getApplicationContext(), R.string.no_mail, Toast.LENGTH_SHORT).show();
        }
    }

    //ONCLICK DIALOG INTERFACE//
    @Override
    public void onClick(DialogInterface dialog, int which) {
        this.isSureToContinue = true;
    }

    private void checkNetwork(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo.getType() != ConnectivityManager.TYPE_WIFI){
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_wifi_continue)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, null)
                    .show();
        }else{
            this.isSureToContinue = true;
        }
    }

    private void saveInfo(String mail, String phoneNumber){
        saveUserInfo(mail, phoneNumber);
        saveUserPhoneRelation(phoneNumber);
        saveUserFriends();
    }

    private void saveUserInfo(String mail, String phone){
        String uid = firebaseAuth.getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        UserInfo userInfo = new UserInfo(mail.split("@")[0], mail, phone, token);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).setValue(userInfo);

        String extra = "Email: " + mail + " | Phone: " + phone;
        apiService.createLog(uid, "NEW_USER", extra).enqueue(new Callback<HerokuLog>() {
            @Override
            public void onResponse(Call<HerokuLog> call, Response<HerokuLog> response) {
            }

            @Override
            public void onFailure(Call<HerokuLog> call, Throwable t) {

            }
        });

    }

    private void saveUserPhoneRelation(String phone){
        String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-phone");
        ref.child(phone).child(uid).setValue(true);
    }

    private void saveUserFriends(){
        getPermissionToReadUserContacts();
        getContacts();
        saveUserContacts();
    }

    private void getPermissionToReadUserContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {}
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }

    private void getContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = this.getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        phones = new HashSet<>();
        if (people.moveToFirst()) {
            do {
                String phone = people.getString(indexName).replace(" ", "");
                if(phone != null && !phone.equals("") &&
                        (phone.length() == 9 || phone.length() == 12) &&
                        phone.contains("6")){
                    while (!phone.startsWith("6")){
                        phone = phone.substring(1);
                    }
                    phones.add(phone);
                }
            } while (people.moveToNext());
        }
    }


    private void saveUserContacts(){
        for (String phone: phones){
            if(phone.length() == 9)
                saveContact(phone);
        }
    }

    private void saveContact(String contact){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        final String phone = contact;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-phone");

        ref.child(contact).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-contacts");
                    ref.child(uid).child(phone).setValue(true);
                }
                else {
                    // The contact has not the app
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
