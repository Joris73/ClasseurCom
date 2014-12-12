package com.joris.classeurcom;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class AddActivity extends Activity {

    private EditText edit_nom;
    private EditText edit_image;
    private String nom;
    private String image;
    private Spinner dropdownCat;
    private Spinner dropdownType;
    private boolean isCategorie = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        edit_nom = (EditText) findViewById(R.id.edit_nom);
        edit_image = (EditText) findViewById(R.id.edit_image);
        Button button_add = (Button) findViewById(R.id.bt_ajouter);
        dropdownType = (Spinner) findViewById(R.id.spinner_type);
        dropdownCat = (Spinner) findViewById(R.id.spinner_cat);

        ArrayAdapter<String> adapter;
        List<String> list;
        list = new ArrayList<String>();

        for (Categorie cat : MainActivity.listeCategorie) {
            list.add(cat.getNom());
        }

        adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownCat.setAdapter(adapter);

        dropdownType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    isCategorie = false;
                    dropdownCat.setVisibility(View.VISIBLE);
                } else {
                    isCategorie = true;
                    dropdownCat.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recupererValeurs()) {
                    if (isCategorie) {
                        MainActivity.listeCategorie.add(new Categorie(nom, image, null));
                    } else {
                        Categorie categorie = MainActivity.listeCategorie.get(dropdownCat.getSelectedItemPosition());
                        categorie.getListItem().add(new Item(nom, image));
                    }

                    finish();

                    String SQLrequest = "SELECT * "
                            + "FROM user U "
                            + "JOIN usager US ON U.id_usager=US.id_usager "
                            + "WHERE U.login_user = '" + nom + "' and pass_login_user = '" + image + "'";

                    //new RequestTaskConnexion().execute(SQLrequest);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.probleme_champs), Toast.LENGTH_SHORT).show();
                }
            }
        });

        setFocusChange();
    }

    /**
     * @return vrai si champs valide, faux sinon
     */
    private boolean recupererValeurs() {
        nom = edit_nom.getText().toString();
        image = edit_image.getText().toString();

        if (nom.isEmpty() || image.isEmpty())
            return false;

        return true;
    }

    /**
     * Va changer le text dans les edittext en fonction du focus
     */
    private void setFocusChange() {
        edit_nom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && TextUtils.isEmpty(edit_nom.getText().toString())) {
                    edit_nom.setText(getString(R.string.edit_name_item));
                } else if (hasFocus && edit_nom.getText().toString().equals(getString(R.string.edit_name_item))) {
                    edit_nom.setText("");
                }
            }
        });

        edit_image.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && TextUtils.isEmpty(edit_image.getText().toString())) {
                    edit_image.setText(getString(R.string.edit_image_item));
                } else if (hasFocus && edit_image.getText().toString().equals(getString(R.string.edit_image_item))) {
                    edit_image.setText("");
                }
            }
        });
    }
}
