package jack.village;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

public class ConnectionCardActivity extends AppCompatActivity implements View.OnClickListener{

    EditText nameField, dobField, addressField, emailField, phoneField, commentsField;
    String name, dob, address, email, phone, comments, relation, discovered, next;
    CheckBox committing, baptised, joinmc, member, volunteer, contacted;
    RadioButton married, single, website, social, friend;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_card);

        nameField = findViewById(R.id.name);
        dobField = findViewById(R.id.dob);
        addressField = findViewById(R.id.address);
        emailField = findViewById(R.id.email);
        phoneField = findViewById(R.id.phone);
        commentsField = findViewById(R.id.comments);
        married = findViewById(R.id.married);
        single = findViewById(R.id.single);
        website = findViewById(R.id.website);
        social = findViewById(R.id.social);
        friend = findViewById(R.id.friend);
        committing = findViewById(R.id.committing);
        baptised = findViewById(R.id.baptised);
        joinmc = findViewById(R.id.joinmc);
        member = findViewById(R.id.member);
        volunteer = findViewById(R.id.volunteer);
        contacted = findViewById(R.id.contacted);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(this);

    }

    private void sendCard(){



        name = nameField.getText().toString();
        dob = dobField.getText().toString();
        address = addressField.getText().toString();
        email = emailField.getText().toString();
        phone = phoneField.getText().toString();
        comments = commentsField.getText().toString();

        if(name.isEmpty()){
            nameField.setError("Name Required");
            nameField.requestFocus();
            return;
        }

        if(dob.isEmpty()){
            dobField.setError("DOB Required");
            dobField.requestFocus();
            return;
        }

        if(address.isEmpty()){
            addressField.setError("Address Required");
            addressField.requestFocus();
            return;
        }

        if(email.isEmpty()){
            emailField.setError("Email Required");
            emailField.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailField.setError("Please Enter A Valid Email");
            emailField.requestFocus();
            return;
        }

        if(!Patterns.PHONE.matcher(phone).matches()){
            phoneField.setError("Please Enter A Valid Phone Number");
            phoneField.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            phoneField.setError("Phone Number Required");
            phoneField.requestFocus();
            return;
        }

        if(married.isChecked()){
            relation = "Married";
        }

        if(single.isChecked()){
            relation = "Single";
        }

        if(website.isChecked()){
            discovered = "Website";
        }

        if(social.isChecked()){
            discovered = "Social Media";
        }

        if(friend.isChecked()){
            discovered = "Friend";
        }

        if(committing.isChecked()){
            next = "I want to know more about committing my life to Jesus \n";
        }

        if(baptised.isChecked()){
            next = next + "\n I'd like to be baptised \n";
        }

        if(joinmc.isChecked()){
            next = next + "\n I'm interested in joining a missional community \n";
        }

        if(member.isChecked()){
            next = next + "\n I'm interested in becoming a member \n";
        }

        if(volunteer.isChecked()){
            next = next + "\n I'd like to volunteer in a ministry \n";
        }

        if(contacted.isChecked()){
            next = next + "\n I'd like to be contacted by a leader \n";
        }

        String[] TO = {"jrobb6696@gmail.com"};


        String output = "Name: " + "\n" + name + "\n\n Birthday: " + "\n" + dob + "\n\n Address: " + "\n" + address + "\n\n Email: " + "\n" + email + "\n\n Phone: " + "\n" + phone + "\n\n Marital Status: " + "\n" + relation
                + "\n\n How did you hear about us?: " + "\n" + discovered + "\n\n What's next?: " + "\n" + next + "\n\n Additional Comments: " + "\n" + comments;

        Intent mail = new Intent(Intent.ACTION_SEND);
        mail.putExtra(Intent.EXTRA_EMAIL, TO);
        mail.putExtra(Intent.EXTRA_SUBJECT, "Connection Card");
        mail.putExtra(Intent.EXTRA_TEXT, output);
        mail.setType("message/rfc822");
        startActivity(Intent.createChooser(mail, "Send email via:"));
    }


    @Override
    public void onClick(View view) {
        //Switch between the different options
        switch(view.getId()){
            case R.id.submit:
                sendCard();
                finish();
        }
    }

}
