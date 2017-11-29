package com.tru.firechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    protected static String Username;
    protected static String profilePhotoUrl;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    //firebase variables
    private FirebaseAuth FirebaseAuthClient;//firebase auth was a pita for me
    private FirebaseUser FirebaseUser;
    final DatabaseReference reference= FirebaseDatabase.getInstance().getReference("messages");
    final Query ref = FirebaseDatabase.getInstance().getReference().child("messages").limitToLast(20);
    //google
    private GoogleApiClient GoogleApiClient;
    //content view
    private RecyclerView recyclerView;
    //messaging tools
    private EditText messageBox;
    private ImageView sendphoto;
    private  ImageButton sendMessageButton;

    //the magic
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        FirebaseAuthClient = com.google.firebase.auth.FirebaseAuth.getInstance();
        FirebaseUser = FirebaseAuthClient.getCurrentUser();

        if (FirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            Username = FirebaseUser.getDisplayName();
            if (FirebaseUser.getPhotoUrl() != null) {
                profilePhotoUrl = FirebaseUser.getPhotoUrl().toString();
            }

            GoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }

        recyclerView = findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager l = new LinearLayoutManager(this);
        l.setStackFromEnd(true);
        recyclerView.setLayoutManager(l);

        this.setTitle("Messages");
        Toast.makeText(getApplicationContext(),"welcome "+ Username,Toast.LENGTH_SHORT).show();

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

        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(ref, parser).build();

        mFirebaseAdapter= new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final MessageViewHolder holder, int position, final Message model) {
                if (model.getText() != null) {
                    holder.messageTextView.setText(model.getText());
                    holder.messageTextView.setVisibility(TextView.VISIBLE);
                    holder.attachment.setVisibility(ImageView.GONE);
                    Glide.with(MainActivity.this).load(model.getPhotoUrl()).into(holder.messenger);
                } else {
                    String imageurl = model.getAttachmentImageUrl();
                    if (imageurl != null) {
                        if (imageurl.startsWith("gs://")) {
                            StorageReference storageReference = FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(imageurl);

                            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String downloadurl = task.getResult().toString();
                                        Glide.with(holder.attachment.getContext()).load(downloadurl)
                                                .into(holder.attachment);

                                    } else Log.w(TAG, "getting image failed");
                                }
                            });
                        } else {
                            Glide.with(holder.attachment.getContext()).load(model.getAttachmentImageUrl())
                                    .into(holder.attachment);
                        }

                        holder.attachment.setVisibility(ImageView.VISIBLE);
                        holder.messageTextView.setVisibility(TextView.GONE);
                    }
                }
                holder.titleTextView.setText(model.getName());
                if(model.getPhotoUrl()==null){
                    holder.messenger
                            .setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                                    R.drawable.ic_account_circle_48px));
                }else {
                    Glide.with(MainActivity.this)
                            .load(model.getPhotoUrl()).into(holder.messenger);
                }
                // expand image attachment by sending its url via intent to ExpandPicture
                holder.attachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(model.getAttachmentImageUrl()!=null) {
                            Intent intent = new Intent(getBaseContext(), PictureExpandedActivity.class);
                            String imgpath = model.getAttachmentImageUrl();
                            intent.putExtra("image path", imgpath);
                            startActivity(intent);
                        }
                    }
                });
                //expand profile photo by same method as above attachment
                holder.messenger.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(model.getPhotoUrl()!=null) {
                            Intent intent = new Intent(getBaseContext(), PictureExpandedActivity.class);
                            String imgpath = model.getPhotoUrl();
                            intent.putExtra("image path", imgpath);
                            startActivity(intent);
                        }
                    }
                });
            }

            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                //oncreateviewholder (messageViewHolder) it expands messages with predefined message layout
                LayoutInflater inflater =LayoutInflater.from(parent.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.layout_message,parent,false));
            }
        };

        messageBox= findViewById(R.id.message);
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()>0){
                    sendMessageButton.setEnabled(true);
                }else {
                    sendMessageButton.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sendphoto= findViewById(R.id.sendphoto);
        sendphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open image selection window and only allow images
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        sendMessageButton = findViewById(R.id.sendbutton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message(messageBox.getText().toString(),
                        Username,null, profilePhotoUrl);
                reference.push().setValue(message);
                messageBox.setText("");
            }
        });
        recyclerView.setAdapter(mFirebaseAdapter);

    }
    //end onCreate

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
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(GoogleApiClient);
                FirebaseUser =null;
                Username =null;
                profilePhotoUrl =null;
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                return true;
            default:return super.onOptionsItemSelected(item);
        }
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
                    Message tempMessage = new Message(null, Username, profilePhotoUrl, LOADING_IMAGE_URL);
                    reference.child("messages").push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {

                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(FirebaseUser.getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.", databaseError.toException());
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
                                    new Message(null, Username,task.getResult().getDownloadUrl()
                                            .toString(), profilePhotoUrl);
                            reference1.child("messages").child(key)
                                    .setValue(Message);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.", task.getException());
                            Toast.makeText(getApplicationContext(),"image upload fialed :(",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(GoogleApiClient.isConnected())
            GoogleApiClient.disconnect();
        mFirebaseAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleApiClient.connect();
        mFirebaseAdapter.startListening();
    }

    //custom item in recycler view contains widgets to be put in item as per layout in oncreateviewholder
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView titleTextView;
        ImageView attachment;
        ImageView messenger;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            titleTextView = itemView.findViewById(R.id.senderTextview);
            attachment = itemView.findViewById(R.id.messageattachmentview);
            messenger= itemView.findViewById(R.id.circularimageview);
        }
    }

}
