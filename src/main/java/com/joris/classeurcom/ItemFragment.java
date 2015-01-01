package com.joris.classeurcom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * Fragment qui affiche les item d'une categorie
 */
public class ItemFragment extends GridFragment {

    public ItemFragment() {
    }

    private static ArrayList<Item> listTemp;

    private MainActivity mainContext;
    int idCategorie;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categorie, container, false);

        mainContext = (MainActivity) getActivity();

        Bundle bundle = this.getArguments();
        idCategorie = bundle.getInt("id", 0);
        listTemp = mainContext.listeCategorie.get(idCategorie).getListItem();

        GridView grid = (GridView) rootView.findViewById(R.id.grid_view_principale);
        adapter = new ItemAdapter(getActivity(), listTemp);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long arg3) {
                mainContext.addItemChoisi(listTemp.get(position));
                CategorieFragment fragment = new CategorieFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_item_detail_container, fragment)
                        .commit();
            }
        });

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mainContext);
                builder.setMessage(R.string.message_dialog_mod_item)
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog,
                                                        final int id) {
                                        Intent intent = new Intent(getActivity(), ModifyActivity.class);
                                        intent.putExtra("isCategorie", false);
                                        intent.putExtra("posCategorie", idCategorie);
                                        intent.putExtra("posItem", position);
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton(getString(R.string.no), null);
                builder.create().show();
                return true;
            }
        });

        return rootView;
    }
}