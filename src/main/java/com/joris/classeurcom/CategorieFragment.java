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
import android.widget.Toast;

/**
 * Fragment pour géré l'affichage des categories
 */
public class CategorieFragment extends GridFragment {
    private MainActivity mainContext;

    public CategorieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categorie, container, false);

        mainContext = (MainActivity) getActivity();

        GridView grid = (GridView) rootView.findViewById(R.id.grid_view_principale);
        adapter = new CategorieAdapter(getActivity(), MainActivity.listeCategorie);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long arg3) {
                if (!mainContext.listeCategorie.get(position).getListItem().isEmpty()) {
                    ItemFragment fragment = new ItemFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", position);
                    fragment.setArguments(bundle);

                    mainContext.fragmentGrid = fragment;

                    getFragmentManager().beginTransaction()
                            .replace(R.id.frame_item_detail_container, fragment)
                            .commit();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.probleme_cat_vide), Toast.LENGTH_SHORT).show();
                }
            }
        });

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mainContext);
                builder.setMessage(R.string.message_dialog_mod_cat)
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog,
                                                        final int id) {
                                        Intent intent = new Intent(getActivity(), ModifyActivity.class);
                                        intent.putExtra("isCategorie", true);
                                        intent.putExtra("posCategorie", position);
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