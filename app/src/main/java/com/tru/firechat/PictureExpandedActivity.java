package com.tru.firechat;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class PictureExpandedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_expanded);
        String path=getIntent().getStringExtra("image path");
        ImageView picture = (ImageView) findViewById(R.id.ExpandedPictureView);
        Glide.with(this).load(path).into(picture);
        setTitle("Expanded View");
    }
}
