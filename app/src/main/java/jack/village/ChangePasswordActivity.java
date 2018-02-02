package jack.village;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText changeEmail;
    EditText changePassword;
    FirebaseAuth auth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        changeEmail = findViewById(R.id.changeEmail);
        changePassword = findViewById(R.id.changePassword);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
    }

    public void change (View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){

            String email = changeEmail.getText().toString().trim();
            String password = changePassword.getText().toString().trim();

            if(email.isEmpty()){
                changeEmail.setError("Email Required");
                changeEmail.requestFocus();
                return;
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                changeEmail.setError("Please Enter A Valid Email");
                changeEmail.requestFocus();
                return;
            }

            if(password.isEmpty()){
                changePassword.setError("Password Required");
                changePassword.requestFocus();
                return;
            }

            if(password.length()<8){
                changePassword.setError("Password Must Be At Least 8 Characters Long");
                changePassword.requestFocus();
                return;
            }
            dialog.setMessage("Updating Account, Please Wait");
            dialog.show();
            user.updateEmail(email);
            user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Account Updated", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        finish();
                        Intent login = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        finishAffinity();
                        startActivity(login);
                    }else{
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
