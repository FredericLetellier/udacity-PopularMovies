package technology.tasty.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Popular Movies
 * Created on 29/06/2016 by Espace de travail.
 */
public class MoviesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int VIDEOS = 200;
    static final int REVIEWS = 300;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_VIDEOS, VIDEOS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case VIDEOS:
                return MoviesContract.VideosEntry.CONTENT_TYPE;
            case REVIEWS:
                return MoviesContract.ReviewsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case VIDEOS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.VideosEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case REVIEWS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {

                String movieId = String.valueOf(values.get(MoviesContract.MoviesEntry._ID));

                String[] projection = new String[]{
                        MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
                        MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_OLDDATA,
                        MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR,
                        MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED,
                        MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_BOOKMARK};
                String selection = MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID + " = ? ";
                String[] selectionArgs = new String[]{movieId};

                Cursor cursor = query(
                        uri,
                        null,
                        selection,
                        selectionArgs,
                        null);

                long _id;
                if (cursor.moveToFirst()) {

                    if (cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OLDDATA)).equals("0")){
                        if (values.get(MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR).equals("1")){
                            values.remove(MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED);
                        } else {
                            values.remove(MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR);
                        }
                    }

                    values.remove(MoviesContract.MoviesEntry._ID);
                    values.put(MoviesContract.MoviesEntry.COLUMN_BOOKMARK, cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_BOOKMARK)));
                    _id = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, MoviesContract.MoviesEntry._ID + " = ?", new String[]{movieId});

                } else {
                    _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                }

                if ( _id > 0 )
                    returnUri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                cursor.close();
                break;
            }
            case VIDEOS: {
                long _id = db.insert(MoviesContract.VideosEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.VideosEntry.buildVideosUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.ReviewsEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDEOS:
                rowsDeleted = db.delete(
                        MoviesContract.VideosEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted = db.delete(
                        MoviesContract.ReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case VIDEOS:
                rowsUpdated = db.update(MoviesContract.VideosEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(MoviesContract.ReviewsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        String movieId = String.valueOf(value.get(MoviesContract.MoviesEntry._ID));

                        String[] projection = new String[]{
                                MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
                                MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_OLDDATA,
                                MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR,
                                MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED,
                                MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_BOOKMARK};
                        String selection = MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID + " = ? ";
                        String[] selectionArgs = new String[]{movieId};

                        Cursor cursor = query(
                                uri,
                                null,
                                selection,
                                selectionArgs,
                                null);

                        long _id;
                        if (cursor.moveToFirst()) {

                            if (cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OLDDATA)).equals("0")){
                                if (value.get(MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR).equals("1")){
                                    value.remove(MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED);
                                } else {
                                    value.remove(MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR);
                                }
                            }
                            //To keep the bookmark
                            value.remove(MoviesContract.MoviesEntry.COLUMN_BOOKMARK);
                            _id = db.update(MoviesContract.MoviesEntry.TABLE_NAME, value, MoviesContract.MoviesEntry._ID + " = ?", new String[]{movieId});


                            value.remove(MoviesContract.MoviesEntry._ID);
                            value.put(MoviesContract.MoviesEntry.COLUMN_BOOKMARK, cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_BOOKMARK));
                            _id = db.update(MoviesContract.MoviesEntry.TABLE_NAME, value, MoviesContract.MoviesEntry._ID + " = ?", new String[]{movieId});

                        } else {
                            _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        }

                        if (_id != -1) {
                            returnCount++;
                        } else {
                            throw new android.database.SQLException("Failed to insert row into " + uri);
                        }
                        cursor.close();
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case VIDEOS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.VideosEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case REVIEWS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
