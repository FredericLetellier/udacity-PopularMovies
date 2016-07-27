package technology.tasty.popularmovies;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Popular Movies
 * Created on 27/07/2016 by Espace de travail.
 */
public class ReviewRecyclerViewAdapter
        extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursorReviews;

    public ReviewRecyclerViewAdapter(Context context, Cursor cursorMovies) {
        mContext = context;
        mCursorReviews = cursorMovies;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursorReviews) {
            return null;
        }
        Cursor oldCursor = mCursorReviews;
        mCursorReviews = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mView.setTag(position);

        mCursorReviews.moveToPosition(position);
        holder.mReviewAuthorTextView.setText(mCursorReviews.getString(MovieDetailFragment.COL_REVIEW_AUTHOR) + ": ");
        holder.mReviewContentTextView.setText(mCursorReviews.getString(MovieDetailFragment.COL_REVIEW_CONTENT));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (holder.mReviewContentTextView.getMaxLines() == 3){
                    holder.mReviewContentTextView.setMaxLines(Integer.MAX_VALUE);
                } else {
                    holder.mReviewContentTextView.setMaxLines(3);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mCursorReviews != null){
            return mCursorReviews.getCount();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mReviewAuthorTextView;
        public final TextView mReviewContentTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mReviewAuthorTextView = (TextView) view.findViewById(R.id.review_author);
            mReviewContentTextView = (TextView) view.findViewById(R.id.review_content);
        }
    }
}
