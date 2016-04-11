package com.ramesh.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by RAMESH on 07-04-2016.
 */
public class FavouritesProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mOpenHelper;

    static final int FAVOURITE_MOVIES = 100;
    static final int FAVOURITE_MOVIE_WITH_ID = 101;

    private static final String sFavouriteMovieWithIDSelection = MovieContract.FavouriteEntry.TABLE_NAME +
            "." + MovieContract.FavouriteEntry._ID  + "= ?";

    private static UriMatcher buildUriMatcher() {
        UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = MovieContract.CONTENT_AUTHORITY;

        sURIMatcher.addURI(authority, MovieContract.PATH_FAVOURITES, FAVOURITE_MOVIES);
        sURIMatcher.addURI(authority, MovieContract.PATH_FAVOURITES + "/#", FAVOURITE_MOVIE_WITH_ID);

        return sURIMatcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case FAVOURITE_MOVIES:
                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                retCursor = db.query(MovieContract.FavouriteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVOURITE_MOVIE_WITH_ID:
                SQLiteDatabase db1 = mOpenHelper.getReadableDatabase();
                long movieId = MovieContract.FavouriteEntry.getMovieIdFromUri(uri);
                retCursor = db1.query(MovieContract.FavouriteEntry.TABLE_NAME, projection, sFavouriteMovieWithIDSelection,
                        new String[]{Long.toString(movieId)}, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVOURITE_MOVIES:
                return MovieContract.FavouriteEntry.CONTENT_TYPE;
            case FAVOURITE_MOVIE_WITH_ID:
                return MovieContract.FavouriteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri retUri;

        switch (sUriMatcher.match(uri)) {
            case FAVOURITE_MOVIES:
                long _id = db.insert(MovieContract.FavouriteEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    retUri = MovieContract.FavouriteEntry.buildFavouriteMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case FAVOURITE_MOVIES:
                db.beginTransaction();
                int retCount = 0;
                try {

                    for (ContentValues value:values) {
                        long _id = db.insert(MovieContract.FavouriteEntry.TABLE_NAME, null, value);
                        if(_id != -1)
                            retCount++;
                    }
                    db.setTransactionSuccessful();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return retCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
