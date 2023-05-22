package com.example.acase.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.acase.UC.ClientAuth;
import com.example.acase.databinding.ActivityLoginBinding;

/**
 * Entry point of application, allows individual experiences.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        b.btnLogin.setOnClickListener(e-> {
            if (getEditTextTexts()) {
                if (ClientAuth.authenticateUser(username, password)) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(this, "Wrong credentials", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private boolean getEditTextTexts() {
        username = b.edtUsername.getText().toString().trim();
        password = b.edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter credentials", Toast.LENGTH_SHORT).show();
            username = null;
            password = null;
            return false;
        }
        else {
            return true;
        }
    }


}