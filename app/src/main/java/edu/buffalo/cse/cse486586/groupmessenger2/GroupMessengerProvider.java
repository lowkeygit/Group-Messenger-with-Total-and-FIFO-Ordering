package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {


    //The Database reference needed for storage
    private SQLiteDatabase messengerDb;
    //A reference for Db Helper Class
    private GroupMessageDbHelper dbHelper;

    //Db Attributes
    private static final String DB_NAME = "messagesDb";
    private static final int DB_VERSION = 3;

    //Db Table and Column Name
    private String TABLE_NAME = "tblchatMessage";
    private static final String KEY_COL = "key";
    private static final String VAL_COL = "value";
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */

        //My Code Starts here---
        messengerDb = dbHelper.getWritableDatabase();
        Cursor cur = messengerDb.query(TABLE_NAME, new String[]{VAL_COL}, KEY_COL +"='" + values.get(KEY_COL) +"'", null, null, null, null);
        if(cur.getCount() > 0)
        {
            messengerDb.update(TABLE_NAME,values,"value = '"+values.get(VAL_COL)+"'",null);
        }
        else {
            long rowId = messengerDb.insert(TABLE_NAME, null, values);
            if (rowId > 0) {
                Uri CONTENT_URI = Uri.parse(uri.toString() + "/" + TABLE_NAME);
                Uri myUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(myUri, null);
                Log.v("insert", values.toString());
                return myUri;
            }
        }
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.

        //My code Starts here.
        dbHelper = new GroupMessageDbHelper(getContext(), DB_NAME, DB_VERSION, TABLE_NAME);

        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        //My Code starts here
        messengerDb = dbHelper.getReadableDatabase();

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(TABLE_NAME);

        Cursor resultCursor = qBuilder.query(messengerDb, projection, " key = \'" + selection + "\' ",
                selectionArgs, null, null, sortOrder);
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

        Log.v("query", selection);
        return resultCursor;
    }

    public class GroupMessageDbHelper extends SQLiteOpenHelper
    {
        private String CREATE_TABLE = ""; //= "tbl_chatMessage";

        public GroupMessageDbHelper(Context context, String dbName, int dbVersion, String tableName)
        {
            super(context, dbName, null, dbVersion);
            if(!"".equals(tableName))
            {
                CREATE_TABLE = "CREATE TABLE " +
                        tableName +  " ( " + KEY_COL + " TEXT PRIMARY KEY, " + VAL_COL + " TEXT )";
            }
            //messengerDb.execSQL(CREATE_TABLE);

        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
