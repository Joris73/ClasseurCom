package com.joris.classeurcom;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {

    private Activity activity;
    private static LayoutInflater inflater = null;
    private ArrayList<Item> listeItem;

    public ItemAdapter(Activity a, ArrayList<Item> listeItem) {
        activity = a;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listeItem = listeItem;
    }

    public int getCount() {
        return listeItem.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.grid_adapter, null);

        Item item = listeItem.get(position);

        ImageView image = (ImageView) vi.findViewById(R.id.grid_image);
        TextView name = (TextView) vi.findViewById(R.id.grid_name);

        image.setImageResource(activity.getResources().getIdentifier(item.getImage(), "drawable", activity.getPackageName()));
        name.setText(item.getNom());

        return vi;
    }
}