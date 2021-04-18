package com.geniobits.mediachooserapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.geniobits.mediachooserdialog.MediaChooser.MediaChooserDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View image_select = findViewById(R.id.image_select);
        View video_select = findViewById(R.id.video_select);
        TextView path_txt = findViewById(R.id.path_txt);
        image_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MediaChooserDialog(MainActivity.this, new MediaChooserDialog.MediaSelectionListener() {
                    @Override
                    public void onMediaSelected(Uri uri, String path) {
                        path_txt.setText(path);
                    }
                }).showPictureDialog();
            }
        });

        video_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MediaChooserDialog(MainActivity.this, new MediaChooserDialog.MediaSelectionListener() {
                    @Override
                    public void onMediaSelected(Uri uri, String path) {
                        path_txt.setText(path);
                    }
                }).showVideoDialog();
            }
        });
    }
}