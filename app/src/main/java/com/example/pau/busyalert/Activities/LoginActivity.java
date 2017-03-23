package com.example.pau.busyalert.Activities;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginBtn;
    private EditText email;
    private EditText password;

    /** FIREBASE **/
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.userEmail);
        password =(EditText) findViewById(R.id.userPassword);
        loginBtn = (Button) findViewById(R.id.login_button);
        loginBtn.setOnClickListener(this);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onClick(View view) {
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful())
                            Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(getApplicationContext(), "Connection completed", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
                });
    }
}
