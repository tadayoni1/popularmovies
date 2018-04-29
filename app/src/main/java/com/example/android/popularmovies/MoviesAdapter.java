package com.example.android.PopularMovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.PopularMovies.model.Movie;
import com.example.android.PopularMovies.model.PopularResults;
import com.example.android.PopularMovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();
    private final Context mContext;

    private PopularResults mPopularResults;

    private OnLastItemReachedListener onLastItemReachedListener;

    final private MoviesAdapterOnClickHandler mClickHandler;

    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public MoviesAdapter(Context context, MoviesAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    public int getLastPage() {
        if (mPopularResults != null) {
            return mPopularResults.getLastPage();
        } else {
            return -1;
        }
    }


    @NonNull
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_movie, parent, false);

        view.setFocusable(true);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesAdapterViewHolder holder, int position) {
        Picasso.with(mContext)
                .load(NetworkUtils.getPosterUrl(mPopularResults.getResults().get(position).getPosterPath(), mContext))
                .placeholder(R.drawable.placeholder_image)
                .into(holder.mMovieImageView);
        if (position >= getItemCount() - 1) {
            if (mPopularResults.getLastPage() < mPopularResults.getTotalPages()) {
                onLastItemReachedListener.onLastItemReached(mPopularResults.getLastPage());
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mPopularResults == null) {
            return 0;
        } else {
            return mPopularResults.getResults().size();
        }
    }

    public void setOnLastItemReachedListener(OnLastItemReachedListener onLastItemReachedListener) {
        this.onLastItemReachedListener = onLastItemReachedListener;
    }

    public PopularResults getData() {
        return mPopularResults;
    }

    public void setData(PopularResults popularResults) {
        mPopularResults = popularResults;
        notifyDataSetChanged();
    }

    public void addToData(PopularResults popularResults) {
        if (mPopularResults == null) {
            mPopularResults = new PopularResults();
        }
        mPopularResults.add(popularResults);
        notifyDataSetChanged();
    }


    public class MoviesAdapterViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView mMovieImageView;

        public MoviesAdapterViewHolder(View itemView) {
            super(itemView);
            mMovieImageView = itemView.findViewById(R.id.movie_poster_iv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie movie = mPopularResults.getResults().get(adapterPosition);
            mClickHandler.onClick(movie);
        }
    }

    public interface OnLastItemReachedListener {

        void onLastItemReached(int lastPage);

    }
}
