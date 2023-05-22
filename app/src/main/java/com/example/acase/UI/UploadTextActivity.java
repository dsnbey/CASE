package com.example.acase.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.acase.R;
import com.example.acase.UC.BigStringConvolizer;
import com.example.acase.databinding.ActivityMemorizationBinding;
import com.example.acase.databinding.ActivityUploadPdfactivityBinding;
import com.example.acase.databinding.ActivityUploadTextBinding;

/**
 * Allows you to upload big chunks of texts for the model to remember later.
 */
public class UploadTextActivity extends AppCompatActivity {

    ActivityUploadTextBinding b;
    BigStringConvolizer convolizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityUploadTextBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        convolizer = BigStringConvolizer.getInstance();

        b.btnUploadCancel.setOnClickListener(e -> {
            Intent intent = new Intent(UploadTextActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        b.btnUpload.setOnClickListener(e -> {
            Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show();
            convolizer.sendFile(b.edtBigText.getText().toString());
            Toast.makeText(this, "Finish!", Toast.LENGTH_SHORT).show();
            b.edtBigText.setText("");
            Intent intent = new Intent(UploadTextActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }
}