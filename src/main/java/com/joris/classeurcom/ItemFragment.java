package com.joris.classeurcom;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categorie, container, false);

        mainContext = (MainActivity) getActivity();

        Bundle bundle = this.getArguments();
        int id = bundle.getInt("id", 0);
        listTemp = mainContext.listeCategorie.get(id).getListItem();

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
        return rootView;
    }
}