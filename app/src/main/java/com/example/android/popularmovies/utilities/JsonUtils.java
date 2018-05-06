package com.example.android.PopularMovies.utilities;

import android.content.Context;
import android.database.Cursor;

import com.example.android.PopularMovies.data.ApiJsonContract;
import com.example.android.PopularMovies.data.MovieContract;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.PopularResults;
import com.example.android.PopularMovies.model.Review;
import com.example.android.PopularMovies.model.Reviews;
import com.example.android.PopularMovies.model.Trailer;
import com.example.android.PopularMovies.model.Videos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final String LOG_TAG = JsonUtils.class.getSimpleName();

    /* This method extracts movie information from Popular Results json.
     It is only called from within getPopularResultsFromJson thus it doesn't need to be public
     */
    private static Movie getMovieFromResultsJson(JSONObject resultsJson, Context context) throws JSONException {

        Movie movie = new Movie();

        movie.setVoteCount(resultsJson.getInt(ApiJsonContract.MovieJsonContract.VOTE_COUNT));
        movie.setId(resultsJson.getInt(ApiJsonContract.MovieJsonContract.ID));
        movie.setVoteAverage(resultsJson.getDouble(ApiJsonContract.MovieJsonContract.VOTE_AVERAGE));
        movie.setTitle(resultsJson.getString(ApiJsonContract.MovieJsonContract.TITLE));
        movie.setPopularity(resultsJson.getDouble(ApiJsonContract.MovieJsonContract.POPULARITY));
        movie.setPosterPath(resultsJson.getString(ApiJsonContract.MovieJsonContract.POSTER_PATH));
        movie.setOriginalLanguage(resultsJson.getString(ApiJsonContract.MovieJsonContract.ORIGINAL_LANGUAGE));
        movie.setOriginalTitle(resultsJson.getString(ApiJsonContract.MovieJsonContract.ORIGINAL_TITLE));

        List<Integer> genre_ids = new ArrayList<>();
        JSONArray genre_ids_json = resultsJson.getJSONArray(ApiJsonContract.MovieJsonContract.GENRE_IDS);
        for (int i = 0; i < genre_ids_json.length(); i++) {
            genre_ids.add((Integer) genre_ids_json.get(i));
        }
        movie.setGenreIds(genre_ids);

        movie.setBackdropPath(resultsJson.getString(ApiJsonContract.MovieJsonContract.BACKDROP_PATH));
        movie.setOverview(resultsJson.getString(ApiJsonContract.MovieJsonContract.OVERVIEW));
        movie.setReleaseDate(resultsJson.getString(ApiJsonContract.MovieJsonContract.RELEASE_DATE));

        Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.buildMovieUriWithId(movie.getId()),
                new String[]{MovieContract.MovieEntry.COLUMN_ID},
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            movie.setMarkedAsFavorite(true);
            cursor.close();
        } else {
            movie.setMarkedAsFavorite(false);
        }

        return movie;
    }


    // This method extracts information from @link NetworksUtils.API_POPULAR_RESULTS_LINK
    public static PopularResults getPopularResultsFromJson(String jsonString, Context context) throws JSONException {

        PopularResults popularResults = new PopularResults();
        JSONObject jsonObject = new JSONObject(jsonString);

        popularResults.setLastPage(jsonObject.getInt(ApiJsonContract.PopularResults.PAGE));
        popularResults.setTotalResults(jsonObject.getInt(ApiJsonContract.PopularResults.TOTAL_RESULTS));
        popularResults.setTotalPages(jsonObject.getInt(ApiJsonContract.PopularResults.TOTAL_PAGES));

        JSONArray resultsJson = jsonObject.getJSONArray(ApiJsonContract.PopularResults.RESULTS);
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < resultsJson.length(); i++) {
            movies.add(getMovieFromResultsJson(resultsJson.getJSONObject(i), context));
        }
        popularResults.setResults(movies);

        return popularResults;
    }

    private static Trailer getTrailerFromResultsJson(JSONObject resultsJson) throws JSONException {
        Trailer trailer = new Trailer();

        trailer.setId(resultsJson.getString(ApiJsonContract.Trailer.ID));
        trailer.setIso_639_1(resultsJson.getString(ApiJsonContract.Trailer.ISO_639_1));
        trailer.setIso_3166_1(resultsJson.getString(ApiJsonContract.Trailer.ISO_3166_1));
        trailer.setKey(resultsJson.getString(ApiJsonContract.Trailer.KEY));
        trailer.setName(resultsJson.getString(ApiJsonContract.Trailer.NAME));
        trailer.setSite(resultsJson.getString(ApiJsonContract.Trailer.SITE));
        trailer.setSize(resultsJson.getInt(ApiJsonContract.Trailer.SIZE));
        trailer.setType(resultsJson.getString(ApiJsonContract.Trailer.TYPE));

        return trailer;
    }

    public static Videos getVideosFromJson(String jsonString) throws JSONException {

        Videos videos = new Videos();
        JSONObject jsonObject = new JSONObject(jsonString);

        videos.setId(jsonObject.getInt(ApiJsonContract.Videos.ID));
        JSONArray resultsJson = jsonObject.getJSONArray(ApiJsonContract.Videos.RESULTS);
        List<Trailer> trailers = new ArrayList<>();
        for (int i = 0; i < resultsJson.length(); i++) {
            trailers.add(getTrailerFromResultsJson(resultsJson.getJSONObject(i)));
        }
        videos.setResults(trailers);

        return videos;
    }

    private static Review getReviewFromResultsJson(JSONObject resultsJson) throws JSONException {
        Review review = new Review();

        review.setAuthor(resultsJson.getString(ApiJsonContract.Review.AUTHOR));
        review.setContent(resultsJson.getString(ApiJsonContract.Review.CONTENT));
        review.setId(resultsJson.getString(ApiJsonContract.Review.ID));
        review.setUrl(resultsJson.getString(ApiJsonContract.Review.URL));

        return review;
    }

    public static Reviews getReviewsFromJson(String jsonString) throws JSONException {

        Reviews reviews = new Reviews();
        JSONObject jsonObject = new JSONObject(jsonString);

        reviews.setId(jsonObject.getInt(ApiJsonContract.Reviews.ID));
        reviews.setPage(jsonObject.getInt(ApiJsonContract.Reviews.PAGE));
        reviews.setTotalPages(jsonObject.getInt(ApiJsonContract.Reviews.TOTAL_PAGES));
        reviews.setTotalResults(jsonObject.getInt(ApiJsonContract.Reviews.TOTAL_RESULTS));
        JSONArray resultsJson = jsonObject.getJSONArray(ApiJsonContract.Reviews.RESULTS);
        List<Review> review = new ArrayList<>();
        for (int i = 0; i < resultsJson.length(); i++) {
            review.add(getReviewFromResultsJson(resultsJson.getJSONObject(i)));
        }
        reviews.setResults(review);

        return reviews;
    }

}
