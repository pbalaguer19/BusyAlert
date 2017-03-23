package com.example.pau.busyalert.Activities;

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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    EditText email, password1, password2;
    Button btn;

    /**
     * FIREBASE
     **/
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn = (Button) findViewById(R.id.create_account_button);
        email = (EditText) findViewById(R.id.email);
        password1 = (EditText) findViewById(R.id.userPassword1);
        password2 = (EditText) findViewById(R.id.userPassword2);

        btn.setOnClickListener(this);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        String pass1 = password1.getText().toString();
        String pass2 = password2.getText().toString();

        if(pass1.length() < 5)
            Toast.makeText(getApplicationContext(), getString(R.string.bad_password), Toast.LENGTH_SHORT).show();
        else if(pass1.equals(pass2)){
            firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), pass1)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful())
                                Toast.makeText(getApplicationContext(), getString(R.string.registration_no), Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(getApplicationContext(), getString(R.string.registration_ok), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.no_password), Toast.LENGTH_SHORT).show();
        }
    }
}
