package technology.tasty.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
                .into(holder.mPosterView);
        holder.mPosterView.setContentDescription(mCursorMovies.getString(MovieListActivity.COL_MOVIE_ORIGINALTITLE));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                mCursorMovies.moveToPosition(position);

                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(MovieDetailFragment.ARG_MOVIE, mCursorMovies.getString(MovieListActivity.COL_MOVIE_ID));
                    MovieDetailFragment fragment = new MovieDetailFragment();
                    fragment.setArguments(arguments);
                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
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
