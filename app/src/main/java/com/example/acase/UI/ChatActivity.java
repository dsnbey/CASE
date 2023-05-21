package com.example.acase.UI;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.acase.Common;
import com.example.acase.Model.Chat;
import com.example.acase.R;
import com.example.acase.UC.SendChatService;
import com.example.acase.databinding.ActivityChatBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import java.util.Queue;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.AndroidEntryPoint;


public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding b;
    SendChatService sendChatService;
    FirebaseRecyclerAdapter<Chat, RecyclerView.ViewHolder> adapter;
    LinearLayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChatBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        sendChatService = new SendChatService();

        initViews();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadChatContent();
        if (adapter != null) {
            adapter.startListening();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void initViews() {

        layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        // send normal text
        b.imgSend.setOnClickListener(e -> {
            String message = b.edtChat.getText().toString().trim();
            Chat chat = new Chat(0, message, false);
            sendChatService.sendChat(chat);
            b.edtChat.setText("");

        });

        b.imgMic.setOnClickListener(e -> {
            b.recordingLyt.setVisibility(View.VISIBLE);
            String message = recordVoice();
            Chat chat = new Chat(0, message, true);
        });

        b.imgMemory.setOnClickListener(e -> {
            Intent intent = new Intent(this, MemorizationActivity.class);
            startActivity(intent);
        });
    }

    private void loadChatContent() {
        Query query = Common.ref.child("Conversations").child(Common.id);
        FirebaseRecyclerOptions<Chat> options =
                new FirebaseRecyclerOptions.Builder<Chat>()
                        .setQuery(query, Chat.class)
                        .build();

        ;
        b.recyclerChatCA.setLayoutManager(layoutManager);



        adapter = new FirebaseRecyclerAdapter<Chat,  RecyclerView.ViewHolder>(options) {



            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Chat model) {

                if (holder instanceof OwnChatHolder) {
                    OwnChatHolder ownChatHolder = (OwnChatHolder) holder;
                    ownChatHolder.txtOwnChatMessage.setText(model.getMessage());
                }

                else if (holder instanceof AIChatHolder) {
                    AIChatHolder aiChatHolder = (AIChatHolder) holder;
                    aiChatHolder.txtAIMessage.setText(model.getMessage());
                    aiChatHolder.imgMic.setVisibility(View.VISIBLE);
                    aiChatHolder.imgMic.setOnClickListener(e -> {
                        playContent(model.getMessage());
                    });
                }
            }

            @Override
            public int getItemViewType(int position) {
                return adapter.getItem(position).getSender();
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if (viewType == 0) {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.own_chat_item, parent, false);
                    return new OwnChatHolder(view);
                }
                else {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.ai_chat_item, parent, false);
                    return new AIChatHolder(view);
                }


            }
        };

        // Auto scroll when receive new message
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void  onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1)))
                {
                    b.recyclerChatCA.scrollToPosition(positionStart);
                }
            }
        });

        b.recyclerChatCA.setAdapter(adapter);
    }


    private String recordVoice() {
        return "";
    }

    private void playContent(String message) {
        Log.d(TAG, "playContent: ");
    }
}