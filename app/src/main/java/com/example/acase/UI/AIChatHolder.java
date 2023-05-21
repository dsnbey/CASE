package com.example.acase.UI;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acase.R;

public class AIChatHolder extends RecyclerView.ViewHolder{

    TextView txtAIMessage;
    ImageView imgMic;

    public AIChatHolder(@NonNull View itemView) {
        super(itemView);
        txtAIMessage = itemView.findViewById(R.id.txt_ai_chat_message);
        imgMic = itemView.findViewById(R.id.img_hear_chat);
    }
}
