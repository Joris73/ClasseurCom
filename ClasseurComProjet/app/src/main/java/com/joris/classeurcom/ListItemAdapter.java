package com.joris.classeurcom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Adaptateur de la phrase actuel
 */
class ListItemAdapter extends BaseAdapter {

    private final Activity activity;
    private static LayoutInflater inflater = null;
    private final ArrayList<Item> listeItem;

    public ListItemAdapter(Activity a, ArrayList<Item> listeItem) {
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
            vi = inflater.inflate(R.layout.listview_item, null);

        Item item = listeItem.get(position);

        ImageView image = (ImageView) vi.findViewById(R.id.list_image_item);
        TextView name = (TextView) vi.findViewById(R.id.list_name_item);

        image.setImageResource(activity.getResources().getIdentifier(item.getImage(), "drawable", activity.getPackageName()));
        name.setText(item.getNom());

        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File f = new File(root + "/ClasseurCom_images/" + item.getImage());
            Uri realUri = Uri.fromFile(f);
            image.setImageBitmap(getBitmapFromUri(realUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        name.setText(item.getNom());

        return vi;
    }

    /**
     * Methode pour récupérer une image au format bitmap à partir de son uri
     *
     * @param uri
     *         chemin de l'image
     * @return image bitmap
     * @throws IOException
     */
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                activity.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}