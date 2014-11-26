package com.joris.classeurcom;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class CategorieFragment extends Fragment {
    private MainActivity context;

    public CategorieFragment() {
    }

    @SuppressLint("ValidFragment")
    public CategorieFragment(MainActivity context) {
        super();
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categorie, container, false);

        GridView grid = (GridView) rootView.findViewById(R.id.grid_view_principale);
        CategorieAdapter adapter = new CategorieAdapter(getActivity(), MainActivity.listeCategorie);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long arg3) {
                ItemFragment fragment = new ItemFragment(context);
                Bundle bundle = new Bundle();
                bundle.putInt("id", position);
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_item_detail_container, fragment)
                        .commit();
            }
        });
        return rootView;
    }
}