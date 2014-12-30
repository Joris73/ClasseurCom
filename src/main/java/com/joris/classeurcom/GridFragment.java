package com.joris.classeurcom;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Classe abstraite de Fragment qui nous sert dans le @MainActivity qui nous permet de mettre à jour
 * la liste du fragment actuel
 */
public abstract class GridFragment extends Fragment {

    protected BaseAdapter adapter;

    public GridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categorie, container, false);
    }

    /**
     * Mets à jour la liste
     */
    protected void updateList() {
        adapter.notifyDataSetChanged();
    }
}