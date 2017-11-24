package com.tru.firechat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener{
    protected static String mUsername;
    protected static String mPhotoUrl;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    //firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    //google
    private GoogleApiClient mGoogleApiClient;
    //
    private RecyclerView recyclerView;
    //
    private EditText messageBox;
    private ImageView sendphoto;
    private  ImageButton sendmessage;
    //
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView titleTextView;
        ImageView attachment;
        ImageView messenger;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            titleTextView = (TextView) itemView.findViewById(R.id.senderTextview);
            attachment = (ImageView) itemView.findViewById(R.id.messageattachmentview);
            messenger= (ImageView) itemView.findViewById(R.id.circularimageview);
        }
    }
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }
        recyclerView = (RecyclerView)(findViewById(R.id.messagesRecyclerView));
        LinearLayoutManager l = new LinearLayoutManager(this);
        l.setStackFromEnd(true);
        recyclerView.setLayoutManager(l);


        this.setTitle("Messages");

        SnapshotParser<Message> parser = new SnapshotParser<Message>() {
            @Override
            public Message parseSnapshot(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    message.setId(dataSnapshot.getKey());
                }
                return message;
            }
        };

        final DatabaseReference reference= FirebaseDatabase.getInstance().getReference("messages");
        final Query ref = FirebaseDatabase.getInstance().getReference().child("messages").limitToLast(20);

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(ref, parser)
                        .build();
        mFirebaseAdapter= new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final MessageViewHolder holder, int position, Message model) {
                    if(model.getText()!=null){
                        holder.messageTextView.setText(model.getText());
                        holder.messageTextView.setVisibility(TextView.VISIBLE);
                        holder.attachment.setVisibility(ImageView.GONE);
                       // holder.messenger.setVisibility(ImageView.GONE);
                    }else {
                        String imageurl=model.getImageUrl();
                        if(imageurl.startsWith("gs://")){
                            StorageReference storageReference= FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(imageurl);
                            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        String downloadurl = task.getResult().toString();
                                        Glide.with(holder.attachment.getContext()).load(downloadurl)
                                                .into(holder.attachment);

                                    }else Log.w(TAG,"getting image failed");
                                }
                            });
                        } else {
                            Glide.with(holder.attachment.getContext()).load(model.getImageUrl())
                                    .into(holder.attachment);
                        }
                        holder.attachment.setVisibility(ImageView.VISIBLE);
                        holder.messageTextView.setVisibility(TextView.GONE);
                    }
                    holder.titleTextView.setText(model.getName());
                    if(model.getImageUrl()==null){
                        holder.messenger
                                .setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                                                R.drawable.ic_account_circle_48px));
                    }else {
                        Glide.with(MainActivity.this)
                                .load(model.getPhotoUrl()).into(holder.messenger);
                    }

            }

            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater =LayoutInflater.from(parent.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.layout_message,parent,false));
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

            }
        });

            messageBox= (EditText) findViewById(R.id.message);
            messageBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.toString().trim().length()>0){
                        sendmessage.setEnabled(true);
                    }else {
                        sendmessage.setEnabled(false);
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            sendphoto= (ImageView)findViewById(R.id.sendphoto);
        sendphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
        sendmessage= (ImageButton) findViewById(R.id.sendbutton);
        sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Message message = new Message(messageBox.getText().toString(),
                       mUsername,mPhotoUrl,null);
               reference.push().setValue(message);
               messageBox.setText("");
            }
        });
        recyclerView.setAdapter(mFirebaseAdapter);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser=null;
                mUsername=null;
                mPhotoUrl=null;
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                return true;
                default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected())
        mGoogleApiClient.disconnect();
        mFirebaseAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
         mGoogleApiClient.connect();
         mFirebaseAdapter.startListening();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    Message tempMessage = new Message(null, mUsername, mPhotoUrl,
                            LOADING_IMAGE_URL);
                    reference.child("messages").push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(mFirebaseUser.getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }}
                            });
                }
            }
        }
    }
    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        DatabaseReference reference1= FirebaseDatabase.getInstance().getReference();
                        if (task.isSuccessful()) {
                            Message Message =
                                    new Message(null, mUsername,task.getResult().getDownloadUrl()
                                            .toString(),mPhotoUrl);
                            reference1.child("messages").child(key)
                                    .setValue(Message);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }


}
