package com.prashant.Bajao;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {
   Bitmap imag;
    String iconpath;
     ArrayList<Song> songList;
    RecyclerView songView;
    //ArrayList<Song> songs ;
    MediaPlayer mediaPlayer;

   // Boolean showControl=false;

    private boolean paused=false, playbackPaused=false;

//for service
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    //for musiccontroller
    private MusicController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        songView=(RecyclerView)findViewById(R.id.song_list);
        SongListAdapter slAdapter=new SongListAdapter();
        LinearLayoutManager lm=new LinearLayoutManager(this);


        songList=Song.getSongs();
        getSongList();

        //this is used to sort the songs alphabetically.
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());


            }
        });
      //  controller.hide();
       songView.setLayoutManager(lm);
        songView.setAdapter(slAdapter);

//for calling the services.
        setController();

    }

    @Override
    protected void onPause() {
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
// here on click listener works
    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(1000);
    }
//these ar the functions of mediaplayercontrol
    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getDur();
        else return 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null  && musicBound && musicSrv.isPng())
        return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);

    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
        return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        //set the controller up

        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);}

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
//till here mediaplayercontrol


    public class SongViewHolder extends RecyclerView.ViewHolder {
        public SongViewHolder(View itemView) {
            super(itemView);
            rootView=itemView;
            songView=(TextView)itemView.findViewById(R.id.song_title);
            songPath=(TextView)itemView.findViewById(R.id.song_path);
            iv=(ImageView)itemView.findViewById(R.id.iv);
        }
        View rootView;
        ImageView iv;
        TextView songView;
        TextView songPath;
    }

    public class SongListAdapter extends RecyclerView.Adapter<SongViewHolder>{
        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater li = getLayoutInflater();
            View convertView = null;
            convertView = li.inflate(R.layout.songlist, parent, false);
            SongViewHolder songViewHolder=new SongViewHolder(convertView);

            return songViewHolder;
        }

        @Override
        public void onBindViewHolder(final SongViewHolder holder, final int position) {
            holder.songView.setText(songList.get(position).getTitle());
            holder.songPath.setText(songList.get(position).getPath());
            if(songList.get(position).getIconpath()!=null) {
               /* Bitmap bImage = BitmapFactory.decodeFile(songList.get(position).getIconpath());
                holder.iv.setImageBitmap(bImage);
            */
                Glide.
                        with(MainActivity.this).
                        load(songList.get(position).getIconpath()).
                        thumbnail(0.5f).
                        crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.iv);
            }

            holder.rootView.setTag(position);
            holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (holder.songPath.getVisibility() == View.GONE) {
                      holder.songPath.setVisibility(View.VISIBLE);
                  }
                    else
                  {
                      holder.songPath.setVisibility(View.GONE);
                  }
                      return true;
                }
            });
        }

        @Override
        public int getItemCount() {
         return   songList.size();
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }


    public void getSongList() {
        //retrieve song info

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumId = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM_ID);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thispath=musicCursor.getString(pathColumn);
                String thisAlbumArt =musicCursor.getString(albumId);
                Cursor cursor =  musicResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Audio.Albums._ID + "=?",
                        new String[]{thisAlbumArt},
                        null);
                if (cursor.moveToFirst()) {
                    iconpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                  }
                songList.add(new Song(thisId, thisTitle, thisArtist,thispath,iconpath));
            }
            while (musicCursor.moveToNext());
            musicCursor.close();

        }
    }
}
