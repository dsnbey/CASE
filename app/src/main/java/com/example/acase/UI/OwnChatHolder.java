package com.example.acase.UI;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.acase.R;


public class OwnChatHolder extends RecyclerView.ViewHolder {


    TextView txtOwnChatMessage;



    public OwnChatHolder(View itemView) {
        super(itemView);

        txtOwnChatMessage = itemView.findViewById(R.id.txt_own_chat_message);

    }
}
