package com.joris.classeurcom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private final static String FRAGMENT_TAG_LIST = "ItemListFragment_TAG";

    static public ArrayList<Categorie> listeCategorie = new ArrayList<>();
    static public ArrayList<Item> listeEnCours = new ArrayList<>();
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

    public static void addCategorie(Categorie cat) {
        listeCategorie.add(cat);
        fragmentGrid.updateList();
    }

    /**
     * Va testé si une categorie ou un item d'une categorie existe déjà
     * @param name le nom
     * @param categorie peut etre null
     * @return si existe ou pas
     */
    public static boolean isExist(String name, Categorie categorie) {
        if (categorie == null){
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

    @Override
    public void onBackPressed() {
        fragmentGrid = new CategorieFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_item_detail_container, fragmentGrid)
                .commit();
    }

    public void addItemChoisi(Item item) {
        listeEnCours.add(item);
        ItemListFragment mitemListFragment = (ItemListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        mitemListFragment.updateList();
    }

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
