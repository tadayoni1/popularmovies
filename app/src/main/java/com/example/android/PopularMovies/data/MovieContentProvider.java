package com.example.android.PopularMovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class MovieContentProvider extends ContentProvider {

    private static final String LOG_TAG = MovieContentProvider.class.getSimpleName();

    public static final int CODE_MOVIE = 100;
    public static final int CODE_MOVIE_WITH_ID = 101;
    public static final int CODE_IMAGE = 200;
    public static final int CODE_IMAGE_WITH_FRONT_BACK_AND_ID = 201;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE, CODE_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", CODE_MOVIE_WITH_ID);

        matcher.addURI(authority, MovieContract.PATH_IMAGE, CODE_IMAGE);
        matcher.addURI(authority, MovieContract.PATH_IMAGE + "/*/#", CODE_IMAGE_WITH_FRONT_BACK_AND_ID);
        matcher.addURI(authority, MovieContract.PATH_IMAGE + "/*", CODE_IMAGE_WITH_FRONT_BACK_AND_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_MOVIE_WITH_ID:
                String movieIdSegment = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{movieIdSegment};
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_IMAGE_WITH_FRONT_BACK_AND_ID:
                List<String> segments = uri.getPathSegments();
                String[] newSelectionArguments = new String[]{segments.get(1), segments.get(2)};
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ImageEntry.TABLE_NAME,
                        projection,
                        MovieContract.ImageEntry.COLUMN_FRONT_BACK + " = ? AND " + MovieContract.ImageEntry.COLUMN_ID + " = ? ",
                        newSelectionArguments,
                        null,
                        null,
                        sortOrder);

                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {


        long _id = -1;
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE:
                Cursor cursor = query(MovieContract.MovieEntry.buildMovieUriWithId(values.getAsInteger(MovieContract.MovieEntry.COLUMN_ID)),
                        new String[]{MovieContract.MovieEntry.COLUMN_ID},
                        null,
                        null,
                        null);

                if (cursor.moveToFirst()) {
                    Log.d(LOG_TAG, "Attempted to save the same movie to db", new Exception());
                    cursor.close();
                    return uri;
                }
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    cursor.close();
                }
                break;

            case CODE_IMAGE:
                Cursor imageCursor = query(
                        MovieContract.ImageEntry.buildImageUriWithFrontBackAndId(values.getAsString(MovieContract.ImageEntry.COLUMN_FRONT_BACK), values.getAsInteger(MovieContract.ImageEntry.COLUMN_ID)),
                        new String[]{MovieContract.MovieEntry.COLUMN_ID},
                        null,
                        null,
                        null);

                if (imageCursor.moveToFirst()) {
                    Log.d(LOG_TAG, "Attempted to save the same image to db", new Exception());
                    imageCursor.close();
                    return uri;
                }
                final SQLiteDatabase imageDb = mOpenHelper.getWritableDatabase();
                imageDb.beginTransaction();
                try {
                    _id = imageDb.insert(MovieContract.ImageEntry.TABLE_NAME, null, values);

                    imageDb.setTransactionSuccessful();
                } finally {
                    imageDb.endTransaction();
                    imageCursor.close();
                }

                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (_id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        } else {
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int numRowsDeleted;


        if (null == selection) selection = "1";
        String movieIdSegment = uri.getLastPathSegment();
        String[] selectionArguments = new String[]{movieIdSegment};

        switch (sUriMatcher.match(uri)) {

            case CODE_MOVIE:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case CODE_MOVIE_WITH_ID:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry.COLUMN_ID + " = ? ",
                        selectionArguments);
                break;

            case CODE_IMAGE:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.ImageEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case CODE_IMAGE_WITH_FRONT_BACK_AND_ID:
                List<String> segments = uri.getPathSegments();
                String[] newSelectionArguments = new String[]{segments.get(1), segments.get(2)};
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.ImageEntry.TABLE_NAME,
                        MovieContract.ImageEntry.COLUMN_FRONT_BACK + " = ? AND " + MovieContract.ImageEntry.COLUMN_ID + " = ? ",
                        newSelectionArguments);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
