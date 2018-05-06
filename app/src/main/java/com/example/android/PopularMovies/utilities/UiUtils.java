package com.example.android.PopularMovies.utilities;

import android.content.Context;
import android.widget.Toast;

import com.example.android.PopularMovies.R;

public class UiUtils {

    public static int getImageResourceForFavoriteButton(boolean isMarkedAsFavorite) {
        if (isMarkedAsFavorite) {
            return android.R.drawable.btn_star_big_on;
        } else {
            return android.R.drawable.btn_star_big_off;
        }
    }

    public static void showToastForFavoriteButton(Context context, boolean isMarkedAsFavorite, String title) {
        if (isMarkedAsFavorite) {
            Toast.makeText(context,
                    String.format(context.getString(R.string.format_added_as_favorite), title),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,
                    String.format(context.getString(R.string.format_removed_from_favorites), title),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
