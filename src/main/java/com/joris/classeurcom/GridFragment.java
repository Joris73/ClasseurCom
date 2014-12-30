package com.joris.classeurcom;

import android.app.Fragment;
import android.widget.BaseAdapter;

/**
 * Classe abstraite de Fragment qui nous sert dans le @MainActivity qui nous permet de mettre à jour
 * la liste du fragment actuel
 */
public abstract class GridFragment extends Fragment {

    BaseAdapter adapter;

    /**
     * Mets à jour la liste
     */
    void updateList() {
        adapter.notifyDataSetChanged();
    }
}