package com.joris.classeurcom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

    static public ArrayList<Categorie> listeCategorie = new ArrayList<>();
    static final ArrayList<Item> listeEnCours = new ArrayList<>();
    static GridFragment fragmentGrid;
    static ItemListFragment mitemFragment;

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        listeCategorie.clear();
        listeCategorie = db.getAllCategorie();

        mitemFragment = new ItemListFragment();
        if (fragmentGrid == null)
            fragmentGrid = new CategorieFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_item_list, mitemFragment)
                .replace(R.id.frame_item_detail_container, fragmentGrid)
                .commit();

        db.closeDB();
    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentGrid.updateList();
        mitemFragment.updateList();
    }

    /**
     * Ajoute une categorie dans la liste java et mets à jour l'affichage
     *
     * @param cat La categorie
     */
    public static void addCategorie(Categorie cat) {
        listeCategorie.add(cat);
        fragmentGrid.updateList();
    }

    public static void removeCategorie(Categorie cat) {
        listeCategorie.remove(cat);
        fragmentGrid.updateList();
    }

    /**
     * Va testé si une categorie ou un item d'une categorie existe déjà
     *
     * @param name      le nom
     * @param categorie peut etre null
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
     * @param item item choisi
     */
    public void addItemChoisi(Item item) {
        listeEnCours.add(item);
        mitemFragment.updateList();
    }

    /**
     * Appeler lors d'un appuie sur un item de la phrase pour demander sa suppréssion ou pas
     *
     * @param pos position de l'item dans la liste
     */
    public void dellItemChoisi(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.message_dialog_del_item))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                listeEnCours.remove(pos);
                                mitemFragment.updateList();
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

                // Permet de rendre visible les fichier depuis l'explorer windows
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                sendBroadcast(intent);

                messageInformation(R.string.export_done_title, R.string.export_done_message, android.R.drawable.ic_dialog_info);
            }
        });
        fileDialog.setSelectDirectoryOption(true);
        fileDialog.showDialog();
    }

    /**
     * Importation d'une base de donnée à partir du fichier json passé en paramètre Methode qui doit
     * si possible est appeler dans un thread
     *
     * @param file
     */
    void importBDD(File file) {

        final DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        if (file.exists()) {
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

            try {
                JSONObject jsonObj = new JSONObject(text.toString());
                JSONArray arrayCat = jsonObj.getJSONArray("ClasseurCom");

                final int sizeJson = arrayCat.length();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress = createdProgressDialog(R.string.title_progress_import, R.string.message_progress_import, sizeJson);
                    }
                });
                listeCategorie.clear();
                listeEnCours.clear();
                db.removeAll();

                for (int i = 0; i < sizeJson; i++) {
                    String nomCat = arrayCat.getJSONObject(i).getString("Name");
                    String imageCat = arrayCat.getJSONObject(i).getString("Image");

                    final Categorie categorie = db.createCategorie(nomCat, imageCat);

                    JSONArray arrayItem = arrayCat.getJSONObject(i).getJSONArray("Items");
                    for (int j = 0; j < arrayItem.length(); j++) {
                        String nomItem = arrayItem.getJSONObject(j).getString("Name");
                        String imageItem = arrayItem.getJSONObject(j).getString("Image");
                        db.createItem(categorie, nomItem, imageItem);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addCategorie(categorie);
                        }
                    });
                    progress.incrementProgressBy(1);
                }
                progress.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageInformation(R.string.import_done_title, R.string.import_done_message, android.R.drawable.ic_dialog_info);
                    }
                });
            } catch (JSONException e) {
                Log.e("Importation", "Mauvais fichier !!!!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageInformation(R.string.import_fail_title, R.string.import_fail_message, android.R.drawable.ic_dialog_alert);
                    }
                });
                e.printStackTrace();
            }
        } else {
            Log.e("Importation", "Fichier n'existe pas");
        }

        db.closeDB();
    }

    /**
     * Methode qui demande à l'utilisateur de choisir le fichier json
     */
    void importChoiseBDD() {
        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".txt");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(final File file) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        importBDD(file);
                    }
                }).start();
            }
        });
        fileDialog.showDialog();
    }

    /**
     * Methode pour afficher des messages d'informations
     *
     * @param title   id du string titre
     * @param message id du string message
     * @param icon    id de l'icon
     */
    void messageInformation(int title, int message, int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setNeutralButton(R.string.ok, null)
                .setIcon(icon)
                .setCancelable(true);
        builder.create().show();
    }

    /**
     * Methode qui génère une progress dialog
     *
     * @param title   Le titre
     * @param message Le message
     * @param max     La valeur max
     * @return une ProgressDialog
     */
    ProgressDialog createdProgressDialog(int title, int message, int max) {
        ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);

        mProgressDialog.setTitle(getString(title));
        mProgressDialog.setMessage(getString(message));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgress(0);
        mProgressDialog.setMax(max);
        mProgressDialog.show();

        return mProgressDialog;
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
                mitemFragment.updateList();
                return true;
            case R.id.action_add:
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_export:
                exportBDD();
                return true;
            case R.id.action_import:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.message_dialog_import))
                        .setCancelable(true)
                        .setTitle(R.string.message_dialog_import_title)
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog,
                                                        final int id) {
                                        importChoiseBDD();
                                    }
                                })
                        .setNegativeButton(getString(R.string.no), null)
                        .setIcon(android.R.drawable.ic_dialog_alert);
                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
