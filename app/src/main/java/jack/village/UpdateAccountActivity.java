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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateAccountActivity extends AppCompatActivity {

    EditText changeEmail;
    EditText changePassword;
    EditText changeName;
    FirebaseAuth auth;
    ProgressBar progressBar;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_account);

        changeEmail = findViewById(R.id.changeEmail);
        changePassword = findViewById(R.id.changePassword);
        changeName = findViewById(R.id.changeName);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference().child("Users");
        progressBar = findViewById(R.id.progressbar);
    }

    public void change (View view){
        //Get the current user information
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Fail safe check to ensure a user is definitely logged in before allowing them to change their password
        if(user!=null){

            //Get new email and password
            final String userName = changeName.getText().toString().trim();
            String email = changeEmail.getText().toString().trim();
            String password = changePassword.getText().toString().trim();

            //Ensure email is not empty, set focus on email field if it is
            if(userName.isEmpty()){
                changeName.setError("Name Required");
                changeName.requestFocus();
                return;
            }

            //Ensure email is not empty, set focus on email field if it is
            if(email.isEmpty()){
                changeEmail.setError("Email Required");
                changeEmail.requestFocus();
                return;
            }

            //Ensure email is and email, set focus on email field if not
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                changeEmail.setError("Please Enter A Valid Email");
                changeEmail.requestFocus();
                return;
            }

            //Ensure Password is not empty, set focus on password field if it is
            if(password.isEmpty()){
                changePassword.setError("Password Required");
                changePassword.requestFocus();
                return;
            }

            //Ensure password is at least 8 characters long (security), if not set focus on password field
            if(password.length()<8){
                changePassword.setError("Password Must Be At Least 8 Characters Long");
                changePassword.requestFocus();
                return;
            }

            //Display Progress Bar
            progressBar.setVisibility(View.VISIBLE);

            //Update email and password
            user.updateEmail(email);
            user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        String user_id = auth.getCurrentUser().getUid();
                        DatabaseReference current_user = database.child(user_id);
                        current_user.child("Name").setValue(userName);

                        //Clear Progress Bar
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Account Updated", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        finish();
                        Intent login = new Intent(UpdateAccountActivity.this, LoginActivity.class);
                        finishAffinity();
                        startActivity(login);
                    }else{

                        //Clear Progress Bar
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
