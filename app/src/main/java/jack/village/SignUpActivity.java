package jack.village;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextEmail, editTextPassword;

    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        mAuth = FirebaseAuth.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        findViewById(R.id.buttonSignUp).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);

    }

    private void registerUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //Check to ensure email is not empty, focus field if it is
        if(email.isEmpty()){
            editTextEmail.setError("Email Required");
            editTextEmail.requestFocus();
            return;
        }

        //Check to ensure an email has been entered, set focus to email if it hasn't
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please Enter A Valid Email");
            editTextEmail.requestFocus();
            return;
        }

        //Check to ensure the password is not empty, set focus to password if it is
        if(password.isEmpty()){
            editTextPassword.setError("Password Required");
            editTextPassword.requestFocus();
            return;
        }

        //Validate password is over 8 characters long
        if(password.length()<8){
            editTextPassword.setError("Password Must Be At Least 8 Characters Long");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //Create new user in the database
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);

                //If successful finish the activity, give the user a notification it was successful and open Main Activity
                if(task.isSuccessful()){
                    finish();
                    Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent j = new Intent(SignUpActivity.this, MainActivity.class);
                    j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(j);
                }else {
                    if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "Email Already Registered", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //On click open sign up activity
            case R.id.buttonSignUp:
                registerUser();
                break;

            case R.id.textViewLogin:
                //On click finish activity and open Login Activity
                finish();
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
        }
    }
}
