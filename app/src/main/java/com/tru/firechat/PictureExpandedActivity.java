package com.tru.firechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class PictureExpandedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_expanded);
        final String path=getIntent().getStringExtra("image path");
        ImageView picture = findViewById(R.id.ExpandedPictureView);
        Glide.with(this).load(path).into(picture);
        setTitle("");
        picture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(path));
                startActivity(i);
                return true;
            }
        });
    }

}
