package com.example.android.PopularMovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.PopularMovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_IMAGE = "image";

    public static final String SUB_PATH_FRONT = "front";
    public static final String SUB_PATH_BACK = "back";


    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_GENRE_IDS = "genre_ids";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";


        public static Uri buildMovieUriWithId(int id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(id))
                    .build();
        }
    }

    public static final class ImageEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_IMAGE)
                .build();

        public static final String TABLE_NAME = "image";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_FRONT_BACK = "front_back";
        public static final String COLUMN_BITMAP = "image_bitmap";

        public static Uri buildImageUriWithFrontBack(String front_back) {
            return CONTENT_URI.buildUpon()
                    .appendPath(front_back)
                    .build();
        }

        public static Uri buildImageUriWithFrontBackAndId(String front_back, int id) {
            return buildImageUriWithFrontBack(front_back).buildUpon()
                    .appendPath(Integer.toString(id))
                    .build();
        }

    }
}
