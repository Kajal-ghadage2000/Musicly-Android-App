package com.example.dell.readsong1;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity  {
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.valdioveliu.valdio.audioplayer.PlayNewAudio";
    private Context mContext;
    private Activity mActivity;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private LinearLayout mRootLayout;

    ArrayAdapter<String> adapter;
    ArrayList<String> myarray;
    ArrayList<String> data;

    ImageView ivplay,nextsong,prevsong;
    ListView listView;
    TextView textView,textView2,start,end;
    Button b1,b2;
    SeekBar seekBar;
    SearchView mSearchView;
    MenuItem mSearch;

    int curentsong=0;
    String filename,queryArtist;
    boolean repeat=false;
    int count = 0;
    public static int oneTimeOnly = 0;
    Cursor cursor;
    public static MediaPlayer mediaPlayer;
    private boolean shuffle=true;
    private Random rand;
    int total;

    //SearchView searchView;
    private double startTime = 0;
    private static double finalTime = 0;

    private Handler myHandler = new Handler();;
    private static final int MY_PERMISSION_REQUEST_CODE = 123;

    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    MainActivity()
    {
        mediaPlayer=new MediaPlayer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        
        callStateListener();

        ivplay=(ImageView)findViewById(R.id.imageView2);
        nextsong=(ImageView)findViewById(R.id.imageView3);
        prevsong=(ImageView)findViewById(R.id.imageView4);
        seekBar=(SeekBar)findViewById(R.id.seekBar3);
        seekBar.setClickable(true);

        textView=(TextView)findViewById(R.id.textView);
        textView2=(TextView)findViewById(R.id.textView2);
        start=(TextView)findViewById(R.id.textView3);
        end=(TextView)findViewById(R.id.textView4);
        textView.setSelected(true);

        mediaPlayer=new MediaPlayer();

        myarray = new ArrayList<String>();
        data = new ArrayList<String>();

        adapter= new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item, myarray);

        listView.setTextFilterEnabled(true);
        listView.setAdapter(adapter);

        // Get the application context
        mContext = getApplicationContext();
        mActivity = MainActivity.this;

        // Get the widget reference from xml layout
        mRootLayout = findViewById(R.id.root_layout);

        // Custom method to check permission at run time
        checkPermission();
        getMediaFileList();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            boolean userTouch;
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                userTouch = false;
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                userTouch = true;
            }
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if(mediaPlayer.isPlaying() && arg2)
                    mediaPlayer.seekTo(arg1);
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp+forwardTime)<=finalTime){
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(),"You have Jumped forward 5 seconds",Toast.LENGTH_SHORT).show();
                    start.setText(String.format("%d : %d",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                            startTime)))
                    );

                }else{
                    Toast.makeText(getApplicationContext(),"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //backword
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp-backwardTime)>0){
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(),"You have Jumped backward 5 seconds",Toast.LENGTH_SHORT).show();
                    start.setText(String.format("%d : %d",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                            startTime)))
                    );


                }else{
                    Toast.makeText(getApplicationContext(),"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        nextsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curentsong!=total-1) {
                    ivplay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                    curentsong = curentsong + 1;
                    mediaPlayer.reset();
                    call(curentsong);
                }
            }
        });

        prevsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curentsong!=0) {
                    ivplay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                    curentsong = curentsong - 1;
                    mediaPlayer.reset();
                    call(curentsong);
                }
            }
        });


    }


    protected void onStart() {
        super.onStart();
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            start.setText(String.format("%d : %d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            //startTime=startTime/1000;
            seekBar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);

        }
    };

    // Custom method to get all audio files list from external storage
    protected void getMediaFileList(){

        ContentResolver contentResolver = mContext.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        cursor = contentResolver.query(
                uri, // Uri
                projection, // Projection
                selection, // Selection
                null, // Selection args
                null // Sor order
        );
        count = cursor.getCount();
        if (cursor == null) {

            Toast.makeText(getApplicationContext(),"\n" +"Query failed, handle error.",Toast.LENGTH_LONG);
        } else if (!cursor.moveToFirst()) {

            Toast.makeText(getApplicationContext(),"\n" +"Nno music found on the sd card.",Toast.LENGTH_LONG);

        } else {
            int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            // Loop through the musics
            do {

                String data1 = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                data.add(data1);

                String thisTitle = cursor.getString(title);

                myarray.add(thisTitle);

            } while (cursor.moveToNext());

            total=myarray.size();
            adapter= new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item, myarray);

            listView = (ListView) findViewById(R.id.list);

            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                    {

                        @Override
                        public void onCompletion(MediaPlayer mp)
                        {

                   if(shuffle)
                    {
                        visibilty(position);
                        ivplay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                        rand = new Random();
                        int currentSongIndex = rand.nextInt((myarray.size() - 1) - 0 + 1) + 0;
                        curentsong=currentSongIndex;
                        mediaPlayer.reset();
                        call(currentSongIndex);
                    }
                    else
                   {
                       visibilty(position);
                       ivplay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                       mediaPlayer.reset();
                       call(curentsong);
                   }
                        }

                    });
                    visibilty(position);
                    ivplay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                    curentsong=position;
                    call(position);

                    mSearchView.clearFocus();
                    mSearchView.setIconified(true);
                    mSearchView.setIconified(true);

                }
            });
        }
    }


    private void visibilty(int position) {

        prevsong.setVisibility(View.VISIBLE);
        nextsong.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        start.setVisibility(View.VISIBLE);
        end.setVisibility(View.VISIBLE);
        ivplay.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);

    }

    private void call(int position) {
        System.gc();
       // Toast.makeText(this,position,Toast.LENGTH_LONG).show();
        String name = listView.getItemAtPosition(position).toString();
        textView.setText(name);

        int music_column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        position=findpos(name);
        curentsong=position;
        cursor.moveToPosition(position);
        filename = cursor.getString(music_column_index);

        //blink
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(400); //You can manage the time of the blink with this parameter
        anim.setStartOffset(10);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        start.startAnimation(anim);


        //underline
        SpannableString content = new SpannableString(name);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);

        try
        {
            mediaPlayer.reset();

            queryArtist = cursor.getString(1);
            textView2.setText(queryArtist);

            Intent i=new Intent(getApplicationContext(),AlarmService.class);
            startService(i);
            //new AlarmService().sendNotification(queryArtist);

            mediaPlayer.setDataSource(filename);
            mediaPlayer.prepare();
            mediaPlayer.start();
            //mediaPlayer.setLooping(true);


            finalTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();

            if (oneTimeOnly == 0) {
                //seekBar.setMax(mediaPlayer.getDuration());
                seekBar.setMax((int) finalTime);
                oneTimeOnly = 1;
            }

            seekBar.setProgress((int) startTime);
            myHandler.postDelayed(UpdateSongTime, 100);

            end.setText(String.format("%d : %d",
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime)))
            );

            start.setText(String.format("%d : %d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    startTime)))
            );

            //notification
            //NotificationGenerator.openActivityNotification(getApplicationContext());
            //NotificationGenerator.customBigNotification(getApplicationContext());

        }
        catch (Exception e) {}

    }

    private int findpos(String name) {
        int i;
        for(i=0;i<total;i++)
        {
            if(name== myarray.get(i))
                break;
        }
        return  i;
    }

    protected void checkPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                    // Show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage("Read external storage permission is required.");
                    builder.setTitle("Please grant permission");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    mActivity,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSION_REQUEST_CODE
                            );
                        }
                    });
                    builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            mActivity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSION_REQUEST_CODE
                    );
                }
            }else {
                // Permission already granted
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case MY_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission granted
                }else {
                    // Permission denied
                }
            }
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        mSearch = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) mSearch.getActionView();
        //mCloseButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        //mCloseButton.setVisibility(View.VISIBLE);
        //for back arrow
        mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text).setBackgroundResource(R.drawable.abc_textfield_search_default_mtrl_alpha);
        mSearchView.setQueryHint("\uD83D\uDD0D Search");
       // mSearchView.setIconified(true);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_search:
            return true;
            case R.id.action_end:
                newGame();
                return true;
            case R.id.action_close:
                  this.finish();
                  Runtime.getRuntime().gc();
                  System.gc();
                  item.setChecked(true);
                  shuffle=false;
                return true;
            case R.id.action_lyrics:
                displayLyrics();
                return true;
            case R.id.action_shuffle:
                item.setChecked(true);
                shuffle=true;
                repeat=false;
                return true;
            case R.id.action_repeat:
                item.setChecked(true);
                repeat=true;
                shuffle=false;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayLyrics() {
        if(isQLInstalled())
        {
            startActivity(new Intent("com.geecko.QuickLyric.getLyrics").putExtra("TAGS", new String[]{queryArtist, filename}));
        }
        else
        {
            Toast.makeText(this,"Please install QuickLyric App from playstore",Toast.LENGTH_LONG).show();

        }
    }

    public boolean isQLInstalled() {

        PackageManager pm = getApplicationContext().getPackageManager();
        try {
            pm.getPackageInfo("com.geecko.QuickLyric", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private void newGame() {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        final SeekBar seekBar1 = new SeekBar(this);


        popDialog.setView(seekBar1);

        AlertDialog alertDialog=popDialog.create();

        alertDialog.setIcon(R.drawable.volume);
        alertDialog.setTitle("Music Volume");
        alertDialog.show();

        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        seekBar1.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekBar1.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG);
                switch (state) {

                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();//pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            mediaPlayer.pause();//pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                mediaPlayer.start();
                                // resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        if (getIntent().getBooleanExtra("EXIT", false))
        {
            finish();
        }
        Runtime.getRuntime().gc();
    }

    public void playpause(View view) {
        if(mediaPlayer.isPlaying())
        {
            ivplay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            mediaPlayer.pause();
        }
        else {
            ivplay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            mediaPlayer.start();
        }
    }
}
