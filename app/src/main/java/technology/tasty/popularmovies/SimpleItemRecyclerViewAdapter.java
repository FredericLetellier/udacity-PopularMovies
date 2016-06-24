package technology.tasty.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Popular Movies
 * Created on 24/06/2016 by Espace de travail.
 */
public class SimpleItemRecyclerViewAdapter
        extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Movie> mMovies;
    private final Boolean mTwoPane;

    public SimpleItemRecyclerViewAdapter(Context context, List<Movie> movies, Boolean twoPane) {
        mContext = context;
        mMovies = movies;
        mTwoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mMovie = mMovies.get(position);
        Picasso.with(mContext)
                .load("http://image.tmdb.org/t/p/w185/" + holder.mMovie.getPosterPath())
                .fit().centerCrop()
                .into(holder.mPosterView);
        holder.mPosterView.setContentDescription(holder.mMovie.getOriginalTitle());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(MovieDetailFragment.ARG_MOVIE, holder.mMovie);
                    MovieDetailFragment fragment = new MovieDetailFragment();
                    fragment.setArguments(arguments);
                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MovieDetailActivity.class);
                    intent.putExtra(MovieDetailFragment.ARG_MOVIE, holder.mMovie);

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mPosterView;
        public Movie mMovie;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPosterView = (ImageView) view.findViewById(R.id.poster);
        }
    }
}
