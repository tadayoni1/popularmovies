package com.example.android.PopularMovies.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.PopularMovies.data.MovieContract;
import com.example.android.PopularMovies.data.MovieContract.MovieEntry;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.PopularResults;

import java.util.ArrayList;
import java.util.List;

public class DbUtils {

    public static ContentValues createContentValuesFromMovie(Movie movie) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
        contentValues.put(MovieEntry.COLUMN_ID, movie.getId());
        contentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        contentValues.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
        contentValues.put(MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
        contentValues.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        contentValues.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movie.getOriginalLanguage());
        contentValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        contentValues.put(MovieEntry.COLUMN_GENRE_IDS, movie.getGenreIds().toString());
        contentValues.put(MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        contentValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate().toString());

        return contentValues;
    }

    public static Movie createMovieFromCursor(Cursor cursor, Context context) {
        Movie movie = new Movie();

        movie.setVoteCount(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT)));
        movie.setId(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ID)));
        movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE)));
        movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)));
        movie.setPopularity(cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY)));
        movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
        movie.setOriginalLanguage(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_LANGUAGE)));
        movie.setOriginalTitle(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE)));

        String genre_ids_string = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_GENRE_IDS));
        genre_ids_string = genre_ids_string.substring(1, genre_ids_string.length() - 1);
        String[] genre_ids_tokenized = genre_ids_string.split(",");
        List<Integer> genre_ids = new ArrayList<>();
        for (String str : genre_ids_tokenized) {
            genre_ids.add(Integer.parseInt(str.trim()));
        }
        movie.setGenreIds(genre_ids);

        movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH)));
        movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW)));
        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE)));

        movie.setMarkedAsFavorite(true);

        return movie;
    }

    public static void updateFavorites(Context context, boolean isMarkedAsFavorite, Movie movie) {
        if (isMarkedAsFavorite) {
            context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                    DbUtils.createContentValuesFromMovie(movie));
        } else {
            context.getContentResolver().delete(MovieContract.MovieEntry.buildMovieUriWithId(movie.getId()),
                    null,
                    null);
        }
    }

    public static PopularResults readPopularResultsFromDB(Context context) {
        Cursor cursor = context.getContentResolver().query(MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        List<Movie> movies = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                movies.add(createMovieFromCursor(cursor, context));
            } while (cursor.moveToNext());
        }
        if (movies.size() < 1) {
            return null;
        }
        PopularResults popularResults = new PopularResults();
        popularResults.setLastPage(1);
        popularResults.setTotalPages(1);
        popularResults.setTotalResults(movies.size());
        popularResults.setResults(movies);

        return popularResults;
    }
}
