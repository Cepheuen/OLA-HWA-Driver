package com.cepheuen.olahwadriver.app;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.cepheuen.olahwadriver.app.api.OlaAPI;
import com.cepheuen.olahwadriver.app.api.OlaExtAPI;
import com.cepheuen.olahwadriver.app.models.*;

import java.io.IOException;


public class HomeActivity extends AppCompatActivity {

    private Integer CRN;
    int position = 0;
    MediaPlayer mp = new MediaPlayer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        Button locateButton = (Button) findViewById(R.id.locateButton);
        Button startButton = (Button) findViewById(R.id.startTrip);
        Button endButton = (Button) findViewById(R.id.endTrip);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new SearchPassengers().execute();
            }
        });
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LocatePassenger().execute();
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StartTrip().execute();
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EndTrip().execute();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void playMusic(final SongModel[] songs){
        try {

            mp.reset();
            mp.setDataSource(songs[position].url);
            mp.prepare();
            mp.start();

            // Setup listener so next song starts automatically
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer arg0) {
                    position ++;
                    playMusic(songs);
                }

            });

        } catch (IOException e) {
            Log.v(getString(R.string.app_name), e.getMessage());
        }
    }
    private class SearchPassengers extends AsyncTask<Void, Void, SearchModel> {
        @Override
        protected SearchModel doInBackground(Void... voids) {
            return OlaAPI.getPublicApiService().searchFares();
        }

        @Override
        protected void onPostExecute(SearchModel model) {
            if (model != null) {
                CRN = model.getCrn();
            } else {
                Log.e("OLAHWA","No Model" );
            }
        }
    }

    private class LocatePassenger extends AsyncTask<Void, Void, ClientLocationModel> {
        @Override
        protected ClientLocationModel doInBackground(Void... voids) {
            return OlaAPI.getPublicApiService().locateClient(CRN);
        }

        @Override
        protected void onPostExecute(ClientLocationModel model) {
            if (model != null) {

            } else {
                Log.e("OLAHWA","No Model" );
            }
        }
    }

    private class EndTrip extends AsyncTask<Void, Void, EndTripModel> {
        @Override
        protected EndTripModel doInBackground(Void... voids) {
            mp.stop();
            return OlaAPI.getPublicApiService().endRide(CRN);
        }

        @Override
        protected void onPostExecute(EndTripModel model) {
            if (model != null) {

            } else {
                Log.e("OLAHWA", "No Model");
            }
        }
    }

    private class StartTrip extends AsyncTask<Void, Void, StartTripModel> {
        @Override
        protected StartTripModel doInBackground(Void... voids) {
            return OlaAPI.getPublicApiService().startRide(CRN);

        }

        @Override
        protected void onPostExecute(StartTripModel model) {
            if (model != null) {
                new PlayMusic().execute();
            } else {
                Log.e("OLAHWA","No Model" );
            }
        }
    }

    private class PlayMusic extends AsyncTask<Void,Void,PlaylistModel[]> {

        @Override
        protected PlaylistModel[] doInBackground(Void... voids) {

            return OlaExtAPI.getPublicApiService().fetchPlaylist(CRN);

        }

        @Override
        protected void onPostExecute(PlaylistModel[] model) {
            Log.d("Fucker",model[0].crn+"");
            SongModel[] songModelList = model[0].songs;
            playMusic(songModelList);
        }
    }
}
