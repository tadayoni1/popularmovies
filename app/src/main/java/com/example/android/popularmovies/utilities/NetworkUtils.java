package com.example.android.PopularMovies.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.android.PopularMovies.R;
import com.example.android.PopularMovies.model.PopularResults;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    private static final String API_POPULAR_RESULTS_LINK = "http://api.themoviedb.org/3/movie/%s?api_key=%s&page=%s";
    private static final String API_POPULAR_CONTEXT = "popular";
    private static final String API_TOP_RATED_CONTEXT = "top_rated";


    private static final String API_POSTER_BASE_LINK = "http://image.tmdb.org/t/p/%s";
    private static final String API_POSTER_SIZE_NORMAL = "w185";
    private static final String API_POSTER_SIZE_LARGE = "w500";

    private static final String API_KEY = "";

    public static String getPosterUrl(String posterFile, Context context) {
        if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return String.format(API_POSTER_BASE_LINK, API_POSTER_SIZE_LARGE) + posterFile;
        } else {
            return String.format(API_POSTER_BASE_LINK, API_POSTER_SIZE_NORMAL) + posterFile;
        }
    }

    private static URL buildPopularResultsUrl(String page, String api_context) throws MalformedURLException {
        return new URL(String.format(API_POPULAR_RESULTS_LINK, api_context, API_KEY, page));
    }

    public static PopularResults readPopularResultsFromFakeUtils(Context context) {
        PopularResults popularResults;
        try {
            popularResults = JsonUtils.getPopularResultsFromJson(FakeUtils.POPULAR_RESULTS, context);
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Unable to parse results json");
            e.printStackTrace();
            return null;
        }
        return popularResults;
    }

    public static PopularResults readPopularResultsFromApi(Context context, String page, String sort_order) {
        URL url = null;
        String results = null;
        PopularResults popularResults;
        try {
            String api_context = "";
            if (sort_order.equals(context.getString(R.string.pref_sort_popular))) {
                api_context = API_POPULAR_CONTEXT;
            } else if (sort_order.equals(context.getString(R.string.pref_sort_rate))) {
                api_context = API_TOP_RATED_CONTEXT;
            }
            url = buildPopularResultsUrl(page, api_context);
            results = getResponseFromHttpUrl(url);
            popularResults = JsonUtils.getPopularResultsFromJson(results, context);
        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, "Unable to build url");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.d(LOG_TAG, "Unable to open url: " + url.toString());
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Unable to parse results json: " + results);
            e.printStackTrace();
            return null;
        }
        return popularResults;
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Log.v("getResponseFromHttpUrl", "Url: " + urlConnection.toString());
        try {
            InputStream in = urlConnection.getInputStream();
            Log.d("getResponseFromHttpUrl", "started");
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            Log.d("getResponseFromHttpUrl", "scanner initialized");

            boolean hasInput = scanner.hasNext();
            Log.d("getResponseFromHttpUrl", hasInput + "");
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        return netInfo.isConnectedOrConnecting();
    }

}
