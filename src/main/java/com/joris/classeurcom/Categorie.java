package com.joris.classeurcom;

import java.util.ArrayList;

/**
 * Classe Categorie
 * Created by Joris on 25/11/2014.
 */
public class Categorie {
    private final long id;
    private String nom;
    private String image;
    private ArrayList<Item> listItem;

    public Categorie(long id, String nom, String image) {
        this.id = id;
        this.nom = nom;
        this.image = image;
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
