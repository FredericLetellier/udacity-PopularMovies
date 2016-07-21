package technology.tasty.popularmovies;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import technology.tasty.popularmovies.data.MoviesContract;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_MOVIE = "movie";

    /**
     * The dummy content this fragment is presenting.
     */
    private String mMovieId;

    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {

            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_POSTERPATH,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_RELEASEDATE,
            MoviesContract.MoviesEntry.COLUMN_ORIGINALTITLE,
            MoviesContract.MoviesEntry.COLUMN_POPULARITY,
            MoviesContract.MoviesEntry.COLUMN_VOTEAVERAGE,
            MoviesContract.MoviesEntry.COLUMN_BOOKMARK
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_POSTERPATH = 1;
    static final int COL_MOVIE_OVERVIEW = 2;
    static final int COL_MOVIE_RELEASEDATE = 3;
    static final int COL_MOVIE_ORIGINALTITLE = 4;
    static final int COL_MOVIE_POPULARITY = 5;
    static final int COL_MOVIE_VOTEAVERAGE = 6;
    static final int COL_MOVIE_BOOKMARK = 7;

    private CollapsingToolbarLayout appBarLayout;
    private View rootView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_MOVIE)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mMovieId = (String) getArguments().getSerializable(ARG_MOVIE);
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Activity activity = this.getActivity();
        appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        rootView = inflater.inflate(R.layout.movie_detail, container, false);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
        String selection = MoviesContract.MoviesEntry._ID + " = ?";
        String[] selectionArgs = new String[] {mMovieId};

        return new CursorLoader(getContext(),
                uri,
                MOVIE_COLUMNS,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Activity activity = this.getActivity();
        appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        if (data.moveToFirst()) {

            if (appBarLayout != null) {
                appBarLayout.setTitle(data.getString(MovieDetailFragment.COL_MOVIE_ORIGINALTITLE));
                Picasso.with(getContext())
                        .load("http://image.tmdb.org/t/p/w342/" + data.getString(MovieDetailFragment.COL_MOVIE_POSTERPATH))
                        .fit().centerCrop()
                        .into((ImageView) activity.findViewById(R.id.background_toolbar));
            }

            // Show the dummy content as text in a TextView.
            ((TextView) rootView.findViewById(R.id.movie_date_duration)).setText(data.getString(MovieDetailFragment.COL_MOVIE_RELEASEDATE));
            String voteaverage = String.valueOf(data.getString(MovieDetailFragment.COL_MOVIE_VOTEAVERAGE))+"/10";
            ((TextView) rootView.findViewById(R.id.movie_voteaverage)).setText(voteaverage);
            ((TextView) rootView.findViewById(R.id.movie_detail)).setText(data.getString(MovieDetailFragment.COL_MOVIE_OVERVIEW));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
