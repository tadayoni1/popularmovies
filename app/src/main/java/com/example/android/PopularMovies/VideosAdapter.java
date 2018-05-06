package com.example.android.PopularMovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.PopularMovies.model.Videos;
import com.example.android.PopularMovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder> {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    private static final String TRAILER_SITE_YOUTUBE = "YouTube";

    private final Context mContext;
    private Videos mVideos;

    public interface OnClickHandler {
        void onClick(String url);

    }

    final private OnClickHandler mOnClickHandler;

    public VideosAdapter(Context context, OnClickHandler clickHandler) {
        this.mContext = context;
        mOnClickHandler = clickHandler;
    }


    @NonNull
    @Override
    public VideosAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_trailer, parent, false);

        view.setFocusable(true);
        return new VideosAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideosAdapterViewHolder holder, int position) {
        Picasso.with(mContext)
                .load(NetworkUtils.getYouTubeThumbnailLink(mVideos.getResults().get(position).getKey()))
                .into(holder.mVideoImageView);
        holder.mVideoTitle.setText(mVideos.getResults().get(position).getName());

    }

    @Override
    public int getItemCount() {
        if (mVideos == null) {
            return 0;
        } else {
            return mVideos.getResults().size();
        }
    }

    public void setData(Videos videos) {
        if (videos != null) {
            videos.filterBy(TRAILER_SITE_YOUTUBE);
        }
        mVideos = videos;
        notifyDataSetChanged();
    }

    public Videos getData() {
        return mVideos;
    }

    public class VideosAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mVideoImageView;
        public final TextView mVideoTitle;

        public VideosAdapterViewHolder(View itemView) {
            super(itemView);
            mVideoImageView = itemView.findViewById(R.id.thumbnail_iv);
            mVideoTitle = itemView.findViewById(R.id.video_title_tv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String videoLink = NetworkUtils.getYouTubeVideoLink(mVideos.getResults().get(adapterPosition).getKey());
            mOnClickHandler.onClick(videoLink);
        }
    }
}

