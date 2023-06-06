package com.example.city_sight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // todo: client parcable. передавать целый объект, а не по отдельности. запрос на свою геолокацию.
    // todo: введите радиус - кусок класса user. достопримечательности вокруг улк/гз
    Button nextActivity;


    User user;
    String name;
    String surname;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextActivity = findViewById(R.id.nextActivity);
        nextActivity.setOnClickListener(this);

        final EditText editText1 = findViewById(R.id.surnameInput);
        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                surname = s.toString();
            }
        });


        final EditText editText2 = findViewById(R.id.nameInput);
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                name = s.toString();
            }
        });

        final EditText editText3 = findViewById(R.id.emailInput);
        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                email = s.toString();
            }
        });
    }

    @Override
    public void onClick(View v) {
        user = new User(name, surname, email);
        if (user.getName() == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Имя является обезательным!", Toast.LENGTH_SHORT);
            toast.show();
        } else if (user.getSurname() == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Фамилия является обезательным!", Toast.LENGTH_SHORT);
            toast.show();
        } else if (user.getEmail() == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "mail является обезательным!", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Intent intent = new Intent(this, SightList.class);
            intent.putExtra(User.class.getSimpleName(), user);
            startActivity(intent);
        }
    }
}