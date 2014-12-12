package com.joris.classeurcom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Joris on 12/12/2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "classeurCom";

    // Table Names
    private static final String TABLE_CATEGORIE = "categorie";
    private static final String TABLE_ITEM = "item";
    private static final String TABLE_CATEGORIE_ITEM = "categorie_item";

    private static final String KEY_ID_CATEGORIE = "id_categorie";
    private static final String KEY_ID_ITEM = "id_item";

    private static final String KEY_NAME_CATEGORIE = "name_categorie";
    private static final String KEY_NAME_ITEM = "name_item";

    private static final String KEY_IMAGE_CATEGORIE = "image_categorie";
    private static final String KEY_IMAGE_ITEM = "image_item";

    private static final String CREATE_TABLE_CATEGORIE = "CREATE TABLE "
            + TABLE_CATEGORIE + "(" + KEY_ID_CATEGORIE + " INTEGER PRIMARY KEY," + KEY_NAME_CATEGORIE
            + " TEXT," + KEY_IMAGE_CATEGORIE + " INTEGER)";

    private static final String CREATE_TABLE_ITEM = "CREATE TABLE " +
            TABLE_ITEM + "(" + KEY_ID_ITEM + " INTEGER PRIMARY KEY," + KEY_NAME_ITEM
            + " TEXT," + KEY_IMAGE_ITEM + " INTEGER)";


    private static final String CREATE_TABLE_CATEGORIE_ITEM = "CREATE TABLE " +
            TABLE_CATEGORIE_ITEM + "(" + KEY_ID_CATEGORIE + " INTEGER," + KEY_ID_ITEM + " INTEGER,"
            + " FOREIGN KEY(" + KEY_ID_CATEGORIE + ") REFERENCES " + TABLE_CATEGORIE + "(" + KEY_ID_CATEGORIE + "),"
            + " FOREIGN KEY(" + KEY_ID_ITEM + ") REFERENCES " + TABLE_ITEM + "(" + KEY_ID_ITEM + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_CATEGORIE);
        db.execSQL(CREATE_TABLE_ITEM);
        db.execSQL(CREATE_TABLE_CATEGORIE_ITEM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIE_ITEM);

        // create new tables
        onCreate(db);
    }


    public Categorie createCategorie(String nom, String image) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME_CATEGORIE, nom);
        values.put(KEY_IMAGE_CATEGORIE, image);

        long id = db.insert(TABLE_CATEGORIE, null, values);

        return new Categorie(id, nom, image, null);
    }

    public Item createItem(Categorie categorie, String nom, String image) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME_ITEM, nom);
        values.put(KEY_IMAGE_ITEM, image);

        long id = db.insert(TABLE_ITEM, null, values);

        createCategorieItem(categorie.getId(), id);
        Item item = new Item(id, nom, image);
        categorie.addItem(item);

        return item;
    }

    public void createCategorieItem(long id_categorie, long id_item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_CATEGORIE, id_categorie);
        values.put(KEY_ID_ITEM, id_item);

        long id = db.insert(TABLE_CATEGORIE_ITEM, null, values);

    }


    public ArrayList<Categorie> getAllCategorie() {
        ArrayList<Categorie> listCategories = new ArrayList<>();

        String selectQuery = "SELECT  * "
                + "FROM " + TABLE_CATEGORIE;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Categorie categorie = new Categorie(c.getInt((c.getColumnIndex(KEY_ID_CATEGORIE))),
                            c.getString(c.getColumnIndex(KEY_NAME_CATEGORIE)), c.getString(c.getColumnIndex(KEY_IMAGE_CATEGORIE)), null);
                    getAllItemForCategorie(categorie);

                    listCategories.add(categorie);
                } while (c.moveToNext());
            }
        }
        c.close();
        return listCategories;
    }


    /**
     * Return true si ok.
     *
     * @param categorie
     * @return
     */
    public boolean getAllItemForCategorie(Categorie categorie) {
        ArrayList<Item> listItem = new ArrayList<>();

        String selectQuery = "SELECT  * "
                + "FROM " + TABLE_CATEGORIE_ITEM + " CI "
                + "JOIN " + TABLE_ITEM + " I ON I." + KEY_ID_ITEM + " = CI." + KEY_ID_ITEM
                + " WHERE " + KEY_ID_CATEGORIE + "=" + categorie.getId();

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Item item = new Item(c.getInt((c.getColumnIndex(KEY_ID_ITEM))),
                            c.getString(c.getColumnIndex(KEY_NAME_ITEM)), c.getString(c.getColumnIndex(KEY_IMAGE_ITEM)));

                    listItem.add(item);
                } while (c.moveToNext());
            }
            categorie.setListItem(listItem);
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
