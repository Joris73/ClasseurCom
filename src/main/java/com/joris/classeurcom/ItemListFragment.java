package com.joris.classeurcom;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ItemListFragment extends Fragment {
    private MainActivity context;

    public ItemListFragment() {
    }

    @SuppressLint("ValidFragment")
    public ItemListFragment(MainActivity context) {
        super();
        this.context = context;
    }

    private ListItemAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView list = (ListView) getActivity().findViewById(R.id.list_view_item);
        adapter = new ListItemAdapter(getActivity(), MainActivity.listeEnCours);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long arg3) {
                context.dellItemChoisi(position);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liste_item, container, false);
    }

    public void updateList() {
        adapter.notifyDataSetChanged();
    }
}
