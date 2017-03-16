package com.home.amngomes.controller.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.home.amngomes.controller.Constants;
import com.home.amngomes.controller.RetrofitClient;
import com.home.amngomes.controller.Utils;
import com.home.amngomes.models.Song;
import com.home.amngomes.ultrastarsongviewer.R;
import com.home.amngomes.views.SongActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private ArrayList<Song> songs = new ArrayList<>();
    private static Context context;

    public String getNextRange() {
        return "items=" + songs.size() + "-" + (songs.size()+20);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SongAdapter(Context mContext) {
        SongAdapter.context = mContext;
        songs = new ArrayList<>();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void clear(){
        songs.clear();
    }


    @Override
    public void onBindViewHolder(final SongViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Picasso.with(context)
                .load(RetrofitClient.getInstance().getImagePath(songs.get(position).id))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.target);

        holder.song.setText(songs.get(position).song);
        holder.artist.setText(songs.get(position).artist);
        holder.setPosition(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SongAdapter.SongViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_song, parent, false);
        // set the view's size, margins, paddings and layout parameters

        SongViewHolder vh = new SongViewHolder(v);
        v.setTag(vh);
        return vh;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return songs.size();
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        @BindView(R.id.song_image)
        public ImageView songImage;

        @BindView(R.id.song_artist)
        public TextView artist;

        @BindView(R.id.song_name)
        public TextView song;

        @BindView(R.id.card_background)
        LinearLayout card_background;

        private int cardColor;
        private int artistTextColor;
        private int songTextColor;
        private int position = 0;

        public SongViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            ButterKnife.bind(this, view);
        }

        private Palette palette;

        private void initColors(Palette palette) {
            Palette.Swatch vibrant = null;
            if (palette != null) {
                if (palette.getDarkVibrantSwatch() != null) {
                    vibrant = palette.getDarkVibrantSwatch();
                } else if (palette.getVibrantSwatch() != null) {
                    vibrant = palette.getVibrantSwatch();
                } else if (palette.getLightVibrantSwatch() != null) {
                    vibrant = palette.getLightVibrantSwatch();
                }
                if (vibrant != null) {
                    cardColor = vibrant.getRgb();
                    artistTextColor = vibrant.getTitleTextColor();
                    songTextColor = vibrant.getBodyTextColor();
                }
            }
        }

        public Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                setColors(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Bitmap bitmap = Utils.drawableToBitmap(errorDrawable);
                setColors(bitmap);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Bitmap bitmap = Utils.drawableToBitmap(placeHolderDrawable);
                setColors(bitmap);
            }
        };

        private void setColors(Bitmap bitmap){
            cardColor = context.getResources().getColor(R.color.md_orange_a200);
            artistTextColor = context.getResources().getColor(R.color.md_grey_300);
            songTextColor = context.getResources().getColor(R.color.md_white);
            songImage.setImageBitmap(bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette p) {
                    // Use generated instance
                    initColors(p);
                    card_background.setBackgroundColor(cardColor);
                    artist.setTextColor(artistTextColor);
                    song.setTextColor(songTextColor);
                    palette = p;
                }
            });
        }

        public Palette getPalette() {
            return palette;
        }


        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(final View view) {
            Intent intent = new Intent(context, SongActivity.class);
            Bundle b = new Bundle();
            b.putString(Constants.Bundle.SONG_ARTIST, songs.get(position).artist);
            b.putString(Constants.Bundle.SONG_NAME, songs.get(position).song);
            b.putString(Constants.Bundle.SONG_PATH, RetrofitClient.getInstance()
                    .getSongPath(songs.get(position).id));

            ImageView imageView = (ImageView) view.findViewById(R.id.song_image);
            SongViewHolder holder = (SongViewHolder) view.getTag();
            SongActivity.palette = holder.getPalette();
            SongActivity.songImageBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

            intent.putExtras(b);
            String transitionName = context.getString(R.string.transition_album_cover);

            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                            view.findViewById(R.id.song_image),   // The view which starts the transition
                            transitionName    // The transitionName of the view we're transitioning to
                    );
            ActivityCompat.startActivity((Activity) context, intent, options.toBundle());
        }
    }



}