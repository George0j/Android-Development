package com.android.music;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] items;
    private static final int PERMISSION_REQUEST_CODE = 101; // Request code for permissions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listview);

        if (checkPermission()) {
            displaySongs();
        } else {
            requestPermission();
        }
    }


    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displaySongs();
            } else {
                Toast.makeText(this, "Permission Denied! Cannot access audio files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ArrayList<File> findSongs(Context context) {
        ArrayList<File> songList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.DATA};

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(0);
                File songFile = new File(filePath);
                songList.add(songFile);
            }
            cursor.close();
        }

        return songList;
    }

    public void displaySongs() {
        final ArrayList<File> mySongs = findSongs(this);

        if (mySongs.isEmpty()) {
            Toast.makeText(this, "No songs found!", Toast.LENGTH_SHORT).show();
            return;
        }

        items = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            items[i] = mySongs.get(i).getName().replace(".mp3", "").replace(".wav", "");
        }

        CustomerAdapter adapter = new CustomerAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName=(String) listView.getItemAtPosition(position);

                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs",mySongs)
                        .putExtra("songname",songName)
                        .putExtra("pos",position)
                );
                
            }
        });
    }

    class CustomerAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView txtSong = view.findViewById(R.id.txtSong);
            txtSong.setSelected(true);
            txtSong.setText(items[position]);
            return view;
        }
    }
}