package technology.tasty.popularmovies;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_MOVIE = "movie";

    /**
     * The dummy content this fragment is presenting.
     */
    private Movie mMovie;

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
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mMovie.getOriginalTitle());
            Picasso.with(getContext())
                    .load("http://image.tmdb.org/t/p/w342/" + mMovie.getPosterPath())
                    .fit().centerCrop()
                    .into((ImageView) activity.findViewById(R.id.background_toolbar));
        }

        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mMovie != null) {
            SimpleDateFormat simpleDate =  new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
            ((TextView) rootView.findViewById(R.id.movie_date_duration)).setText(simpleDate.format(mMovie.getReleaseDate()));
            String voteaverage = String.valueOf(mMovie.getVoteAverage())+"/10";
            ((TextView) rootView.findViewById(R.id.movie_voteaverage)).setText(voteaverage);
            ((TextView) rootView.findViewById(R.id.movie_detail)).setText(mMovie.getOverview());
        }

        return rootView;
    }
}
