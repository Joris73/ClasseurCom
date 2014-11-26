package com.joris.classeurcom;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class ItemFragment extends Fragment {
    private MainActivity context;

    public ItemFragment() {
    }

    @SuppressLint("ValidFragment")
    public ItemFragment(MainActivity context) {
        super();
        this.context = context;
    }

    static ArrayList<Item> listTemp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categorie, container, false);

        Bundle bundle = this.getArguments();
        int id = bundle.getInt("id", 0);
        listTemp = MainActivity.listeCategorie.get(id).getListItem();

        GridView grid = (GridView) rootView.findViewById(R.id.grid_view_principale);
        ItemAdapter adapter = new ItemAdapter(getActivity(), listTemp);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long arg3) {
                context.addItemChoisi(listTemp.get(position));
                CategorieFragment fragment = new CategorieFragment(context);
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_item_detail_container, fragment)
                        .commit();
            }
        });
        return rootView;
    }
}