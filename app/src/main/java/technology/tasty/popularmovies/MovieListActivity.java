package technology.tasty.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import technology.tasty.popularmovies.data.MoviesContract;
import technology.tasty.popularmovies.sync.ImdbSyncAdapter;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {

            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_POSTERPATH,
            MoviesContract.MoviesEntry.COLUMN_ORIGINALTITLE,
            MoviesContract.MoviesEntry.COLUMN_OLDDATA,
            MoviesContract.MoviesEntry.COLUMN_BOOKMARK
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_POSTERPATH = 1;
    static final int COL_MOVIE_ORIGINALTITLE = 2;

    private boolean mTwoPane;

    private RecyclerView recyclerView;

    private SimpleItemRecyclerViewAdapter simpleItemRecyclerViewAdapter;

    private String sortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getTitle());
        }

        recyclerView = (RecyclerView) findViewById(R.id.movie_list);
        assert recyclerView != null;
        final int columns = getResources().getInteger(R.integer.grid_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), columns));
        simpleItemRecyclerViewAdapter = new SimpleItemRecyclerViewAdapter(getApplicationContext(), null, mTwoPane);
        recyclerView.setAdapter(simpleItemRecyclerViewAdapter);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (Utility.isOnline(this)){
            ImdbSyncAdapter.initializeSyncAdapter(this);
        }else{
            CharSequence text = "No Internet Connection";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }

    }
/*
    private void updateMovies() {
        Context context = getApplicationContext();

        if (Utility.isOnline(context)){
            FetchIMDBTask imdbTask = new FetchIMDBTask();
            imdbTask.delegate = this;
            imdbTask.execute(sortOrder);
        }else{
            CharSequence text = "No Internet Connection";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    //this override the implemented method from asyncTask
    public void processFinish(List<Movie> result) {
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(getApplicationContext(), result, mTwoPane));
    }
*/

    @Override
    public void onStart() {
        super.onStart();
        if (sortOrder == null){
            sortOrder = MoviesContract.MoviesEntry.COLUMN_POPULARITY;
        }
        getSupportLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menuSortMostPopular) {
            sortMovies(MoviesContract.MoviesEntry.COLUMN_POPULARITY);
            return true;
        }else if (id == R.id.menuSortHighestRated) {
            sortMovies(MoviesContract.MoviesEntry.COLUMN_VOTEAVERAGE);
            return true;
        }else if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortMovies(String newSort){
        if (newSort != sortOrder){
            sortOrder = newSort;
            getSupportLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrderCursor = sortOrder + " DESC";

        Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
        String selection;
        String[] selectionArgs;

        switch (sortOrder) {
            default:
            case MoviesContract.MoviesEntry.COLUMN_POPULARITY: {
                selection = MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR + " = ?";
                selectionArgs = new String[] {"1"};
                break;
            }
            case MoviesContract.MoviesEntry.COLUMN_VOTEAVERAGE: {
                selection = MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED + " = ?";
                selectionArgs = new String[] {"1"};
                break;
            }
        }

        return new CursorLoader(this,
                uri,
                MOVIE_COLUMNS,
                selection,
                selectionArgs,
                sortOrderCursor);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        simpleItemRecyclerViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        simpleItemRecyclerViewAdapter.swapCursor(null);
    }

}
