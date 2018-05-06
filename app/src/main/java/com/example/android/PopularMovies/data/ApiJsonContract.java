package com.example.android.PopularMovies.data;

public class ApiJsonContract {

    public class MovieJsonContract {
        // Constants for Movie Json
        public static final String VOTE_COUNT = "vote_count";
        public static final String ID = "id";
        public static final String VOTE_AVERAGE = "vote_average";
        public static final String TITLE = "title";
        public static final String POPULARITY = "popularity";
        public static final String POSTER_PATH = "poster_path";
        public static final String ORIGINAL_LANGUAGE = "original_language";
        public static final String ORIGINAL_TITLE = "original_title";
        public static final String GENRE_IDS = "genre_ids";
        public static final String BACKDROP_PATH = "backdrop_path";
        public static final String OVERVIEW = "overview";
        public static final String RELEASE_DATE = "release_date";
    }

    public class PopularResults {
        // Constants for PopularResults Json
        public static final String PAGE = "page";
        public static final String TOTAL_RESULTS = "total_results";
        public static final String TOTAL_PAGES = "total_pages";
        public static final String RESULTS = "results";
    }

    public static class Trailer {
        public static final String ID = "id";
        public static final String ISO_639_1 = "iso_639_1";
        public static final String ISO_3166_1 = "iso_3166_1";
        public static final String KEY = "key";
        public static final String NAME = "name";
        public static final String SITE = "site";
        public static final String SIZE = "size";
        public static final String TYPE = "type";

    }

    public class Videos {
        public static final String ID = "id";
        public static final String RESULTS = "results";
    }

    public class Review {
        public static final String AUTHOR = "author";
        public static final String CONTENT = "content";
        public static final String ID = "id";
        public static final String URL = "url";
    }

    public class Reviews {
        public static final String ID = "id";
        public static final String PAGE = "page";
        public static final String RESULTS = "results";
        public static final String TOTAL_RESULTS = "total_results";
        public static final String TOTAL_PAGES = "total_pages";
    }
}
