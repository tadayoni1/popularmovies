package com.example.android.PopularMovies;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.PopularMovies.databinding.ActivityMainBinding;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.PopularResults;
import com.example.android.PopularMovies.utilities.NetworkUtils;

public class MainActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<PopularResults>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final static int LOADER_ID_POPULAR_RESULTS = 44;
    private final static String LOADER_EXTRA_PAGE_NUMBER = "loader-extra-page-number";
    private final static String LOADER_EXTRA_SORT_ORDER = "loader-extra-sort-order";

    private final static String LIFECYCLE_CALLBACKS_POPULAR_RESULTS_KEY = "callbacks-popular-results-key";

    public static final String INTENT_EXTRA_MOVIE = "intent-extra-movie";

    private ActivityMainBinding mActivityMainBinding;

    private MoviesAdapter mMoviesAdapter;

    private int mSpanCount;
    private String mSortOrder;
    private boolean mReloadFromApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                mReloadFromApi = false;
                String page_number = String.valueOf(++last_page);
                restartLoader(page_number);
            }
        });

        if (savedInstanceState == null || !(savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_POPULAR_RESULTS_KEY))) {
            Log.d(LOG_TAG, "restartLoader() in onCreate()");
            mReloadFromApi = true;
            restartLoader(getString(R.string.first_page));
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

    private void restartLoader(String page_number) {
        Bundle args = new Bundle();
        args.putString(LOADER_EXTRA_PAGE_NUMBER, page_number);
        args.putString(LOADER_EXTRA_SORT_ORDER, mSortOrder);
        getSupportLoaderManager().restartLoader(LOADER_ID_POPULAR_RESULTS, args, this).forceLoad();
    }

    private void readSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSpanCount = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_grid_column_count_key), getString(R.string.pref_grid_column_count_default)));
        mSortOrder = sharedPreferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
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
                }
                if (mReloadFromApi) {
                    mMoviesAdapter.setData(data);
                } else {
                    //if the loader is called through setOnLastItemReachedListener then addToData
                    mMoviesAdapter.addToData(data);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Since preferences are updates read them from SharedPreferences again
        readSharedPreferences();
        if (key.equals(getString(R.string.pref_grid_column_count_key))) {
            // if GRID COLUMN COUNT is changed set a new layout manager with new Span Count
            setLayoutManager(mSpanCount);
        } else if (key.equals(getString(R.string.pref_sort_key))) {
            // if SORT BY option is changed, reload from api with the new api context
            int last_page = mMoviesAdapter.getLastPage();
            if (last_page > 0) {
                mReloadFromApi = true;
                restartLoader(String.valueOf(last_page));
            }
        }

    }

    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(INTENT_EXTRA_MOVIE, movie);
        startActivity(intent);
    }
}
