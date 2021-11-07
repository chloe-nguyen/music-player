package group4.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import group4.music_player.dao.NoteDAO;

public class MainActivity extends AppCompatActivity  {

    ListView listView;
    String[] items;
    private NoteDAO noteDao;
    SearchView searchView;
    ArrayList<String> initItems, searchResultItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listViewSong);
        setUpDB();
        runtimePermission();

    }

    private void setUpDB() {
        noteDao = new NoteDAO(this, "MusicPlayer.sqlite", null, 1);
        noteDao.QueryData("CREATE TABLE IF NOT EXISTS Note(\n" +
                "   uri VARCHAR(200) PRIMARY KEY,\n" +
                "   note VARCHAR(200) NOT NULL,\n" +
                "   isLike INT NOT NULL\n" +
                ");");
        // Search
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSongs(query);
                if (searchResultItems.size() == 0) {
                    Toast.makeText(MainActivity.this, "No Match found", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().equals("")) {
                    items = new String[initItems.size()];
                    for (int i = 0; i < initItems.size(); i++) {
                        items[i] = initItems.get(i);
                    }
                }

                return false;
            }
        });
    }

    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.linktofavorite, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(this, FavoriteActivity.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findSong(singleFile));
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        Uri u = Uri.parse(singleFile.toString());
                        //check if file not exist in db
                        String sql = "";
                        if (getNoteBefore(u.toString()) == null) {
                            sql = "INSERT INTO Note VALUES ('" + u.toString() + "','" + "" + "','0')";
                            noteDao.QueryData(sql);
                        }
                        arrayList.add(singleFile);
                    }
                }
            }
        }


        return arrayList;
    }

    void displaySongs() {
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());

        items = new String[mySongs.size()];
        initItems = new ArrayList<>();
        for (int i = 0; i < mySongs.size(); i++) {
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
            initItems.add(items[i]);
        }

//        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
//        listView.setAdapter(myAdapter);

        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) listView.getItemAtPosition(i);
                int pos = getPositionInInitItemsByName(songName);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)

                        .putExtra("songs", mySongs)
                        .putExtra("songname", songName)
                        .putExtra("pos", pos));
            }
        });
    }

    public String getNoteBefore(String uri) {
        String content = null;
        String sql = "SELECT * FROM Note WHERE uri = '" + uri + "'";
        Cursor dataNote = noteDao.GetData(sql);
        if (dataNote.moveToNext()) {
            content = dataNote.getString(1);

        }


        return content;
    }

    int getPositionInInitItemsByName(String input) {
        for (int i = 0; i < initItems.size(); i++) {
            if (initItems.get(i).equals(input))
                return i;
        }
        return 0;
    }

    void searchSongs(String searchValue) {
        if (searchValue.trim().equals("")) {
            items = new String[initItems.size()];
            for (int i = 0; i < initItems.size(); i++) {
                items[i] = initItems.get(i);
            }
            return;
        }

        searchResultItems = new ArrayList<>();
        for (String name : initItems) {
            if (name.toLowerCase().contains(searchValue.toLowerCase())) {
                searchResultItems.add(name);
            }
        }
        if (searchResultItems.size() == 0)
            return;

        items = new String[searchResultItems.size()];
        for (int i = 0; i < searchResultItems.size(); i++) {
            items[i] = searchResultItems.get(i);
        }
    }





    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return items[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textsong = myView.findViewById(R.id.txtsongname);
            textsong.setSelected(true);
            textsong.setText(items[i]);

            return myView;
        }
    }

}