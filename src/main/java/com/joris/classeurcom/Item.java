package com.joris.classeurcom;

/**
 * Created by Joris on 25/11/2014.
 * Cool non ?
 */
public class Item {
    private String nom;
    private String image;

    public Item(String nom, String image) {
        this.nom = nom;
        this.image = image;
    }

    public String getNom() {
        return nom;
    }

    public String getImage() {
        return image;
    }
}
