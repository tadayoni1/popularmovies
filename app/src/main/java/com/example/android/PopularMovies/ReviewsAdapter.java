package com.example.android.PopularMovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.PopularMovies.model.Reviews;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();
    private final Context mContext;

    private Reviews mReviews;

    public ReviewsAdapter(Context mContext) {
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_review, parent, false);

        view.setFocusable(true);

        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsAdapterViewHolder holder, int position) {
        holder.mAuthorTextView.setText(mReviews.getResults().get(position).getAuthor());
        holder.mContentTextView.setText(mReviews.getResults().get(position).getContent());
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) {
            return 0;
        } else {
            return mReviews.getResults().size();
        }
    }

    public void setData(Reviews reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }

    public Reviews getData() {
        return mReviews;
    }

    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mAuthorTextView;
        public final TextView mContentTextView;


        public ReviewsAdapterViewHolder(View itemView) {
            super(itemView);
            mAuthorTextView = itemView.findViewById(R.id.author_tv);
            mContentTextView = itemView.findViewById(R.id.content_tv);
        }
    }
}
