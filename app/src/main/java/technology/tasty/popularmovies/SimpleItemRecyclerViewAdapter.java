package technology.tasty.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Popular Movies
 * Created on 24/06/2016 by Espace de travail.
 */
public class SimpleItemRecyclerViewAdapter
        extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {


    public interface MyCallback {
        void onItemClicked(String movieId);
    }

    private MyCallback listener;

    public void setOnItemClickListener(MyCallback callback){
        listener = callback;
    }

    private final Context mContext;
    private Cursor mCursorMovies;
    private final Boolean mTwoPane;

    public SimpleItemRecyclerViewAdapter(Context context, Cursor cursorMovies, Boolean twoPane) {
        mContext = context;
        mCursorMovies = cursorMovies;
        mTwoPane = twoPane;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursorMovies) {
            return null;
        }
        Cursor oldCursor = mCursorMovies;
        mCursorMovies = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mView.setTag(position);

        mCursorMovies.moveToPosition(position);
        Picasso.with(mContext)
                .load("http://image.tmdb.org/t/p/w185/" + mCursorMovies.getString(MovieListActivity.COL_MOVIE_POSTERPATH))
                .fit().centerCrop()
                .placeholder(R.drawable.ic_movie_filter_grey_24dp)
                .error(R.drawable.ic_error_outline_grey_24dp)
                .into(holder.mPosterView);
        holder.mPosterView.setContentDescription(mCursorMovies.getString(MovieListActivity.COL_MOVIE_ORIGINALTITLE));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                mCursorMovies.moveToPosition(position);

                if (mTwoPane) {
                    if (listener != null) {
                        final String movieId = mCursorMovies.getString(MovieListActivity.COL_MOVIE_ID);
                        listener.onItemClicked(movieId);
                    }
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MovieDetailActivity.class);
                    intent.putExtra(MovieDetailFragment.ARG_MOVIE, mCursorMovies.getString(MovieListActivity.COL_MOVIE_ID));

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCursorMovies != null){
            return mCursorMovies.getCount();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mPosterView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPosterView = (ImageView) view.findViewById(R.id.poster);
        }
    }
}
