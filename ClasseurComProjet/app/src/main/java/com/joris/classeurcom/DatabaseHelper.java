package com.joris.classeurcom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Base de données qui gère l'enregistrement des categories et des elements de l'application Created
 * by Joris on 12/12/2014.
 */
class DatabaseHelper extends SQLiteOpenHelper {

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

    /**
     * Créé une categorie et l'ajoute dans la base de donnée
     *
     * @param nom
     *         nom de la categorie
     * @param image
     *         image de la categorie
     * @return l'objet categorie
     */
    public Categorie createCategorie(String nom, String image) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME_CATEGORIE, nom);
        values.put(KEY_IMAGE_CATEGORIE, image);

        long id = db.insert(TABLE_CATEGORIE, null, values);

        return new Categorie(id, nom, image);
    }

    /**
     * Créé un item et l'ajoute à la base de donnée
     *
     * @param categorie
     *         la categorie de l'item
     * @param nom
     *         nom de l'item
     * @param image
     *         l'image de l'item
     * @return l'objet item
     */
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

    /**
     * Methode privée appelé par @createItem pour lier l'item à une categorie dans la base
     *
     * @param id_categorie
     *         l'id de l'a categorie
     * @param id_item
     *         l'id de l'item
     */
    private void createCategorieItem(long id_categorie, long id_item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_CATEGORIE, id_categorie);
        values.put(KEY_ID_ITEM, id_item);

        db.insert(TABLE_CATEGORIE_ITEM, null, values);
    }

    /**
     * Renvoie toutes les categories de la base de donnée
     *
     * @return liste de categorie
     */
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
                            c.getString(c.getColumnIndex(KEY_NAME_CATEGORIE)), c.getString(c.getColumnIndex(KEY_IMAGE_CATEGORIE)));
                    getAllItemForCategorie(categorie);

                    listCategories.add(categorie);
                } while (c.moveToNext());
            }
        }
        c.close();
        return listCategories;
    }


    /**
     * Va récupérer tous les item pour une categorie
     *
     * @param categorie
     *         categorie pour laquel on recherche les item
     * @return true si des items trouvé sinon false
     */
    private boolean getAllItemForCategorie(Categorie categorie) {
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
                    Item item = new Item(c.getLong((c.getColumnIndex(KEY_ID_ITEM))),
                            c.getString(c.getColumnIndex(KEY_NAME_ITEM)), c.getString(c.getColumnIndex(KEY_IMAGE_ITEM)));

                    listItem.add(item);
                } while (c.moveToNext());
            }
            categorie.setListItem(listItem);
            c.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Mets à jour dans la base une categorie
     *
     * @param categorie
     *         la categorie à mettre à jour
     * @return si ça marché ou pas
     */
    public boolean updateCategorie(Categorie categorie) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME_CATEGORIE, categorie.getNom());
        values.put(KEY_IMAGE_CATEGORIE, categorie.getImage());

        return db.update(TABLE_CATEGORIE, values, KEY_ID_CATEGORIE + "=" + categorie.getId(), null) > 0;
    }

    /**
     * Mets à jour dans la base un item
     *
     * @param item
     *         l'item à mettre à jour
     * @return si ça marché ou pas
     */
    public boolean updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME_ITEM, item.getNom());
        values.put(KEY_IMAGE_ITEM, item.getImage());

        return db.update(TABLE_ITEM, values, KEY_ID_ITEM + "=" + item.getId(), null) > 0;
    }


    public boolean deleteCategorie(Categorie categorie) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CATEGORIE, KEY_ID_CATEGORIE + "=" + categorie.getId(), null) > 0;
    }

    public boolean deleteItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIE_ITEM, KEY_ID_ITEM + "=" + item.getId(), null);
        return db.delete(TABLE_CATEGORIE_ITEM, KEY_ID_ITEM + "=" + item.getId(), null)
                - db.delete(TABLE_ITEM, KEY_ID_ITEM + "=" + item.getId(), null) == 0;
    }

    /**
     * Ferme la DB
     */
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * Efface toute la base de donnée
     */
    public void removeAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIE_ITEM, null, null);
        db.delete(TABLE_ITEM, null, null);
        db.delete(TABLE_CATEGORIE, null, null);
        closeDB();
    }
}
