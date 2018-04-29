package com.example.android.PopularMovies.utilities;

import android.content.Context;
import android.util.Log;

import com.example.android.PopularMovies.R;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.PopularResults;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final String LOG_TAG = JsonUtils.class.getSimpleName();

    // Constants for Movie Json
    private static final String VOTE_COUNT = "vote_count";
    private static final String ID = "id";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String TITLE = "title";
    private static final String POPULARITY = "popularity";
    private static final String POSTER_PATH = "poster_path";
    private static final String ORIGINAL_LANGUAGE = "original_language";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String GENRE_IDS = "genre_ids";
    private static final String BACKDROP_PATH = "backdrop_path";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";

    // Constants for PopularResults Json
    private static final String PAGE = "page";
    private static final String TOTAL_RESULTS = "total_results";
    private static final String TOTAL_PAGES = "total_pages";
    private static final String RESULTS = "results";


    /* This method extracts movie information from Popular Results json.
     It is only called from within getPopularResultsFromJson thus it doesn't need to be public
     */
    private static Movie getMovieFromResultsJson(JSONObject resultsJson, Context context) throws JSONException {

        Movie movie = new Movie();

        movie.setVoteCount(resultsJson.getInt(VOTE_COUNT));
        movie.setId(resultsJson.getInt(ID));
        movie.setVoteAverage(resultsJson.getDouble(VOTE_AVERAGE));
        movie.setTitle(resultsJson.getString(TITLE));
        movie.setPopularity(resultsJson.getDouble(POPULARITY));
        movie.setPosterPath(resultsJson.getString(POSTER_PATH));
        movie.setOriginalLanguage(resultsJson.getString(ORIGINAL_LANGUAGE));
        movie.setOriginalTitle(resultsJson.getString(ORIGINAL_TITLE));

        List<Integer> genre_ids = new ArrayList<>();
        JSONArray genre_ids_json = resultsJson.getJSONArray(GENRE_IDS);
        for (int i = 0; i < genre_ids_json.length(); i++) {
            genre_ids.add((Integer) genre_ids_json.get(i));
        }
        movie.setGenreIds(genre_ids);

        movie.setBackdropPath(resultsJson.getString(BACKDROP_PATH));
        movie.setOverview(resultsJson.getString(OVERVIEW));

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_format));
            movie.setReleaseDate(dateFormat.parse(resultsJson.getString(RELEASE_DATE)));
        } catch (ParseException e) {
            Log.d(LOG_TAG, "Unable to cast date: " + resultsJson.getString(RELEASE_DATE));
            e.printStackTrace();
        }

        return movie;
    }


    // This method extracts information from @link NetworksUtils.API_POPULAR_RESULTS_LINK
    public static PopularResults getPopularResultsFromJson(String jsonString, Context context) throws JSONException {

        PopularResults popularResults = new PopularResults();
        JSONObject jsonObject = new JSONObject(jsonString);

        popularResults.setLastPage(jsonObject.getInt(PAGE));
        popularResults.setTotalResults(jsonObject.getInt(TOTAL_RESULTS));
        popularResults.setTotalPages(jsonObject.getInt(TOTAL_PAGES));

        JSONArray resultsJson = jsonObject.getJSONArray(RESULTS);
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < resultsJson.length(); i++) {
            movies.add(getMovieFromResultsJson(resultsJson.getJSONObject(i), context));
        }
        popularResults.setResults(movies);

        return popularResults;
    }

}
