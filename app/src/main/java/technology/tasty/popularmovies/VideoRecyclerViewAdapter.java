package technology.tasty.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Popular Movies
 * Created on 27/07/2016 by Espace de travail.
 */
public class VideoRecyclerViewAdapter
        extends RecyclerView.Adapter<VideoRecyclerViewAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursorVideos;

    public VideoRecyclerViewAdapter(Context context, Cursor cursorMovies) {
        mContext = context;
        mCursorVideos = cursorMovies;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursorVideos) {
            return null;
        }
        Cursor oldCursor = mCursorVideos;
        mCursorVideos = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mView.setTag(position);

        mCursorVideos.moveToPosition(position);
        holder.mTitleTextView.setText(mCursorVideos.getString(MovieDetailFragment.COL_VIDEO_NAME));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                mCursorVideos.moveToPosition(position);

                Context context = v.getContext();
                String url = "http://www." + mCursorVideos.getString(MovieDetailFragment.COL_VIDEO_SITE) + ".com/watch?v="  + mCursorVideos.getString(MovieDetailFragment.COL_VIDEO_SITE_KEY);
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCursorVideos != null){
            return mCursorVideos.getCount();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleTextView = (TextView) view.findViewById(R.id.video_title);
        }
    }
}

