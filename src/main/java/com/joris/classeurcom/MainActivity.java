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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        listeCategorie.clear();
        listeCategorie = db.getAllCategorie();
/*
        ArrayList<Item> listcereales = new ArrayList<>();
        Categorie categorie1 = db.createCategorie("Cereales", "_cereales");
        listeCategorie.add(categorie1);
        db.createItem(categorie1, "Ble", "ble");*/

        /*
        listcereales.add(new Item("Ble", "ble"));
        listcereales.add(new Item("Lentilles", "lentilles"));
        listcereales.add(new Item("Mais", "mais"));
        listcereales.add(new Item("Pain", "pain"));
        listcereales.add(new Item("Pates", "pates"));
        listcereales.add(new Item("Pois chiche", "pois_chiche"));
        listcereales.add(new Item("Riz", "riz"));
        listcereales.add(new Item("Semoule", "semoule"));
        listcereales.add(new Item("Compote", "compote"));
        listcereales.add(new Item("Confiture", "confiture"));
        listcereales.add(new Item("Fromage blanc", "fromage_blanc"));
        listcereales.add(new Item("Yahourt", "yahourt"));

        ArrayList<Item> listlegumes = new ArrayList<Item>();
        listeCategorie.add(db.createCategorie("Legumes", "_legumes"));
        listlegumes.add(new Item("Ail", "ail"));
        listlegumes.add(new Item("Artichauts", "artichauts"));
        listlegumes.add(new Item("Asperge", "asperge"));
        listlegumes.add(new Item("Avocats", "avocats"));
        listlegumes.add(new Item("Betteraves", "betteraves"));
        listlegumes.add(new Item("Blette", "blette"));
        listlegumes.add(new Item("Brocoli", "brocoli"));
        listlegumes.add(new Item("Carrottes", "carrottes"));
        listlegumes.add(new Item("Celeri rave", "celeri_rave"));
        listlegumes.add(new Item("Chou", "chou"));
        listlegumes.add(new Item("Choux de bruxelles", "choux_de_bruxelles"));
        listlegumes.add(new Item("Chou fleur", "chou_fleur"));
        listlegumes.add(new Item("Chou rouge", "chou_rouge"));
        listlegumes.add(new Item("Cornichon", "cornichon"));
        listlegumes.add(new Item("Courge", "courge"));
        listlegumes.add(new Item("Courgettes", "courgettes"));
        listlegumes.add(new Item("Echalote", "echalote"));
        listlegumes.add(new Item("Endives", "endives"));
        listlegumes.add(new Item("Epinards", "epinards"));
        listlegumes.add(new Item("Fenouil", "fenouil"));
        listlegumes.add(new Item("Haricots", "haricots"));
        listlegumes.add(new Item("Oignon", "oignon"));
        listlegumes.add(new Item("Petit pois", "petit_pois"));
        listlegumes.add(new Item("Poireau", "poireau"));
        listlegumes.add(new Item("Pommes de terre", "pommes_de_terre"));
        listlegumes.add(new Item("Radis", "radis"));
        listlegumes.add(new Item("Salade", "salade"));
        listlegumes.add(new Item("Salsifis", "salsifis"));
        listlegumes.add(new Item("Tomates", "tomates"));

        ArrayList<Item> listplatsprepares = new ArrayList<>();
        listeCategorie.add(db.createCategorie("Plats prepares", "_plats_prepares"));
        listplatsprepares.add(new Item("Frites", "frites"));
        listplatsprepares.add(new Item("Pizza", "pizza"));
        listplatsprepares.add(new Item("Quiche", "quiche"));
        listplatsprepares.add(new Item("Soupe", "soupe"));

        ArrayList<Item> listproduitsdorigineanimale = new ArrayList<>();
        listeCategorie.add(db.createCategorie("Produits d'origine animale", "_produits_dorigine_animale"));
        listproduitsdorigineanimale.add(new Item("Charcuterie", "charcuterie"));
        listproduitsdorigineanimale.add(new Item("Fruits de mer", "fruits_de_mer"));
        listproduitsdorigineanimale.add(new Item("Lait", "lait"));
        listproduitsdorigineanimale.add(new Item("Miel", "miel"));
        listproduitsdorigineanimale.add(new Item("Oeufs", "oeufs"));
        listproduitsdorigineanimale.add(new Item("Poisson", "poisson"));
        listproduitsdorigineanimale.add(new Item("Viande", "viande"));

        */

        ItemListFragment fragment1 = new ItemListFragment(this);
        CategorieFragment fragment2 = new CategorieFragment(this);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_item_list, fragment1, FRAGMENT_TAG_LIST)
                .replace(R.id.frame_item_detail_container, fragment2)
                .commit();

        db.closeDB();
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
