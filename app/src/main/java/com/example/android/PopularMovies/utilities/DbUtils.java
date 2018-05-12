package com.example.android.PopularMovies.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.android.PopularMovies.data.MovieContract;
import com.example.android.PopularMovies.data.MovieContract.ImageEntry;
import com.example.android.PopularMovies.data.MovieContract.MovieEntry;
import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.PopularResults;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class DbUtils {

    private static final String LOG_TAG = DbUtils.class.getSimpleName();


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
            if (!str.trim().equals("")) {
                genre_ids.add(Integer.parseInt(str.trim()));
            }
        }
        movie.setGenreIds(genre_ids);

        movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH)));
        movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW)));
        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE)));

        movie.setMarkedAsFavorite(true);

        return movie;
    }

    public static ContentValues persistMovieBitmaps(Context context, Bitmap bitmap, String sub_path, int id) {

        ContentValues contentValues = new ContentValues();

        Log.d(LOG_TAG, "sub_path: " + sub_path);
        Cursor cursor = context.getContentResolver().query(
                ImageEntry.buildImageUriWithFrontBackAndId(sub_path, id),
                new String[]{ImageEntry.COLUMN_ID},
                MovieContract.ImageEntry.COLUMN_ID + " = ? AND " + MovieContract.ImageEntry.COLUMN_FRONT_BACK + " = ? ",
                new String[]{String.valueOf(id), sub_path},
                null);

        if (!cursor.moveToFirst()) {
            Log.d(LOG_TAG, "persisting image to db: " + sub_path + " id: " + id);
            contentValues.put(ImageEntry.COLUMN_ID, id);
            contentValues.put(ImageEntry.COLUMN_FRONT_BACK, sub_path);
            contentValues.put(ImageEntry.COLUMN_BITMAP, BitmapUtils.getBytes(bitmap));
            context.getContentResolver().insert(
                    ImageEntry.CONTENT_URI,
                    contentValues);
        }
        cursor.close();

        return contentValues;
    }

    public static Bitmap getBitmapFromCursor(Cursor cursor) {
        return BitmapUtils.getImage(cursor.getBlob(cursor.getColumnIndex(ImageEntry.COLUMN_BITMAP)));
    }

    public static void updateFavorites(final Context context, boolean isMarkedAsFavorite, final Movie movie) {
        Log.d(LOG_TAG, "isMarkedAsFavorite: " + isMarkedAsFavorite);
        if (isMarkedAsFavorite) {
            context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                    DbUtils.createContentValuesFromMovie(movie));

            Picasso.with(context)
                    .load(NetworkUtils.getPosterUrl(movie.getPosterPath(), context))
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            persistMovieBitmaps(context, bitmap, MovieContract.SUB_PATH_FRONT, movie.getId());
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });

            Picasso.with(context)
                    .load(NetworkUtils.getPosterUrl(movie.getBackdropPath(), context))
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Log.d(LOG_TAG, "onBitmapLoaded");
                            persistMovieBitmaps(context, bitmap, MovieContract.SUB_PATH_BACK, movie.getId());
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        } else {
            context.getContentResolver().delete(MovieContract.MovieEntry.buildMovieUriWithId(movie.getId()),
                    null,
                    null);
            context.getContentResolver().delete(ImageEntry.buildImageUriWithFrontBackAndId(MovieContract.SUB_PATH_FRONT, movie.getId()),
                    null,
                    null);
            context.getContentResolver().delete(ImageEntry.buildImageUriWithFrontBackAndId(MovieContract.SUB_PATH_BACK, movie.getId()),
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
