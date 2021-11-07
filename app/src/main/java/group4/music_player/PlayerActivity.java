package group4.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

import group4.music_player.dao.NoteDAO;
import group4.music_player.model.Note;

public class PlayerActivity extends AppCompatActivity implements TimerDialog.NoticeDialogListener {
    final static int HOUR = 0;
    final static int MIN = 1;
    final static int SEC = 2;
    private static final String CHANNEL_ID = "12";
    private NoteDAO noteDao;
    Button btnplay, btnnext, btnprev, btnff, btnfr,btnCancelTime;
    TextView txtsname, txtsstart, txtsstop,txtTimeView,txtTimeNum;
    SeekBar seekmusic;
    BarVisualizer visualizer;
    String sname;
    String uriString;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    ImageView imageView ;
    Thread updateseekbar ;
    Note playedNote;
    CountDownTimer mCountDownTimer;
    long seconds;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ((item.getItemId()== android.R.id.home)){
            onBackPressed();
        }else if(item.getItemId() == R.id.btnNote){
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("uri",uriString);
            startActivity(intent);
        }else if(item.getItemId() == R.id.btnLike){
            String sql = "UPDATE Note SET isLike = '1' WHERE uri='"+uriString+"'";
            noteDao.QueryData(sql);
            Toast.makeText(this, "Added To Favorite List", Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }else if(item.getItemId() == R.id.btnUnlike){
            String sql = "UPDATE Note SET isLike = '0' WHERE uri='"+uriString+"'";
            noteDao.QueryData(sql);
            Toast.makeText(this, "Removed To Favorite List", Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }else if(item.getItemId() == R.id.btnTimer){
            if(seconds==0){
                DialogFragment newFragment = new TimerDialog(this);
                newFragment.show(getSupportFragmentManager(), "missiles");
            }else{
                Toast.makeText(this, "Your timer is running, cancel for new", Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer!= null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        createNotificationChannel();
        seconds = 0;
        noteDao = new NoteDAO(this, "MusicPlayer.sqlite", null, 1);
        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        txtsname = findViewById(R.id.txtsn);
//        txtsname = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        txtsstart = findViewById(R.id.txtsstart) ;

        txtTimeView = findViewById(R.id.txtTimeView);
        txtTimeNum = findViewById(R.id.txtTimeNum);
        btnCancelTime = findViewById(R.id.btnStopTime);
        btnCancelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seconds = 0;
                mCountDownTimer.cancel();
                hideView();
            }
        });

        seekmusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imageview);
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        uriString = uri.toString();

        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateseekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currenposition = 0 ;

                while(currenposition< totalDuration){
                    try {
                        sleep(200);
                        currenposition = mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currenposition);
                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary),
                PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary)
                , PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000 ;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mediaPlayer.isPlaying()) {
                    btnplay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        // next listener
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

//                txtsstart.setText("0:00");
//                String endTime = createTime(mediaPlayer.getDuration());
//                txtsstop.setText(endTime);
//                txtsstop.setText(mediaPlayer.getDuration());

//                seekmusic.setProgress(mediaPlayer.getDuration());
                seekmusic.setMax(0);

                btnnext.performClick();
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if(audiosessionId!= -1){
            visualizer.setAudioSessionId(audiosessionId);
        }

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position= ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                uriString = u.toString();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);


                txtsstart.setText("0:00");
                txtsstop.setText(createTime(mediaPlayer.getDuration()));
                mediaPlayer.start();
                seekmusic.setMax(mediaPlayer.getDuration());
                seekmusic.refreshDrawableState();

                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                StartAnimation(imageView);

                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId!= -1){
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
//                position =((position-1)<0)?(mySongs.size()-1):(position-1);
                System.out.println("POSITION BEFORE:"+position);
                position =((position-1)<0)?(mySongs.size()-1):(position-1);
                System.out.println("POSITION AFTER:"+position);
                System.out.println("SIZE:"+mySongs.size());

                Uri u = Uri.parse(mySongs.get(position).toString());
                uriString = u.toString();
                mediaPlayer = mediaPlayer.create(getApplicationContext(), u);
                sname= mySongs.get(position).getName();
                txtsname.setText(sname);
                System.out.println(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                StartAnimation(imageView);

                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId!= -1){
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        playedNote = getNoteBefore(uriString);
        if(playedNote.isLike()){
            getMenuInflater().inflate(R.menu.note_unlike,menu);
        }else{
            getMenuInflater().inflate(R.menu.note,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void StartAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();

    }

    public String createTime (int duration){
        String time  = "";
        int min = duration/1000/60 ;
        int sec = duration/1000%60 ;
        time+= min+":";
        if (sec < 10){
            time+="0";
        }
        time+= sec ;
        return time ;
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
    @Override
    public void onDialogPositiveClick(TimerDialog dialog) {

        int hour = dialog.hourPicker.getValue();
        int minutes = dialog.minPicker.getValue();
        seconds = Long.parseLong(String.valueOf(minutes))*60000;
        seconds+=Long.parseLong(String.valueOf(hour*60))*60000;

        if(seconds == 0){
            Toast.makeText(this, "0 hour and 0 min so nothing happend!!", Toast.LENGTH_SHORT).show();
        }
        startTime();
    }
    @Override
    public void onDialogNegativeClick(TimerDialog dialog) {

    }

    public void startTime(){
//        seconds = 10000; // Test value = 5s;

        showView();
        mCountDownTimer = new CountDownTimer(seconds,1000) {
            @Override
            public void onTick(long l) {
                seconds = l;
                String[] time = getMinutes(seconds);
                String hour = time[HOUR];
                String min = time[MIN];
                String sec = time[SEC];
                if(Integer.parseInt(hour) < 10){
                    hour = "0"+hour;
                }
                if(Integer.parseInt(min)<10){
                    min = "0"+min;
                }
                if(Integer.parseInt(sec)<10){
                    sec = "0"+sec;
                }
                txtTimeNum.setText(hour+":"+min+":"+sec);
            }

            @Override
            public void onFinish() {
                createNotification();
                if(mediaPlayer.isPlaying()){
                   btnplay.performClick();
                }
                seconds = 0;
                hideView();
            }
        }.start();

    }
    public void stopTime(){

    }
    public void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Audio Timer")
                .setContentText("Your time was end!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
// notificationId is a unique int for each notification that you must define
        notificationManager.notify(23, builder.build());
    }

    // Create Chanel for Notification
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void hideView(){
        txtTimeView.setVisibility(View.INVISIBLE);
        txtTimeNum.setVisibility(View.INVISIBLE);
        btnCancelTime.setVisibility(View.INVISIBLE);
    }
    public void showView(){
        txtTimeView.setVisibility(View.VISIBLE);
        txtTimeNum.setVisibility(View.VISIBLE);
        btnCancelTime.setVisibility(View.VISIBLE);
    }
    public String[] getMinutes(long sec){
        long realSec = sec/1000;
        String [] result = new String[3];
        String hour = String.valueOf(realSec/3600);
        String min = String.valueOf((realSec%3600)/60);
        String secs  = String.valueOf(realSec%60);
        result[HOUR] = hour;
        result[MIN] = min;
        result[SEC] = secs;
        return result;
    }
}