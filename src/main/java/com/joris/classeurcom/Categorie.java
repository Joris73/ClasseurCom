package com.joris.classeurcom;

import java.util.ArrayList;

/**
 * Created by Joris on 25/11/2014.
 * Cool non ?
 */
public class Categorie {
    private String nom;
    private String image;
    private ArrayList<Item> listItem;

    public Categorie(String nom, String image, ArrayList<Item> listItem) {
        this.nom = nom;
        this.image = image;
        this.listItem = listItem;
    }

    public String getNom() {
        return nom;
    }

    public String getImage() {
        return image;
    }

    public ArrayList<Item> getListItem() {
        return listItem;
    }
}
