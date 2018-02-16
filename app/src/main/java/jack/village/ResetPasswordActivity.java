package jack.village;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText userEmail;

    private Button reset;

    private FirebaseAuth auth;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_password);

        userEmail = findViewById(R.id.email);

        reset = findViewById(R.id.resetPassword);


        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        //On click listener on reset button
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = userEmail.getText().toString().trim();

                //Give fault if email is empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter a registered email", Toast.LENGTH_SHORT).show();
                    return;
                }
                    resetPassword(email);
            }
        });
    }

    private void resetPassword(String email){
        progressBar.setVisibility(View.VISIBLE);

        //Send password reset to users email
        auth.sendPasswordResetEmail(email)

                //On complete give user completion feedback
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}