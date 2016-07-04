package technology.tasty.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Popular Movies
 * Created on 29/06/2016 by Espace de travail.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesContract.MoviesEntry.TABLE_NAME + " (" +
                MoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY," +
                MoviesContract.MoviesEntry.COLUMN_POSTERPATH + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RELEASEDATE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_ORIGINALTITLE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_VOTEAVERAGE + " REAL NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_OLDDATA + " INTEGER NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_STREAM_POPULAR + " INTEGER NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_STREAM_TOPRATED + " INTEGER NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_BOOKMARK + " INTEGER NOT NULL, " +

                // To assure the application have just the last update for a movie
                // it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + MoviesContract.MoviesEntry._ID + ") ON CONFLICT REPLACE " +

                " );";

        final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE " + MoviesContract.VideosEntry.TABLE_NAME + " (" +
                MoviesContract.VideosEntry._ID + " INTEGER PRIMARY KEY," +
                MoviesContract.VideosEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                MoviesContract.VideosEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MoviesContract.VideosEntry.COLUMN_SITE + " TEXT NOT NULL, " +
                MoviesContract.VideosEntry.COLUMN_SITE_KEY + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + MoviesContract.VideosEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME + " (" + MoviesContract.MoviesEntry._ID + "), " +

                // To assure the application have just the last update for a movie
                // it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + MoviesContract.VideosEntry._ID + ") ON CONFLICT REPLACE " +

                " );";

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MoviesContract.ReviewsEntry.TABLE_NAME + " (" +
                MoviesContract.ReviewsEntry._ID + " INTEGER PRIMARY KEY," +
                MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                MoviesContract.ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MoviesContract.ReviewsEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME + " (" + MoviesContract.MoviesEntry._ID + "), " +

                // To assure the application have just the last update for a movie
                // it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + MoviesContract.ReviewsEntry._ID + ") ON CONFLICT REPLACE " +

                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.VideosEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.ReviewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
