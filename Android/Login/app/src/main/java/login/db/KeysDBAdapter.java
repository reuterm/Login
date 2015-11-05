package vs.in.de.uni_ulm.mreuter.login.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by mreuter on 13/02/15.
 */
public class KeysDBAdapter {

    // Column names of the used table.
    public static final String KEY_ID = "_id";
    public static final String KEY_USER = "user";
    public static final String KEY_SITE = "site";
    public static final String KEY_DATA_PUB = "keyDataPub";
    public static final String KEY_DATA_PRIV = "keyDataPriv";
    public static final String KEY_CREATED = "created";

    private static final String DATABASE_NAME = "Login";
    private static final String TABLE_NAME = "Keys";
    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " +
            "\t" +TABLE_NAME+ "(\n" +
            "\t" +KEY_ID+ " VARCHAR(100) PRIMARY KEY NOT NULL,\n" +
            "\t" +KEY_USER+ " VARCHAR(50),\n" +
            "\t" +KEY_SITE+ " VARCHAR(50),\n" +
            "\t" +KEY_DATA_PUB+ " BLOB NOT NULL,\n" +
            "\t" +KEY_DATA_PRIV+ " BLOB NOT NULL,\n" +
            "\t" +KEY_CREATED+ " DATETIME DEFAULT CURRENT_TIMESTAMP\n" +
            "\t);";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "KeysDBAdapter";
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;
    private final Context ctx;

    public KeysDBAdapter(Context ctx) {
        this.ctx = ctx;
    }

    // Establish connection with database.
    public KeysDBAdapter open() {
        dbHelper = new SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                Log.d(TAG, DATABASE_NAME + "created.");
                db.execSQL(DATABASE_CREATE);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                Log.d(TAG, "Upgrading database " +DATABASE_NAME+ " from version " +oldVersion+ " to " +
                        " version " +newVersion+".");
                onCreate(db);
            }
        };

        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    // Save new key pair with only basic information.
    public long saveKeyPairBasic(String jointHash, byte[] dataPub, byte[] dataPriv) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, jointHash);
        values.put(KEY_DATA_PUB, dataPub);
        values.put(KEY_DATA_PRIV, dataPriv);

        return db.insert(TABLE_NAME, null, values);
    }

    // Save new key pair with more detailed information.
    public long saveKeyPairDetailed(String jointHash, String user, String site, byte[] dataPub,
                                    byte[] dataPriv) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, jointHash);
        values.put(KEY_USER, user);
        values.put(KEY_SITE, site);
        values.put(KEY_DATA_PUB, dataPub);
        values.put(KEY_DATA_PRIV, dataPriv);

        return db.insert(TABLE_NAME, null, values);

    }

    // Load list of keys to be displayed.
    public Cursor fetchAllKeys () {
        Cursor cursor = db.query(TABLE_NAME, new String[] {KEY_ID, KEY_USER, KEY_SITE},
                null, null, null, null, KEY_CREATED, null);
        if (cursor != null) cursor.moveToFirst();

        return cursor;
    }

    // Update single key pair. Possible changes of the ID (joint hash) must be taken care of.
    public int updateKeyPair(String newJointHash, String oldJointHash, String user, String site,
                             byte[] dataPub, byte[] dataPriv) {
        ContentValues values = new ContentValues();

        // Save eventual changes of either username or website name, thus ID (i.e. joint hash)
        // as well.
        values.put(KEY_ID, newJointHash);
        values.put(KEY_USER, user);
        values.put(KEY_SITE, site);
        values.put(KEY_DATA_PUB, dataPub);
        values.put(KEY_DATA_PRIV, dataPriv);

        return db.update(TABLE_NAME, values, KEY_ID+"=?", new String[] {oldJointHash});
    }

    // Load complete key pair data in case it is needed.
    public Cursor fetchKeyPair (String jointHash) {

        Cursor cursor = db.query(TABLE_NAME, new String[] {KEY_ID, KEY_USER, KEY_SITE, KEY_DATA_PUB,
                KEY_DATA_PRIV, KEY_CREATED}, KEY_ID+"=?", new String[] {jointHash},null,null, null);
        if(cursor != null) cursor.moveToFirst();

        return cursor;
    }

    // Delete specific key pair.
    public int deleteKeyPair(String jointHash) {

        return db.delete(TABLE_NAME, KEY_ID+"=?", new String[] {jointHash});
    }

    // Clear database from all keys.
    public void deleteAllKeyPairs(){
        db.delete(TABLE_NAME, "1", null);
    }
}
