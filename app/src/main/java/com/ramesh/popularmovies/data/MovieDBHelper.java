package com.ramesh.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.ramesh.popularmovies.data.MovieContract.FavouriteEntry;

/**
 * Created by RAMESH on 07-04-2016.
 */
public class MovieDBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "favouriteMovies.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FavouriteEntry.TABLE_NAME + "( " +
                FavouriteEntry._ID + " REAL PRIMARY KEY, " +
                FavouriteEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                FavouriteEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavouriteEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavouriteEntry.COLUMN_RATING + " REAL NOT NULL, " +
                FavouriteEntry.COLUMN_DURATION + " INTEGER NOT NULL, " +
                FavouriteEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
