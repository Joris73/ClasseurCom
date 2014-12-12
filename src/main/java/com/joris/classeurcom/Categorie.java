package com.joris.classeurcom;

import java.util.ArrayList;

/**
 * Created by Joris on 25/11/2014.
 * Cool non ?
 */
public class Categorie {
    private long id;
    private String nom;
    private String image;
    private ArrayList<Item> listItem;

    public Categorie(long id, String nom, String image, ArrayList<Item> listItem) {
        this.id = id;
        this.nom = nom;
        this.image = image;
        this.listItem = listItem;
    }

    public long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getImage() {
        return image;
    }

    public void addItem(Item item) {
        if (listItem == null)
            listItem = new ArrayList<>();
        listItem.add(item);
    }

    public ArrayList<Item> getListItem() {
        if (listItem == null)
            listItem = new ArrayList<>();
        return listItem;
    }

    public void setListItem(ArrayList<Item> list) {
        listItem = list;
    }
}
