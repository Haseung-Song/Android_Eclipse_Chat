package com.example.testchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.Connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                EditText server = findViewById(R.id.Server);
                EditText nickname = findViewById(R.id.Nickname);
                intent.putExtra("server", server.getText().toString());
                intent.putExtra("nickname", nickname.getText().toString());
                startActivity(intent);
                finish();
            }
        });
    }
}