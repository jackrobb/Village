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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Entry point for Firebase Authentication
    private FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        //Obtain an instance of the FirebaseAuth class
        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressbar);

        findViewById(R.id.textViewSignup).setOnClickListener(this);
        findViewById(R.id.textViewResetPassword).setOnClickListener(this);
        findViewById(R.id.buttonLogin).setOnClickListener(this);
        findViewById(R.id.guestLogin).setOnClickListener(this);
    }

    private void userLogin(){

        //Retrieve user email and password
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //Ensure email is not empty, set focus on email field if it is
        if(email.isEmpty()){
            editTextEmail.setError("Email Required");
            editTextEmail.requestFocus();
            return;
        }

        //Ensure email is an email, set focus on email field of it is not
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please Enter A Valid Email");
            editTextEmail.requestFocus();
            return;
        }

        //Ensure password is not empty, set focus on password if it is
        if(password.isEmpty()){
            editTextPassword.setError("Password Required");
            editTextPassword.requestFocus();
            return;
        }

        //Display Progress Bar
        progressBar.setVisibility(View.VISIBLE);

        //Sign into Firebase using email and password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //Clear Progress Bar
                progressBar.setVisibility(View.GONE);

                //If successful close login activity, start new activity Main activity
                if(task.isSuccessful()){
                    finish();
                    Intent j = new Intent(LoginActivity.this, MainActivity.class);
                    j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(j);
                }else{
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void guestLogin() {
        finish();
        Intent j = new Intent(LoginActivity.this, MainActivity.class);
        j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(j);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //On Start check for current user, if someone is logged in close login activity and open Main Activity
        if(mAuth.getCurrentUser() !=null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        //Switch between the different options
        switch(view.getId()){
            case R.id.textViewSignup:
                Intent signUp = new Intent(this, SignUpActivity.class);
                startActivity(signUp);
                break;

            case R.id.textViewResetPassword:
                Intent reset = new Intent(this, ResetPasswordActivity.class);
                startActivity(reset);
                break;

            case R.id.guestLogin:
                guestLogin();
                break;

            case R.id.buttonLogin:
                userLogin();
                break;
        }
    }
}
