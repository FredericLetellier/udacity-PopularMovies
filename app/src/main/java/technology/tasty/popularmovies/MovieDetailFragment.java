package technology.tasty.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import technology.tasty.popularmovies.data.MoviesContract;
import technology.tasty.popularmovies.sync.ImdbSyncAdapter;

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

    private Cursor movieData;

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

    private static final int VIDEO_LOADER = 1;

    private static final String[] VIDEO_COLUMNS = {

            MoviesContract.VideosEntry.TABLE_NAME + "." + MoviesContract.VideosEntry._ID,
            MoviesContract.VideosEntry.COLUMN_MOVIE_KEY,
            MoviesContract.VideosEntry.COLUMN_NAME,
            MoviesContract.VideosEntry.COLUMN_SITE,
            MoviesContract.VideosEntry.COLUMN_SITE_KEY
    };

    static final int COL_VIDEO_ID = 0;
    static final int COL_VIDEO_MOVIE_KEY = 1;
    static final int COL_VIDEO_NAME = 2;
    static final int COL_VIDEO_SITE = 3;
    static final int COL_VIDEO_SITE_KEY = 4;

    private static final int REVIEW_LOADER = 2;

    private static final String[] REVIEW_COLUMNS = {

            MoviesContract.ReviewsEntry.TABLE_NAME + "." + MoviesContract.ReviewsEntry._ID,
            MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY,
            MoviesContract.ReviewsEntry.COLUMN_AUTHOR,
            MoviesContract.ReviewsEntry.COLUMN_CONTENT
    };

    static final int COL_REVIEW_ID = 0;
    static final int COL_REVIEW_MOVIE_KEY = 1;
    static final int COL_REVIEW_AUTHOR = 2;
    static final int COL_REVIEW_CONTENT = 3;

    private RecyclerView videoRecyclerView;
    private VideoRecyclerViewAdapter videoRecyclerViewAdapter;
    private RecyclerView reviewRecyclerView;
    private ReviewRecyclerViewAdapter reviewRecyclerViewAdapter;

    private CollapsingToolbarLayout appBarLayout;
    private FloatingActionButton favButton;
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

            SyncData syncData = new SyncData();
            syncData.execute();

            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
            getLoaderManager().initLoader(VIDEO_LOADER, null, this);
            getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Activity activity = this.getActivity();
        appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        activity.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFab();
            }
        });

        rootView = inflater.inflate(R.layout.movie_detail, container, false);

        videoRecyclerView = (RecyclerView) rootView.findViewById(R.id.video_list);
        assert videoRecyclerView != null;
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(videoRecyclerView.getContext()));
        videoRecyclerViewAdapter = new VideoRecyclerViewAdapter(getContext(), null);
        videoRecyclerView.setAdapter(videoRecyclerViewAdapter);

        reviewRecyclerView = (RecyclerView) rootView.findViewById(R.id.review_list);
        assert reviewRecyclerView != null;
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(videoRecyclerView.getContext()));
        reviewRecyclerViewAdapter = new ReviewRecyclerViewAdapter(getContext(), null);
        reviewRecyclerView.setAdapter(reviewRecyclerViewAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader;
        Uri uri;
        String selection;
        String[] selectionArgs;

        switch (i) {
            case MOVIE_LOADER:
                uri = MoviesContract.MoviesEntry.CONTENT_URI;
                selection = MoviesContract.MoviesEntry._ID + " = ?";
                selectionArgs = new String[] {mMovieId};

                loader = new CursorLoader(getContext(),
                        uri,
                        MOVIE_COLUMNS,
                        selection,
                        selectionArgs,
                        null);
                break;
            case VIDEO_LOADER:
                uri = MoviesContract.VideosEntry.CONTENT_URI;
                selection = MoviesContract.VideosEntry.COLUMN_MOVIE_KEY + " = ?";
                selectionArgs = new String[] {mMovieId};

                loader = new CursorLoader(getContext(),
                        uri,
                        VIDEO_COLUMNS,
                        selection,
                        selectionArgs,
                        null);
                break;
            case REVIEW_LOADER:
                uri = MoviesContract.ReviewsEntry.CONTENT_URI;
                selection = MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " = ?";
                selectionArgs = new String[] {mMovieId};

                loader = new CursorLoader(getContext(),
                        uri,
                        REVIEW_COLUMNS,
                        selection,
                        selectionArgs,
                        null);
                break;
            default:
                loader = null;
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case MOVIE_LOADER:
                movieData = data;

                Activity activity = this.getActivity();

                if (data.moveToFirst()) {

                    activity.findViewById(R.id.coordinatorLayout).setVisibility(View.VISIBLE);

                    appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                    appBarLayout.setTitle(data.getString(MovieDetailFragment.COL_MOVIE_ORIGINALTITLE));
                    Picasso.with(getContext())
                            .load("http://image.tmdb.org/t/p/w342/" + data.getString(MovieDetailFragment.COL_MOVIE_POSTERPATH))
                            .fit().centerCrop()
                            .placeholder(R.drawable.ic_movie_filter_grey_24dp)
                            .error(R.drawable.ic_error_outline_grey_24dp)
                            .into((ImageView) activity.findViewById(R.id.background_toolbar));

                    displayFab(data.getString(MovieDetailFragment.COL_MOVIE_BOOKMARK));

                    // Show the dummy content as text in a TextView.
                    ((TextView) rootView.findViewById(R.id.movie_date_duration)).setText(data.getString(MovieDetailFragment.COL_MOVIE_RELEASEDATE));
                    String voteaverage = String.valueOf(data.getString(MovieDetailFragment.COL_MOVIE_VOTEAVERAGE))+"/10";
                    ((TextView) rootView.findViewById(R.id.movie_voteaverage)).setText(voteaverage);
                    ((TextView) rootView.findViewById(R.id.movie_detail)).setText(data.getString(MovieDetailFragment.COL_MOVIE_OVERVIEW));

                }else{
                    activity.findViewById(R.id.coordinatorLayout).setVisibility(View.INVISIBLE);
                }

                break;
            case VIDEO_LOADER:
                videoRecyclerViewAdapter.swapCursor(data);
                break;
            case REVIEW_LOADER:
                reviewRecyclerViewAdapter.swapCursor(data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case MOVIE_LOADER:
                movieData = null;
                break;
            case VIDEO_LOADER:
                videoRecyclerViewAdapter.swapCursor(null);
                break;
            case REVIEW_LOADER:
                reviewRecyclerViewAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

    public void displayFab(String displayOn){
        favButton = (FloatingActionButton) this.getActivity().findViewById(R.id.fab);
        if (displayOn.equals("1")){
            favButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_star_black_24dp));
        } else {
            favButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_star_border_black_24dp));
        }
    }

    public void updateFab(){
        if (movieData.moveToFirst()){
            int favState = movieData.getInt(MovieDetailFragment.COL_MOVIE_BOOKMARK);
            int newFavState;

            if (favState == 1){
                newFavState = 0;
            } else if (favState == 0) {
                newFavState = 1;
            } else {
                return;
            }

            Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
            String selection = MoviesContract.MoviesEntry._ID + " = ?";
            String[] selectionArgs = new String[] {movieData.getString(MovieDetailFragment.COL_MOVIE_ID)};

            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_BOOKMARK, newFavState);

            getContext().getContentResolver().update(
                    uri,
                    movieValues,
                    selection,
                    selectionArgs);

            /*displayFab(newFavState);*/
        }
    }

    public class SyncData extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            ImdbSyncAdapter imdbSyncAdapter = new ImdbSyncAdapter(getContext(), true);
            imdbSyncAdapter.ImdbSync(ImdbSyncAdapter.SYNC_MOVIE, mMovieId, 1);
            imdbSyncAdapter.ImdbSync(ImdbSyncAdapter.SYNC_MOVIE_REVIEWS, mMovieId, 1);
            imdbSyncAdapter.ImdbSync(ImdbSyncAdapter.SYNC_MOVIE_VIDEOS, mMovieId, 1);
            return null;
        }
    }
}
