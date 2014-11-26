package com.joris.classeurcom;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private final static String FRAGMENT_TAG_LIST = "ItemListFragment_TAG";

    static public ArrayList<Categorie> listeCategorie = new ArrayList<Categorie>();
    static public ArrayList<Item> listeEnCours = new ArrayList<Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        ArrayList<Item> listItem = new ArrayList<Item>();
        listItem.add(new Item("Artichauts", "artichauts"));
        listItem.add(new Item("Asperge", "asperge"));
        listItem.add(new Item("Avocats", "avocats"));
        listItem.add(new Item("Betteraves", "betteraves"));
        listItem.add(new Item("Carrottes", "carrottes"));
        listItem.add(new Item("Celeri rave", "celeri_rave"));
        listItem.add(new Item("Chou rouge", "chou_rouge"));
        listItem.add(new Item("Courgettes", "courgettes"));
        listItem.add(new Item("Endives", "endives"));
        listItem.add(new Item("Maïs", "mais"));
        listItem.add(new Item("Radis", "radis"));
        listItem.add(new Item("Salade", "salade"));
        listItem.add(new Item("Tomates", "tomates"));

        listeCategorie.clear();
        listeCategorie.add(new Categorie("Legumes", "legumes", listItem));
        listeCategorie.add(new Categorie("Transport", "transport", listItem));

        ItemListFragment fragment1 = new ItemListFragment(this);
        CategorieFragment fragment2 = new CategorieFragment(this);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_item_list, fragment1, FRAGMENT_TAG_LIST)
                .replace(R.id.frame_item_detail_container, fragment2)
                .commit();
    }

    @Override
    public void onBackPressed() {
        CategorieFragment fragment = new CategorieFragment(this);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_item_detail_container, fragment)
                .commit();
    }

    public void addItemChoisi(Item item) {
        listeEnCours.add(item);
        ItemListFragment mitemListFragment = (ItemListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        mitemListFragment.updateList();
    }

    public void dellItemChoisi(int pos) {
        listeEnCours.remove(pos);
        ItemListFragment mitemListFragment = (ItemListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        mitemListFragment.updateList();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
