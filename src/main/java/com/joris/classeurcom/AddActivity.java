package com.joris.classeurcom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AddActivity extends Activity {

    private static final int SELECT_PICTURE = 1;

    private EditText edit_nom;
    private String nom;
    private String selectedImagePath = "";
    private Spinner dropdownCat;
    private Spinner dropdownType;
    private boolean isCategorie = false;
    private ImageView imagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        final DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        edit_nom = (EditText) findViewById(R.id.edit_nom);
        Button button_add = (Button) findViewById(R.id.bt_ajouter);
        Button bt_image = (Button) findViewById(R.id.bt_path_image);
        dropdownType = (Spinner) findViewById(R.id.spinner_type);
        dropdownCat = (Spinner) findViewById(R.id.spinner_cat);
        imagePreview = (ImageView) findViewById(R.id.imagePreview);

        if (MainActivity.listeCategorie.isEmpty()) {
            dropdownType.setSelection(1);
        }

        ArrayAdapter<String> adapter;
        List<String> list;
        list = new ArrayList<>();

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
                    if (!MainActivity.listeCategorie.isEmpty()) {
                        isCategorie = false;
                        dropdownCat.setVisibility(View.VISIBLE);
                    } else {
                        dropdownType.setSelection(1);
                    }
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
                        MainActivity.addCategorie(db.createCategorie(nom, selectedImagePath));
                    } else {
                        Categorie categorie = MainActivity.listeCategorie.get(dropdownCat.getSelectedItemPosition());
                        db.createItem(categorie, nom, selectedImagePath);
                    }

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.probleme_champs), Toast.LENGTH_SHORT).show();
                }
            }
        });

        bt_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });

        db.closeDB();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    selectedImagePath = getRealPathFromURI(this, selectedImageUri);
                } else {
                    selectedImagePath = getPath(selectedImageUri);
                }
                Log.wtf("tg", getPath(selectedImageUri));
                try {
                    imagePreview.setImageBitmap(getBitmapFromUri(selectedImageUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Pour récupérer le realPath avant kitkat
     *
     * @param context
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Pour récupérer le realPath à partir de kitkat
     *
     * @param contentUri
     * @return
     */
    @SuppressLint("NewApi")
    public String getPath(Uri contentUri) {// Will return "image:x*"
        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
                new String[]{id}, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();
        return filePath;
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    /**
     * @return vrai si champs valide, faux sinon
     */
    private boolean recupererValeurs() {
        nom = edit_nom.getText().toString();

        if (nom.isEmpty() || selectedImagePath.isEmpty())
            return false;

        return true;
    }
}
