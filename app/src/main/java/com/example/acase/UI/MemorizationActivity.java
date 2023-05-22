package com.example.acase.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.acase.R;
import com.example.acase.databinding.ActivityChatBinding;
import com.example.acase.databinding.ActivityMemorizationBinding;

/**
 * Directs you to upload text or pdf
 */
public class MemorizationActivity extends AppCompatActivity {

    ActivityMemorizationBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMemorizationBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        b.btnUploadText.setOnClickListener(e -> {
            Intent intent = new Intent(MemorizationActivity.this, UploadTextActivity.class);
            startActivity(intent);
        });

        b.btnUploadPdf.setOnClickListener(e -> {
            Intent intent = new Intent(MemorizationActivity.this, UploadPDFActivity.class);
            startActivity(intent);
        });

        b.btnUploadCancel.setOnClickListener(e -> {
            Intent intent = new Intent(MemorizationActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }
}