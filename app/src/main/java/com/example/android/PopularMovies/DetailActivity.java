package com.example.android.PopularMovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.PopularMovies.databinding.ActivityDetailBinding;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.Reviews;
import com.example.android.PopularMovies.model.Videos;
import com.example.android.PopularMovies.utilities.DbUtils;
import com.example.android.PopularMovies.utilities.NetworkUtils;
import com.example.android.PopularMovies.utilities.UiUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class DetailActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Object>,
        VideosAdapter.OnClickHandler {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private static final int LOADER_ID_VIDEOS = 33;
    private static final int LOADER_ID_REVIEWS = 44;
    private static final String LIFECYCLE_CALLBACKS_VIDEOS_KEY = "callbacks-videos-key";
    private static final String LIFECYCLE_CALLBACKS_REVIEWS_KEY = "callbacks-reviews-key";
    private static final String LIFECYCLE_FAVORITES_UPDATES_KEY = "favorites-updates-key";

    private Movie mMovie;

    private ActivityDetailBinding mActivityDetailBinding;

    private VideosAdapter mVideosAdapter;
    private ReviewsAdapter mReviewsAdapter;

    private boolean isFavoritesUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // if user has marked/unmarked a movie as favorite then we should reload data in MainActivity
        if (savedInstanceState == null || !savedInstanceState.containsKey(LIFECYCLE_FAVORITES_UPDATES_KEY)) {
            isFavoritesUpdated = false;
        } else {
            isFavoritesUpdated = savedInstanceState.getBoolean(LIFECYCLE_FAVORITES_UPDATES_KEY);
        }
        setResults();

        mActivityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent intentThatStartedThisActivity = getIntent();
        mMovie = intentThatStartedThisActivity.getParcelableExtra(MainActivity.INTENT_EXTRA_MOVIE);

        mActivityDetailBinding.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean markedAsFavorite = !mMovie.isMarkedAsFavorite();
                DbUtils.updateFavorites(getBaseContext(), markedAsFavorite, mMovie);
                UiUtils.showToastForFavoriteButton(getBaseContext(), markedAsFavorite, mMovie.getTitle());
                mActivityDetailBinding.favorite.setImageResource(UiUtils.getImageResourceForFavoriteButton(markedAsFavorite));
                mMovie.setMarkedAsFavorite(markedAsFavorite);
                isFavoritesUpdated = !isFavoritesUpdated;
                setResults();
            }
        });

        populateUI();

        initLayoutManagerAndAdapter(LOADER_ID_VIDEOS);
        if (savedInstanceState == null || !(savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_VIDEOS_KEY))) {
            getSupportLoaderManager().restartLoader(LOADER_ID_VIDEOS, null, this).forceLoad();
        } else {
            Videos videos = savedInstanceState.getParcelable(LIFECYCLE_CALLBACKS_VIDEOS_KEY);
            mVideosAdapter.setData(videos);
        }

        initLayoutManagerAndAdapter(LOADER_ID_REVIEWS);
        if (savedInstanceState == null || !(savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_REVIEWS_KEY))) {
            getSupportLoaderManager().restartLoader(LOADER_ID_REVIEWS, null, this).forceLoad();
        } else {
            Reviews reviews = savedInstanceState.getParcelable(LIFECYCLE_CALLBACKS_REVIEWS_KEY);
            mReviewsAdapter.setData(reviews);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIFECYCLE_CALLBACKS_VIDEOS_KEY, mVideosAdapter.getData());
        outState.putParcelable(LIFECYCLE_CALLBACKS_REVIEWS_KEY, mReviewsAdapter.getData());
        outState.putBoolean(LIFECYCLE_FAVORITES_UPDATES_KEY, isFavoritesUpdated);
    }

    @Override
    public void onBackPressed() {
        if (isFavoritesUpdated) {
            setResult(getResources().getInteger(R.integer.favorites_updated));
        } else {
            setResult(getResources().getInteger(R.integer.favorites_not_updated));
        }
        Log.d(LOG_TAG, "isFavoritesUpdated: " + isFavoritesUpdated);
        finish();
    }

    private void populateUI() {
        String year = mMovie.getReleaseDate().substring(0,4);

        mActivityDetailBinding.detailF.originalTitleTv.setText(String.format(getString(R.string.format_original_name_in_detail_view), mMovie.getOriginalTitle(), year));

        setTitle(mMovie.getTitle());


        mActivityDetailBinding.detailF.ratingTv.setText(String.format(getString(R.string.format_ratings), mMovie.getVoteAverage()));
        mActivityDetailBinding.detailF.ratingRb.setRating((float) mMovie.getVoteAverage() / 2);
        mActivityDetailBinding.overviewF.overviewTv.setText(mMovie.getOverview());
        Picasso.with(this)
                .load(NetworkUtils.getPosterUrl(mMovie.getPosterPath(), this))
                .placeholder(R.drawable.placeholder_image)
                .into(mActivityDetailBinding.moviePosterIv);
        String bg_link;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            bg_link = NetworkUtils.getPosterUrl(mMovie.getPosterPath(), this);
        } else {
            bg_link = NetworkUtils.getPosterUrl(mMovie.getBackdropPath(), this);
        }
        Picasso.with(this)
                .load(bg_link)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable bg = new BitmapDrawable(getResources(), bitmap);
                        bg.setAlpha(getBaseContext().getResources().getInteger(R.integer.detail_activity_background_alpha));
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
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

        mActivityDetailBinding.favorite.setImageResource(UiUtils.getImageResourceForFavoriteButton(mMovie.isMarkedAsFavorite()));
    }

    private void initLayoutManagerAndAdapter(int loaderId) {
        switch (loaderId) {
            case LOADER_ID_VIDEOS:
                LinearLayoutManager videosLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                mActivityDetailBinding.overviewF.videosListRv.setLayoutManager(videosLayoutManager);

                mVideosAdapter = new VideosAdapter(this, this);
                mActivityDetailBinding.overviewF.videosListRv.setAdapter(mVideosAdapter);

                break;
            case LOADER_ID_REVIEWS:
                LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mActivityDetailBinding.overviewF.reviewsListRv.setLayoutManager(reviewsLayoutManager);

                mReviewsAdapter = new ReviewsAdapter(this);
                mActivityDetailBinding.overviewF.reviewsListRv.setAdapter(mReviewsAdapter);

                break;
        }
    }

    @NonNull
    @Override
    public Loader<Object> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case LOADER_ID_VIDEOS:
                return new AsyncTaskLoader<Object>(this) {
                    @Override
                    public Videos loadInBackground() {
                        if (NetworkUtils.isOnline(getContext())) {
                            return NetworkUtils.readVideosFromApi(mMovie.getId());
                        } else {
                            return null;
                        }
                    }
                };
            case LOADER_ID_REVIEWS:
                return new AsyncTaskLoader<Object>(this) {
                    @Nullable
                    @Override
                    public Object loadInBackground() {
                        if (NetworkUtils.isOnline(getContext())) {
                            return NetworkUtils.readReviewsFromApi(mMovie.getId());
                        } else {
                            return null;
                        }
                    }
                };
            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {
        switch (loader.getId()) {
            case LOADER_ID_VIDEOS:
                if (data == null) {
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                    mVideosAdapter.setData(null);

                } else {
                    mVideosAdapter.setData((Videos) data);
                }
                break;
            case LOADER_ID_REVIEWS:
                if (data == null) {
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                    mReviewsAdapter.setData(null);

                } else {
                    mReviewsAdapter.setData((Reviews) data);
                }
                break;
            default:
                throw new RuntimeException("Loader not implemented: " + loader.getId());

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Object> loader) {
        switch (loader.getId()) {
            case LOADER_ID_VIDEOS:
                mVideosAdapter.setData(null);
                break;
            case LOADER_ID_REVIEWS:
                mReviewsAdapter.setData(null);
                break;
            default:
                throw new RuntimeException("Loader not implemented: " + loader.getId());

        }
    }

    @Override
    public void onClick(String videoLink) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(videoLink));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void setResults() {
        if (isFavoritesUpdated) {
            setResult(getResources().getInteger(R.integer.favorites_updated));
        } else {
            setResult(getResources().getInteger(R.integer.favorites_not_updated));
        }
    }
}
