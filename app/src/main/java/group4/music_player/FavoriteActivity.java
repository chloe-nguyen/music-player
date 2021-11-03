package group4.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import group4.music_player.dao.NoteDAO;
import group4.music_player.model.Note;

public class FavoriteActivity extends AppCompatActivity {
    private NoteDAO noteDao;
    ListView listView;
    String[] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        noteDao = new NoteDAO(this, "MusicPlayer.sqlite", null, 1);
        listView = findViewById(R.id.listViewSong);
        displaySongs();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ((item.getItemId()== android.R.id.home)){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<File> findSong (File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if(files!=null){
            for (File singleFile: files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findSong(singleFile));
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        Uri u = Uri.parse(singleFile.toString());
                        //check if file not exist in db
                        String sql = "";
                        Note likedAudio = getNoteBefore(u.toString());
                        if(likedAudio==null){
                            sql = "INSERT INTO Note VALUES ('" + u.toString() + "','" + "" + "','0')";
                            noteDao.QueryData(sql);
                        }else{

                            if(likedAudio.isLike()){
                                arrayList.add(singleFile);
                            }
                        }

                    }
                }
            }
        }


        return arrayList;
    }

    void displaySongs() {
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());

        items = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }

//        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
//        listView.setAdapter(myAdapter);

        FavoriteActivity.CustomAdapter customAdapter = new FavoriteActivity.CustomAdapter();
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) listView.getItemAtPosition(i);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)

                        .putExtra("songs", mySongs)
                        .putExtra("songname", songName)
                        .putExtra("pos", i));
            }
        });
    }
    public Note getNoteBefore(String uri) {
        String content = null;
        String uriString = null;
        boolean like = false;
        String sql = "SELECT * FROM Note WHERE uri = '" + uri + "'";
        Cursor dataNote = noteDao.GetData(sql);
        Note result = null;
        if(dataNote.moveToNext()){
            uriString = dataNote.getString(0);
            content = dataNote.getString(1);

            // check isLike with convert data INT to String
            if(String.valueOf(dataNote.getInt(2)).equals("1")){
                like = true;
            }else{
                like = false;
            }


            result = new Note(uriString,content,like);
        }
        return result;
    }
    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
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