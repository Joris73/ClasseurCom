package com.joris.classeurcom;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return rootView;
    }
}