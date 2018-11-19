package com.example.android.PopularMovies;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.PopularMovies.databinding.ActivityMainBinding;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.PopularResults;
import com.example.android.PopularMovies.utilities.DbUtils;
import com.example.android.PopularMovies.utilities.MiscUtils;
import com.example.android.PopularMovies.utilities.NetworkUtils;
import com.example.android.PopularMovies.utilities.UiUtils;

public class MainActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<PopularResults>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final static int LOADER_ID_POPULAR_RESULTS = 44;
    private final static int LOADER_ID_POPULAR_RESULTS_FAVORITES = 55;

    private final static String LOADER_EXTRA_PAGE_NUMBER = "loader-extra-page-number";
    private final static String LOADER_EXTRA_SORT_ORDER = "loader-extra-sort-order";

    private final static String LIFECYCLE_CALLBACKS_POPULAR_RESULTS_KEY = "callbacks-popular-results-key";

    public static final String INTENT_EXTRA_MOVIE = "intent-extra-movie";
    public static final String INTENT_EXTRA_ADAPTER_POSITION = "intent-extra-adapter-position";

    public static final String INTENT_EXTRA_IS_FAVORITES_SELECTED = "intent-extra-is-favorites-selected";

    private ActivityMainBinding mActivityMainBinding;

    private MoviesAdapter mMoviesAdapter;

    private int mSpanCount;
    private String mSortOrder;
    private String mLastSortOrder;

    private boolean mReloadFromApi;
    private int mAdapterLastPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MiscUtils.LOLLIPOP_AND_HIGHER) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Explode explode = new Explode();
            explode.setDuration(1000);
            getWindow().setExitTransition(explode);
        }
        setContentView(R.layout.activity_main);


        mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // register the preference change listener
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        readSharedPreferences();

        setLayoutManager(mSpanCount);

        mMoviesAdapter = new MoviesAdapter(this, this);
        mActivityMainBinding.moviesList.setAdapter(mMoviesAdapter);

        mMoviesAdapter.setOnLastItemReachedListener(new MoviesAdapter.OnLastItemReachedListener() {
            @Override
            public void onLastItemReached(int last_page) {
                readSharedPreferences();
                if (!mSortOrder.equals(getString(R.string.pref_sort_favorites))) {
                    mReloadFromApi = false;
                    String page_number = String.valueOf(++last_page);
                    restartLoader(LOADER_ID_POPULAR_RESULTS, page_number);
                }
            }
        });

        if (savedInstanceState == null || !(savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_POPULAR_RESULTS_KEY))) {
            readSharedPreferences();
            if (mSortOrder.equals(getString(R.string.pref_sort_favorites))) {
                restartLoader(LOADER_ID_POPULAR_RESULTS_FAVORITES, null);
            } else {
                Log.d(LOG_TAG, "restartLoader() in onCreate()");
                mReloadFromApi = true;
                restartLoader(LOADER_ID_POPULAR_RESULTS, getString(R.string.first_page));
            }
        } else {
            Log.d(LOG_TAG, "Load from savedInstance in onCreate()");
            PopularResults popularResults = savedInstanceState.getParcelable(LIFECYCLE_CALLBACKS_POPULAR_RESULTS_KEY);
            mMoviesAdapter.setData(popularResults);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIFECYCLE_CALLBACKS_POPULAR_RESULTS_KEY, mMoviesAdapter.getData());
        Log.d(LOG_TAG, "onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        // unregister the preference change listener
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setLayoutManager(int span_count) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, span_count);
        mActivityMainBinding.moviesList.setLayoutManager(layoutManager);
    }

//    private void restartApiLoader(String page_number) {
//        Bundle args = new Bundle();
//        args.putString(LOADER_EXTRA_PAGE_NUMBER, page_number);
//        args.putString(LOADER_EXTRA_SORT_ORDER, mSortOrder);
//        getSupportLoaderManager().restartLoader(LOADER_ID_POPULAR_RESULTS, args, this).forceLoad();
//    }
//
//    private void restartDbLoader() {
//        getSupportLoaderManager().restartLoader(LOADER_ID_POPULAR_RESULTS_FAVORITES, null, this).forceLoad();
//    }

    private void restartLoader(int loaderId, @Nullable String page_number) {
        switch (loaderId) {
            case LOADER_ID_POPULAR_RESULTS:
                Bundle args = new Bundle();
                args.putString(LOADER_EXTRA_PAGE_NUMBER, page_number);
                args.putString(LOADER_EXTRA_SORT_ORDER, mSortOrder);
                getSupportLoaderManager().restartLoader(LOADER_ID_POPULAR_RESULTS, args, this).forceLoad();
                break;
            case LOADER_ID_POPULAR_RESULTS_FAVORITES:
                getSupportLoaderManager().restartLoader(LOADER_ID_POPULAR_RESULTS_FAVORITES, null, this).forceLoad();
                break;
            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);

        }
    }

    private void readSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSpanCount = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_grid_column_count_key), getString(R.string.pref_grid_column_count_default)));
        mSortOrder = sharedPreferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
        mLastSortOrder = sharedPreferences.getString(getString(R.string.pref_last_sort_key), getString(R.string.pref_sort_default));
    }

    private void writeSortOrderToSharedPreferences(String last_sort_order, String sort_order) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_last_sort_key), last_sort_order);
        editor.putString(getString(R.string.pref_sort_key), sort_order);
        editor.apply();
    }

    @NonNull
    @Override
    public Loader<PopularResults> onCreateLoader(int loaderId, @Nullable final Bundle args) {
        switch (loaderId) {
            case LOADER_ID_POPULAR_RESULTS:
                return new AsyncTaskLoader<PopularResults>(this) {
                    @Nullable
                    @Override
                    public PopularResults loadInBackground() {
                        if (NetworkUtils.isOnline(getContext())) {
                            String page_number = args.getString(LOADER_EXTRA_PAGE_NUMBER);
                            String sort_order = args.getString(LOADER_EXTRA_SORT_ORDER);
                            return NetworkUtils.readPopularResultsFromApi(getContext(), page_number, sort_order);
                        } else {
                            return null;
                        }
                        // You may skip loading from API by uncommenting following line to test other parts of the app.
//                        return NetworkUtils.readPopularResultsFromFakeUtils(getContext());
                    }
                };
            case LOADER_ID_POPULAR_RESULTS_FAVORITES:
                return new AsyncTaskLoader<PopularResults>(this) {
                    @Nullable
                    @Override
                    public PopularResults loadInBackground() {
                        return DbUtils.readPopularResultsFromDB(getContext());
                    }
                };
            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<PopularResults> loader, PopularResults data) {
        switch (loader.getId()) {
            case LOADER_ID_POPULAR_RESULTS:
                if (data == null) {
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
//                    mMoviesAdapter.setData(null);
                } else {
                    if (mReloadFromApi) {
                        mMoviesAdapter.setData(data);
                    } else {
                        //if the loader is called through setOnLastItemReachedListener then addToData
                        mMoviesAdapter.addToData(data);
                    }
                }
                break;
            case LOADER_ID_POPULAR_RESULTS_FAVORITES:
                if (data == null) {
                    Toast.makeText(this, getString(R.string.no_favorites), Toast.LENGTH_LONG).show();
                    mMoviesAdapter.setData(null);
                } else {
                    mMoviesAdapter.setData(data);
                }
                break;
            default:
                throw new RuntimeException("Loader not implemented: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<PopularResults> loader) {
        mMoviesAdapter.setData(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movies_menu, menu);
        readSharedPreferences();
        menu.getItem(0).setIcon(UiUtils.getImageResourceForFavoriteButton(mSortOrder.equals(getString(R.string.pref_sort_favorites))));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_show_favorites:
                readSharedPreferences();
                if (mSortOrder.equals(getString(R.string.pref_sort_favorites))) {
                    item.setIcon(UiUtils.getImageResourceForFavoriteButton(false));
                    writeSortOrderToSharedPreferences(getString(R.string.pref_sort_favorites), mLastSortOrder);
                } else {
                    item.setIcon(UiUtils.getImageResourceForFavoriteButton(true));
                    writeSortOrderToSharedPreferences(mSortOrder, getString(R.string.pref_sort_favorites));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Since preferences are updates read them from SharedPreferences again
        readSharedPreferences();
        invalidateOptionsMenu();
        if (key.equals(getString(R.string.pref_grid_column_count_key))) {
            // if GRID COLUMN COUNT is changed set a new layout manager with new Span Count
            setLayoutManager(mSpanCount);
        } else if (key.equals(getString(R.string.pref_sort_key))) {
            if (mSortOrder.equals(getString(R.string.pref_sort_favorites))) {
                restartLoader(LOADER_ID_POPULAR_RESULTS_FAVORITES, null);
            } else {
                // if SORT BY option is changed, reload from api with the new api context
                mReloadFromApi = true;
                restartLoader(LOADER_ID_POPULAR_RESULTS, getString(R.string.first_page));
            }
        }

    }


    @Override
    public void onClick(Movie movie, int adapterPosition, ImageView aMovieImageView) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(INTENT_EXTRA_MOVIE, movie);
        intent.putExtra(INTENT_EXTRA_IS_FAVORITES_SELECTED, mSortOrder.equals(getString(R.string.pref_sort_favorites)));
        if (MiscUtils.LOLLIPOP_AND_HIGHER) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, aMovieImageView, getString(R.string.shared_element_movie_image_view));
            startActivityForResult(intent,
                    getResources().getInteger(R.integer.is_favorites_updated_request),
                    options.toBundle());
        } else {
            startActivityForResult(intent,
                    getResources().getInteger(R.integer.is_favorites_updated_request));
        }

        mAdapterLastPosition = adapterPosition;
    }

    // if user updated favorites in detail view then we should reload the data.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getResources().getInteger(R.integer.is_favorites_updated_request)) {
            if (resultCode == getResources().getInteger(R.integer.favorites_updated)) {
//                if (mSortOrder.equals(getString(R.string.pref_sort_favorites))) {
//                    restartLoader(LOADER_ID_POPULAR_RESULTS_FAVORITES, null);
//                } else {
//                    mReloadFromApi = true;
//                    restartLoader(LOADER_ID_POPULAR_RESULTS, getString(R.string.first_page));
//                }
                mMoviesAdapter.toggleFavorites(mAdapterLastPosition);
            } else {
                mAdapterLastPosition = RecyclerView.NO_POSITION;
            }
        }
    }
}
