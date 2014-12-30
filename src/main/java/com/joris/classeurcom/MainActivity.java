package com.joris.classeurcom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Activité principal qui gère toute l'application
 */
public class MainActivity extends Activity {

    private final static String FRAGMENT_TAG_LIST = "ItemListFragment_TAG";

    static public ArrayList<Categorie> listeCategorie = new ArrayList<>();
    static public final ArrayList<Item> listeEnCours = new ArrayList<>();
    static public GridFragment fragmentGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        listeCategorie.clear();
        listeCategorie = db.getAllCategorie();

        ItemListFragment fragment1 = new ItemListFragment();
        fragmentGrid = new CategorieFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_item_list, fragment1, FRAGMENT_TAG_LIST)
                .replace(R.id.frame_item_detail_container, fragmentGrid)
                .commit();

        db.closeDB();
    }

    /**
     * Ajoute une categorie dans la liste java et mets à jour l'affichage
     *
     * @param cat
     *         La categorie
     */
    public static void addCategorie(Categorie cat) {
        listeCategorie.add(cat);
        fragmentGrid.updateList();
    }

    /**
     * Va testé si une categorie ou un item d'une categorie existe déjà
     *
     * @param name
     *         le nom
     * @param categorie
     *         peut etre null
     * @return si existe ou pas
     */
    public static boolean isExist(String name, Categorie categorie) {
        if (categorie == null) {
            for (Categorie cat : listeCategorie) {
                if (cat.getNom().equals(name))
                    return true;
            }
        } else {
            for (Item item : categorie.getListItem()) {
                if (item.getNom().equals(name))
                    return true;
            }
        }
        return false;
    }

    /**
     * Modifie le comportement par defaut du bouton retour
     */
    @Override
    public void onBackPressed() {
        if (fragmentGrid.getClass() == ItemFragment.class) {
            fragmentGrid = new CategorieFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_item_detail_container, fragmentGrid)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Ajoute l'item choisi par l'utilisateur dans la liste
     *
     * @param item
     *         item choisi
     */
    public void addItemChoisi(Item item) {
        listeEnCours.add(item);
        ItemListFragment mitemListFragment = (ItemListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        mitemListFragment.updateList();
    }

    /**
     * Appeler lors d'un appuie sur un item de la phrase pour demander sa suppréssion ou pas
     *
     * @param pos
     *         position de l'item dans la liste
     */
    public void dellItemChoisi(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.message_dialog))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                listeEnCours.remove(pos);
                                ItemListFragment mitemListFragment = (ItemListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
                                mitemListFragment.updateList();
                            }
                        })
                .setNegativeButton(getString(R.string.no), null);
        builder.create().show();
    }

    /**
     * Methode qui export la BDD au format json dans un fichier
     */
    void exportBDD() {
        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".txt");
        fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            public void directorySelected(File directory) {
/*
                ProgressDialog progress = new ProgressDialog(getApplicationContext());
                progress.setMessage(getString(R.string.export_message));
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setIndeterminate(true);
                progress.setCancelable(false);
                progress.show();*/
                File file = new File(directory.toString() + "/BDDClasseurCom.txt");
                if (file.exists()) file.delete();

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file, true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                PrintStream ps = new PrintStream(fos);

                JSONObject jsonExport = new JSONObject();
                JSONArray catArray = new JSONArray();

                int jumpTime = 0;

                try {
                    for (Categorie cat : listeCategorie) {
                        JSONObject jsonCategorie = new JSONObject();
                        jsonCategorie.put("Name", cat.getNom());
                        jsonCategorie.put("Image", cat.getImage());

                        JSONArray itemArray = new JSONArray();
                        for (Item item : cat.getListItem()) {

                            JSONObject jsonItem = new JSONObject();
                            jsonItem.put("Name", item.getNom());
                            jsonItem.put("Image", item.getImage());
                            itemArray.put(jsonItem);
                        }

                        jsonCategorie.put("Items", itemArray);

                        catArray.put(jsonCategorie);
                        jumpTime = +listeCategorie.size() / 100;
                        //progress.setProgress(jumpTime);
                    }

                    jsonExport.put("ClasseurCom", catArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ps.append(jsonExport.toString());
                ps.close();
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //progress.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.export_done), Toast.LENGTH_SHORT).show();
            }
        });
        fileDialog.setSelectDirectoryOption(true);
        fileDialog.showDialog();
    }

    /**
     * Methode qui importe une BDD au format json
     */
    void importBDD() {


        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".txt");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
/*
                ProgressDialog progress = new ProgressDialog(getApplicationContext());
                progress.setMessage(getString(R.string.import_message));
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setIndeterminate(true);
                progress.setCancelable(false);
                progress.show();*/

                final DatabaseHelper db = new DatabaseHelper(getApplicationContext());

                //File file = new File(Environment.getExternalStorageDirectory() + "/BDDClasseurCom.txt");
                if (file.exists()) {
                    db.removeAll();
                    StringBuilder text = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //progress.setProgress(50);

                    try {
                        JSONObject jsonObj = new JSONObject(text.toString());
                        JSONArray arrayCat = jsonObj.getJSONArray("ClasseurCom");

                        for (int i = 0; i < arrayCat.length(); i++) {
                            String nomCat = arrayCat.getJSONObject(i).getString("Name");
                            String imageCat = arrayCat.getJSONObject(i).getString("Image");

                            Categorie categorie = db.createCategorie(nomCat, imageCat);

                            JSONArray arrayItem = arrayCat.getJSONObject(i).getJSONArray("Items");
                            for (int j = 0; j < arrayItem.length(); j++) {
                                String nomItem = arrayItem.getJSONObject(j).getString("Name");
                                String imageItem = arrayItem.getJSONObject(j).getString("Image");
                                db.createItem(categorie, nomItem, imageItem);
                            }
                            addCategorie(categorie);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //progress.dismiss();
                    Toast.makeText(getApplicationContext(), getString(R.string.import_done), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Importation", "Fichier n'existe pas");
                }

                db.closeDB();
            }
        });
        fileDialog.showDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                listeEnCours.clear();
                ItemListFragment mitemListFragment = (ItemListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
                mitemListFragment.updateList();
                return true;
            case R.id.action_add:
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_export:
                exportBDD();
                return true;
            case R.id.action_import:
                importBDD();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
