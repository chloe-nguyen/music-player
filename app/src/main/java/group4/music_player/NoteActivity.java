package group4.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import group4.music_player.dao.NoteDAO;

public class NoteActivity extends AppCompatActivity {
    EditText edtNote;
    NoteDAO noteDAO;
    String uri="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Intent intent = getIntent();
        edtNote = findViewById(R.id.edtNote);
        noteDAO = new NoteDAO(this, "MusicPlayer.sqlite", null, 1);
        uri = intent.getStringExtra("uri");
        String content  = getNoteBefore(uri);
        edtNote.setText(content);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ((item.getItemId()== android.R.id.home)){
            onBackPressed();
        }else if(item.getItemId() == R.id.btnSaveNote){
            String content = edtNote.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Nothing to add", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Saved your note", Toast.LENGTH_SHORT).show();

                String sql = "";
                if (getNoteBefore(uri) == null){
                    sql = "INSERT INTO Note VALUES ('" + uri + "','" + content + "','0')";
                }else{

                    sql = "UPDATE Note SET note = '"+content+"' WHERE uri='"+uri+"'";
                }
                noteDAO.QueryData(sql);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public String getNoteBefore(String uri) {
        String content = null;
        String sql = "SELECT * FROM Note WHERE uri = '" + uri + "'";
        Cursor dataNote = noteDAO.GetData(sql);
        if(dataNote.moveToNext()){
            content = dataNote.getString(1);

        }


        return content;
    }
}