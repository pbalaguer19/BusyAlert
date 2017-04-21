package com.example.pau.busyalert.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginBtn, registerBtn;
    private EditText email;
    private EditText password;

    /**
     * FIREBASE
     **/
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.userEmail);
        password = (EditText) findViewById(R.id.userPassword);
        loginBtn = (Button) findViewById(R.id.login_button);
        registerBtn = (Button) findViewById(R.id.register);
        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register:
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.login_button:
                String mail = email.getText().toString();
                if(!mail.isEmpty()){
                    firebaseAuth.signInWithEmailAndPassword(mail, password.getText().toString())
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful())
                                        Toast.makeText(getApplicationContext(), getString(R.string.connection_no), Toast.LENGTH_SHORT).show();
                                    else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.connection_ok), Toast.LENGTH_SHORT).show();
                                        getUserInfo();
                                    }
                                }
                            });
                }else
                    Toast.makeText(this, R.string.no_mail, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void getUserInfo(){
        String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dSnap: snapshot.getChildren()) {
                        if(dSnap.getKey().equals("username")) {
                            String value = dSnap.getValue(String.class);
                            sharedPreferences.edit().putString("username_key", value).apply();
                        }else if(dSnap.getKey().equals("status")) {
                            String value = dSnap.getValue(String.class);
                            sharedPreferences.edit().putString("status", value).apply();
                        }
                    }
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
