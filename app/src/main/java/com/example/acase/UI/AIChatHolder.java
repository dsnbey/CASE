package com.example.acase.UI;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acase.R;

/**
 * Used by recyclerview (dynamic list)
 */
public class AIChatHolder extends RecyclerView.ViewHolder{

    TextView txtAIMessage;

    public AIChatHolder(@NonNull View itemView) {
        super(itemView);
        txtAIMessage = itemView.findViewById(R.id.txt_ai_chat_message);

    }
}
