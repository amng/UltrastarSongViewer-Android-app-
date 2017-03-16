package com.home.amngomes.views;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.home.amngomes.controller.RetrofitClient;
import com.home.amngomes.controller.Utils;
import com.home.amngomes.controller.adapters.SongAdapter;
import com.home.amngomes.models.Song;
import com.home.amngomes.ultrastarsongviewer.R;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private SongAdapter adapter;
    private GridLayoutManager layoutManager;

    @BindView(R.id.song_recycler_view)
    SuperRecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // use a linear layout manager
        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new SongAdapter(this);
        recyclerView.setAdapter(adapter);
        new LoadSongs().execute();
        recyclerView.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
                new LoadSongs().execute();
            }
        }, 10);

        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                new LoadSongs().execute();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        int num_cols = 1;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int new_val = (int) ((float)width / (float) Utils.dpToPixel(200, this));
        num_cols = Math.max(num_cols, new_val);
        layoutManager.setSpanCount(num_cols);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadSongs extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {
            Call<List<Song>> songs = RetrofitClient.getInstance().getService()
                    .getSongs(adapter.getNextRange());
            try {
                Response<List<Song>> songsResponse = songs.execute();
                List<Song> songList = songsResponse.body();
                for (Song song : songList) {
                    adapter.addSong(song);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            recyclerView.setRefreshing(false);
            recyclerView.hideMoreProgress();
        }
    }

}
