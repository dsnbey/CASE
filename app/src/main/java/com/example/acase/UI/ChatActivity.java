package com.example.acase.UI;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.STORAGE;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.acase.Common;
import android.Manifest;
import android.widget.Toast;

import com.example.acase.Model.Chat;
import com.example.acase.R;
import com.example.acase.UC.OpenAiApiClient;
import com.example.acase.UC.SendChatService;
import com.example.acase.databinding.ActivityChatBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.permissionx.guolindev.PermissionX;


import java.io.File;
import java.io.IOException;


/**
 * Main chatting activity
 * NOTE: Emulator does not microphone. Therefore, I only tested text-to-speech with logs
 * (It works, but circumstances might change with device capabilities. In production, it needs to
 * be tested rigorously.)
 * Left button sends you to upload big chunks of data. (Upload infinite if you may, it runs
 * non-blocking and convolizes to small embeddings (will be elaborated in respective Class).
 *
 */
public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding b;
    SendChatService sendChatService;
    FirebaseRecyclerAdapter<Chat, RecyclerView.ViewHolder> adapter;
    LinearLayoutManager layoutManager;
    OpenAiApiClient openAiApiClient = OpenAiApiClient.getInstancePassRef(this);

    // FOR AUDIO RECORDING
    // creating a variable for media recorder object class.
    private MediaRecorder mRecorder;

    // creating a variable for mediaplayer class
    private MediaPlayer mPlayer;

    // string variable is created for storing a file name
    private static String mFileName = null;

    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChatBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        sendChatService = new SendChatService();



        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(ChatActivity.this, permissions, REQUEST_AUDIO_PERMISSION_CODE);

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
            Chat chat = new Chat(0, message, false, false);
            sendChatService.sendChat(chat);
            Log.d(TAG, "initViews: " + chat.getMessage());
            b.edtChat.setText("");

        });


        b.imgMic.setOnClickListener(e -> {
            b.recordingLyt.setVisibility(View.VISIBLE);
            startRecording();
        });

        b.endRecordingCa.setOnClickListener(ev -> {
            pauseRecording();
            b.recordingLyt.setVisibility(View.GONE);
            Log.d(TAG, "initViews: pauseRecording");
            Log.d(TAG, "initViews: " + mFileName);
            openAiApiClient.transcribeAudio(mFileName).thenAccept(result -> {
                        Log.d(TAG, "initViews: thenAc" );
                    })
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        Log.d(TAG, "initViews: thnot" + throwable.getMessage());
                        return null;
                    });


        });

        b.imgMemory.setOnClickListener(e -> {
            Intent intent = new Intent(this, MemorizationActivity.class);
            startActivity(intent);
        });


    }

    private void loadChatContent() {
        Query query = Common.ref.child("Conversations").child(Common.id).orderByChild("deepMemory").equalTo(false);
        FirebaseRecyclerOptions<Chat> options =
                new FirebaseRecyclerOptions.Builder<Chat>()
                        .setQuery(query, Chat.class)
                        .build();

        
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


    /// for audio

    private void startRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record and store the audio.
        if (CheckPermissions()) {

            try {
                Log.d(TAG, "startRecording: p checked");


                Context context = getApplicationContext();
                File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                mFileName = storageDir.getAbsolutePath() + "/AudioRecording1.mp3";


                mRecorder = new MediaRecorder();


                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);


                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

                mRecorder.setOutputFile(mFileName);
                try {

                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
                    Log.e("TAG", "prepare() failed");
                    Log.d(TAG, "startRecording: " + e.getMessage());
                }
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else {
            // if audio recording permissions are
            // not granted by user below method will
            // ask for runtime permission for mic and storage.
            requestPermissions();
            Toast.makeText(this, "permission requested", Toast.LENGTH_SHORT).show();
            //startRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Log.d(TAG, "onRequestPermissionsResult: approved" );

                    } else {


                        Toast.makeText(getApplicationContext(), "Microphone Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        PermissionX.init(ChatActivity.this)
                .permissions(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, STORAGE, MANAGE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Toast.makeText(ChatActivity.this, "All permissions are granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "These permissions are denied: ", Toast.LENGTH_LONG).show();
                        for (String  s : deniedList) {
                            b.recordingLyt.setVisibility(View.GONE);
                            //Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
                        }
                        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(ChatActivity.this, permissions, REQUEST_AUDIO_PERMISSION_CODE);
                    }

                    });
                };




    public void pauseRecording() {

        mRecorder.stop();

        mRecorder.release();
        mRecorder = null;
    }


    public ActivityChatBinding getB() {
        return b;
    }


}