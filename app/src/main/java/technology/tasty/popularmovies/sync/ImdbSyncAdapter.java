package technology.tasty.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import technology.tasty.popularmovies.BuildConfig;
import technology.tasty.popularmovies.MovieListActivity;
import technology.tasty.popularmovies.R;
import technology.tasty.popularmovies.data.MoviesContract;

/**
 * Popular Movies
 * Created on 29/06/2016 by Espace de travail.
 */

/*
http://api.themoviedb.org/3/movie/popular?api_key=dd1d62e43856414c6bf8e2181c50b6f6
http://api.themoviedb.org/3/movie/top_rated?api_key=dd1d62e43856414c6bf8e2181c50b6f6
http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
http://api.themoviedb.org/3/movie/315465/videos?api_key=dd1d62e43856414c6bf8e2181c50b6f6
http://api.themoviedb.org/3/movie/315465/reviews?api_key=dd1d62e43856414c6bf8e2181c50b6f6
 */

public class ImdbSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = ImdbSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the imdb database, in seconds.
    // 60 seconds (1 minute) * 60 * 24 = 1 day
    public static final int SYNC_INTERVAL = 60 * 60 * 24;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int IMDB_NOTIFICATION_ID = 3004;


    private static final String[] NOTIFY_MOVIES_PROJECTION = new String[] {
            MoviesContract.MoviesEntry.COLUMN_ORIGINALTITLE
    };

    // these indices must match the projection
    private static final int INDEX_ORIGINALTITLE = 0;

    public static final String SYNC_POPULAR = "popular";
    public static final String SYNC_TOPRATED = "top_rated";
    public static final String SYNC_MOVIE = "movie";
    public static final String SYNC_MOVIE_VIDEOS = "videos";
    public static final String SYNC_MOVIE_REVIEWS = "reviews";

    public ImdbSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        /*Mark all movies as old data*/
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_OLDDATA, 1);

        getContext().getContentResolver().update(
                MoviesContract.MoviesEntry.CONTENT_URI,
                movieValues,
                null,
                null);

        /*Sync popular and top_rated movies*/
        /*If contains Bookmark movies, data are replaced, and mark as not old data*/
        ImdbSync(SYNC_POPULAR, null, 1);
        ImdbSync(SYNC_TOPRATED, null, 1);

        /*Delete all reviews and videos*/
        getContext().getContentResolver().delete(
                MoviesContract.VideosEntry.CONTENT_URI,
                null,
                null);

        getContext().getContentResolver().delete(
                MoviesContract.ReviewsEntry.CONTENT_URI,
                null,
                null);

        /*Delete all movies without bookmark flag, and with olddata flag*/
        getContext().getContentResolver().delete(
                MoviesContract.MoviesEntry.CONTENT_URI,
                MoviesContract.MoviesEntry.COLUMN_BOOKMARK + " = ? AND " + MoviesContract.MoviesEntry.COLUMN_OLDDATA + " = ?",
                new String[] {"0", "1"});

        /*Notify user with actual popular movies*/
        notifyMovies();
    }

    public void ImdbSync(String mode, String id, Integer page){
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String imdbJsonStr;

        try {
            // Construct the URL for the IMDB query
            // Possible parameters are available at IMDB API page, at
            // http://docs.themoviedb.apiary.io/
            final String IMDB_SCHEME_URL = "http";
            final String IMDB_AUTHORITY_URL = "api.themoviedb.org";
            final String IMDB_PATH_VERSION_URL = "3";
            final String IMDB_PATH_MOVIE_URL = "movie";
            final String IMDB_PATH_POPULAR = "popular";
            final String IMDB_PATH_TOPRATED = "top_rated";
            final String IMDB_PATH_VIDEOS = "videos";
            final String IMDB_PATH_REVIEWS = "reviews";
            final String PAGE_PARAM = "page";
            final String APIKEY_PARAM = "api_key";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(IMDB_SCHEME_URL)
                    .authority(IMDB_AUTHORITY_URL)
                    .appendPath(IMDB_PATH_VERSION_URL)
                    .appendPath(IMDB_PATH_MOVIE_URL);

            switch (mode) {
                default:
                case SYNC_POPULAR: {
                    builder.appendPath(IMDB_PATH_POPULAR);
                    break;
                }
                case SYNC_TOPRATED: {
                    builder.appendPath(IMDB_PATH_TOPRATED);
                    break;
                }
                case SYNC_MOVIE: {
                    builder.appendPath(id);
                    break;
                }
                case SYNC_MOVIE_VIDEOS: {
                    builder.appendPath(id)
                            .appendPath(IMDB_PATH_VIDEOS);
                    break;
                }
                case SYNC_MOVIE_REVIEWS: {
                    builder.appendPath(id)
                            .appendPath(IMDB_PATH_REVIEWS);
                    break;
                }
            }
            builder.appendQueryParameter(PAGE_PARAM, String.valueOf(page));
            builder.appendQueryParameter(APIKEY_PARAM, BuildConfig.OPEN_IMDB_API_KEY);

            URL url = new URL(builder.build().toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return;
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
                return;
            }

            imdbJsonStr = buffer.toString();

            switch (mode) {
                default:
                case SYNC_POPULAR:
                case SYNC_TOPRATED: {
                    getMoviesDataFromJson(imdbJsonStr, mode);
                    break;
                }
                case SYNC_MOVIE: {
                    getMovieDataFromJson(imdbJsonStr);
                    break;
                }
                case SYNC_MOVIE_VIDEOS: {
                    getVideosMovieDataFromJson(imdbJsonStr, id);
                    break;
                }
                case SYNC_MOVIE_REVIEWS: {
                    getReviewsMovieDataFromJson(imdbJsonStr, id);
                    break;
                }
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
    }

    private void getMoviesDataFromJson(String imdbJsonStr, String mode)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String IMDB_LIST = "results";

        final String IMDB_MOVIE_ID = "id";
        final String IMDB_MOVIE_POSTERPATH = "poster_path";
        final String IMDB_MOVIE_OVERVIEW = "overview";
        final String IMDB_MOVIE_RELEASEDATE = "release_date";
        final String IMDB_MOVIE_ORIGINALTITLE = "original_title";
        final String IMDB_MOVIE_POPULARITY = "popularity";
        final String IMDB_MOVIE_VOTEAVERAGE = "vote_average";

        try {
            JSONObject imdbJson = new JSONObject(imdbJsonStr);
            JSONArray movieArray = imdbJson.getJSONArray(IMDB_LIST);

            Vector<ContentValues> cVVector = new Vector<>(movieArray.length());

            for(int i = 0; i < movieArray.length(); i++) {
                Long movieId;
                String posterPath;
                String overview;
                String sReleaseDate;
                Date releaseDate;
                String originalTitle;
                Double popularity;
                Double voteAverage;

                // Get the JSON object representing the movie
                JSONObject movieIMDB = movieArray.getJSONObject(i);
                movieId = movieIMDB.getLong(IMDB_MOVIE_ID);
                posterPath = movieIMDB.getString(IMDB_MOVIE_POSTERPATH);
                overview = movieIMDB.getString(IMDB_MOVIE_OVERVIEW);
                sReleaseDate = movieIMDB.getString(IMDB_MOVIE_RELEASEDATE);
                originalTitle = movieIMDB.getString(IMDB_MOVIE_ORIGINALTITLE);
                popularity = movieIMDB.getDouble(IMDB_MOVIE_POPULARITY);
                voteAverage = movieIMDB.getDouble(IMDB_MOVIE_VOTEAVERAGE);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    releaseDate = format.parse(sReleaseDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    releaseDate = new Date();
                }

                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MoviesEntry._ID, movieId);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTERPATH, posterPath);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASEDATE, String.valueOf(releaseDate));
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_ORIGINALTITLE, originalTitle);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTEAVERAGE, voteAverage);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_OLDDATA, 0);

                switch (mode) {
                    default:
                    case SYNC_POPULAR:
                    {
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR, 1);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED, 0);
                        break;
                    }
                    case SYNC_TOPRATED: {
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR, 0);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED, 1);
                        break;
                    }
                }
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_BOOKMARK, 0);

                cVVector.add(movieValues);
            }

            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getMovieDataFromJson(String imdbJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String IMDB_MOVIE_ID = "id";
        final String IMDB_MOVIE_POSTERPATH = "poster_path";
        final String IMDB_MOVIE_OVERVIEW = "overview";
        final String IMDB_MOVIE_RELEASEDATE = "release_date";
        final String IMDB_MOVIE_ORIGINALTITLE = "original_title";
        final String IMDB_MOVIE_VOTEAVERAGE = "vote_average";

        try {
            JSONObject movieIMDB = new JSONObject(imdbJsonStr);

            Long movieId;
            String posterPath;
            String overview;
            String sReleaseDate;
            Date releaseDate;
            String originalTitle;
            Double voteAverage;

            // Get the JSON object representing the movie
            movieId = movieIMDB.getLong(IMDB_MOVIE_ID);
            posterPath = movieIMDB.getString(IMDB_MOVIE_POSTERPATH);
            overview = movieIMDB.getString(IMDB_MOVIE_OVERVIEW);
            sReleaseDate = movieIMDB.getString(IMDB_MOVIE_RELEASEDATE);
            originalTitle = movieIMDB.getString(IMDB_MOVIE_ORIGINALTITLE);
            voteAverage = movieIMDB.getDouble(IMDB_MOVIE_VOTEAVERAGE);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                releaseDate = format.parse(sReleaseDate);
            } catch (ParseException e) {
                e.printStackTrace();
                releaseDate = new Date();
            }

            ContentValues movieValues = new ContentValues();

            movieValues.put(MoviesContract.MoviesEntry._ID, movieId);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTERPATH, posterPath);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASEDATE, String.valueOf(releaseDate));
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_ORIGINALTITLE, originalTitle);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTEAVERAGE, voteAverage);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_OLDDATA, 0);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR, 0);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED, 0);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_BOOKMARK, 0);

            getContext().getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, movieValues);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void getVideosMovieDataFromJson(String imdbJsonStr, String movieId)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String IMDB_LIST = "results";

        final String IMDB_VIDEO_ID = "id";
        final String IMDB_VIDEO_NAME = "name";
        final String IMDB_VIDEO_SITE = "site";
        final String IMDB_VIDEO_KEY = "key";

        try {
            JSONObject imdbJson = new JSONObject(imdbJsonStr);
            JSONArray videoArray = imdbJson.getJSONArray(IMDB_LIST);

            Vector<ContentValues> cVVector = new Vector<>(videoArray.length());

            for(int i = 0; i < videoArray.length(); i++) {
                Long videoId;
                String name;
                String site;
                String key;

                // Get the JSON object representing the movie
                JSONObject movieIMDB = videoArray.getJSONObject(i);
                videoId = movieIMDB.getLong(IMDB_VIDEO_ID);
                name = movieIMDB.getString(IMDB_VIDEO_NAME);
                site = movieIMDB.getString(IMDB_VIDEO_SITE);
                key = movieIMDB.getString(IMDB_VIDEO_KEY);

                ContentValues videoValues = new ContentValues();

                videoValues.put(MoviesContract.VideosEntry._ID, videoId);
                videoValues.put(MoviesContract.VideosEntry.COLUMN_MOVIE_KEY, movieId);
                videoValues.put(MoviesContract.VideosEntry.COLUMN_NAME, name);
                videoValues.put(MoviesContract.VideosEntry.COLUMN_SITE, site);
                videoValues.put(MoviesContract.VideosEntry.COLUMN_SITE_KEY, key);

                cVVector.add(videoValues);
            }

            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MoviesContract.VideosEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void getReviewsMovieDataFromJson(String imdbJsonStr, String movieId)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String IMDB_LIST = "results";

        final String IMDB_REVIEW_ID = "id";
        final String IMDB_REVIEW_AUTHOR = "author";
        final String IMDB_REVIEW_CONTENT = "content";

        try {
            JSONObject imdbJson = new JSONObject(imdbJsonStr);
            JSONArray reviewArray = imdbJson.getJSONArray(IMDB_LIST);

            Vector<ContentValues> cVVector = new Vector<>(reviewArray.length());

            for(int i = 0; i < reviewArray.length(); i++) {
                Long reviewId;
                String author;
                String content;

                // Get the JSON object representing the movie
                JSONObject movieIMDB = reviewArray.getJSONObject(i);
                reviewId = movieIMDB.getLong(IMDB_REVIEW_ID);
                author = movieIMDB.getString(IMDB_REVIEW_AUTHOR);
                content = movieIMDB.getString(IMDB_REVIEW_CONTENT);

                ContentValues reviewValues = new ContentValues();

                reviewValues.put(MoviesContract.ReviewsEntry._ID, reviewId);
                reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY, movieId);
                reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_AUTHOR, author);
                reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_CONTENT, content);
                cVVector.add(reviewValues);
            }

            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MoviesContract.ReviewsEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void notifyMovies() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with news movies

                Uri moviesUri = MoviesContract.MoviesEntry.CONTENT_URI;
                String selection = MoviesContract.MoviesEntry.COLUMN_OLDDATA + " = ?";
                String[] selectionArgs = new String[] {"0"};
                String sortOrder = MoviesContract.MoviesEntry.COLUMN_POPULARITY + " DESC";

                Cursor cursor = context.getContentResolver().query(moviesUri, NOTIFY_MOVIES_PROJECTION, selection, selectionArgs, sortOrder);

                if (cursor.moveToFirst()) {
                    String desc = cursor.getString(INDEX_ORIGINALTITLE);

                    int iconId = R.mipmap.ic_launcher;
                    Resources resources = context.getResources();
                    Bitmap largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification), desc);

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.colorAccent))
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MovieListActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(IMDB_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.apply();
                }
                cursor.close();

            }
        }
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(null,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ImdbSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
