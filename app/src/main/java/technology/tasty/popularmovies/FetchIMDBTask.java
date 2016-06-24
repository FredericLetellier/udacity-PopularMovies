package technology.tasty.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Popular Movies
 * Created on 24/06/2016 by Espace de travail.
 */
public class FetchIMDBTask extends AsyncTask<String, Void, List<Movie>> {

    private final String LOG_TAG = FetchIMDBTask.class.getSimpleName();

    public AsyncResponse delegate = null;

    /**
     * Take the String representing the complete imdb movies in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private List<Movie> getIMDBDataFromJson(String imdbJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String IMDB_LIST = "results";

        final String IMDB_POSTERPATH = "poster_path";
        final String IMDB_OVERVIEW = "overview";
        final String IMDB_RELEASEDATE = "release_date";
        final String IMDB_ORIGINALTITLE = "original_title";
        final String IMDB_VOTEAVERAGE = "vote_average";

        JSONObject imdbJson = new JSONObject(imdbJsonStr);
        JSONArray movieArray = imdbJson.getJSONArray(IMDB_LIST);

        List<Movie> resultStrs = new ArrayList<>();
        for(int i = 0; i < movieArray.length(); i++) {
            String posterPath;
            String overview;
            String sReleaseDate;
            Date releaseDate;
            String originalTitle;
            Double voteAverage;

            // Get the JSON object representing the movie
            JSONObject movieIMDB = movieArray.getJSONObject(i);
            posterPath = movieIMDB.getString(IMDB_POSTERPATH);
            overview = movieIMDB.getString(IMDB_OVERVIEW);
            sReleaseDate = movieIMDB.getString(IMDB_RELEASEDATE);
            originalTitle = movieIMDB.getString(IMDB_ORIGINALTITLE);
            voteAverage = movieIMDB.getDouble(IMDB_VOTEAVERAGE);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                releaseDate = format.parse(sReleaseDate);
            } catch (ParseException e) {
                e.printStackTrace();
                releaseDate = new Date();
            }

            Movie movie = new Movie(posterPath, overview, releaseDate, originalTitle, voteAverage);
            resultStrs.add(movie);
        }
        return resultStrs;

    }
    @Override
    protected List<Movie> doInBackground(String... params) {

        String sortOrder;
        if (params.length == 0) {
            return null;
        } else {
            sortOrder = params[0];
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String imdbJsonStr = null;

        try {
            // Construct the URL for the IMDB query
            // Possible parameters are available at IMDB API page, at
            // http://docs.themoviedb.apiary.io/
            final String IMDB_SCHEME_URL = "http";
            final String IMDB_AUTHORITY_URL = "api.themoviedb.org";
            final String IMDB_PATH_VERSION_URL = "3";
            final String IMDB_PATH_MOVIE_URL = "movie";
            final String APIKEY_PARAM = "api_key";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(IMDB_SCHEME_URL)
                    .authority(IMDB_AUTHORITY_URL)
                    .appendPath(IMDB_PATH_VERSION_URL)
                    .appendPath(IMDB_PATH_MOVIE_URL)
                    .appendPath(sortOrder)
                    .appendQueryParameter(APIKEY_PARAM, BuildConfig.OPEN_IMDB_API_KEY);

            URL url = new URL(builder.build().toString());

            // Create the request to IMDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            imdbJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getIMDBDataFromJson(imdbJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the imdb.
        return null;
    }

    @Override
    protected void onPostExecute(List<Movie> result) {
        if (result != null) {
            delegate.processFinish(result);
            // New data is back from the server.  Hooray!
        }
    }
}

