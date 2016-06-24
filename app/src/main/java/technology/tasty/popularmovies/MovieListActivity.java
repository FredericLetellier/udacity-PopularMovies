package technology.tasty.popularmovies;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements AsyncResponse {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private RecyclerView recyclerView;

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
        setupRecyclerView(recyclerView);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

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

    @Override
    public void onStart() {
        super.onStart();
        if (sortOrder == null){
            sortOrder = "popular";
        }
        updateMovies();
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
            sortOrder = "popular";
            updateMovies();
            return true;
        }else if (id == R.id.menuSortHighestRated) {
            sortOrder = "top_rated";
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        final int columns = getResources().getInteger(R.integer.grid_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), columns));
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(getApplicationContext(), new ArrayList<Movie>(), mTwoPane));
    }

}
