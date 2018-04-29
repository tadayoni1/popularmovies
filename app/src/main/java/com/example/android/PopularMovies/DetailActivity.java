package com.example.android.PopularMovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.PopularMovies.databinding.ActivityDetailBinding;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Calendar;

public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    Movie mMovie;

    ActivityDetailBinding mActivityDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mActivityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent intentThatStartedThisActivity = getIntent();
        mMovie = intentThatStartedThisActivity.getParcelableExtra(MainActivity.INTENT_EXTRA_MOVIE);

        populateUI();
    }

    private void populateUI() {
        mActivityDetailBinding.originalTitleTv.setText(mMovie.getOriginalTitle());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mMovie.getReleaseDate());
        mActivityDetailBinding.releaseDateTv.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        mActivityDetailBinding.ratingTv.setText(String.format(getString(R.string.format_ratings), mMovie.getVoteAverage()));
        mActivityDetailBinding.ratingRb.setRating((float) mMovie.getVoteAverage() / 2);
        mActivityDetailBinding.overviewTv.setText(mMovie.getOverview());
        Picasso.with(this)
                .load(NetworkUtils.getPosterUrl(mMovie.getPosterPath(),this))
                .placeholder(R.drawable.placeholder_image)
                .into(mActivityDetailBinding.moviePosterIv);
        String bg_link;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            bg_link = NetworkUtils.getPosterUrl(mMovie.getPosterPath(),this);
        } else {
            bg_link = NetworkUtils.getPosterUrl(mMovie.getBackdropPath(),this);
        }
        Picasso.with(this)
                .load(bg_link)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable bg = new BitmapDrawable(getResources(), bitmap);
                        bg.setAlpha(getBaseContext().getResources().getInteger(R.integer.detail_activity_background_alpha));
                        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            mActivityDetailBinding.scrollView.setBackground(bg);
                        } else {
                            mActivityDetailBinding.constraintLayout.setBackground(bg);
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }
}
