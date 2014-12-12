package com.joris.classeurcom;

/**
 * Created by Joris on 25/11/2014.
 * Cool non ?
 */
public class Item {
    private long id;
    private String nom;
    private String image;

    public Item(long id, String nom, String image) {
        this.id = id;
        this.nom = nom;
        this.image = image;
    }

    public long getId() {
        return this.id;
    }

    public String getNom() {
        return nom;
    }

    public String getImage() {
        return image;
    }
}
