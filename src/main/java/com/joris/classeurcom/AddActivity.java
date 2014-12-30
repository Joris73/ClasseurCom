package com.joris.classeurcom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AddActivity extends Activity {

    private static final int SELECT_PICTURE = 1;

    private EditText edit_nom;
    private String nom;
    private Spinner dropdownCat;
    private Spinner dropdownType;
    private boolean isCategorie = false;
    private ImageView imagePreview;
    private Bitmap bitmapSelected = null;

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

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recupererValeurs()) {
                    if (isCategorie) {
                        if (!MainActivity.isExist(nom, null)) {
                            String imageName = saveBitmapInSDCARD(nom);
                            MainActivity.addCategorie(db.createCategorie(nom, imageName));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.probleme_existe_deja), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Categorie categorie = MainActivity.listeCategorie.get(dropdownCat.getSelectedItemPosition());
                        if (!MainActivity.isExist(nom, categorie)) {
                            String imageName = saveBitmapInSDCARD(categorie.getNom() + "." + nom);
                            db.createItem(categorie, nom, imageName);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.probleme_existe_deja), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.probleme_champs), Toast.LENGTH_SHORT).show();
                }
            }
        });

        db.closeDB();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = "";
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    selectedImagePath = getRealPathFromURI(this, selectedImageUri);
                } else {
                    selectedImagePath = getRealPathAfterKitKat(selectedImageUri);
                }
                try {
                    File f = new File(selectedImagePath);
                    bitmapSelected = getBitmapFromUri(Uri.fromFile(f));
                    imagePreview.setImageBitmap(bitmapSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sauvegarde l'image importer après l'avoir redimentionné
     *
     * @param name
     *         le nom du fichier
     * @return Le nom du fichier enregistré
     */
    public String saveBitmapInSDCARD(String name) {
        String debName;
        if (isCategorie) {
            debName = "Categorie-";
        } else {
            debName = "Element-";
        }
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/ClasseurCom_images");
        myDir.mkdirs();
        String fname = debName + name.replaceAll(" ", "_") + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmapSelected = resizeImageForImageView(bitmapSelected, 500);
            bitmapSelected.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fname;
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
    public String getRealPathAfterKitKat(Uri contentUri) {// Will return "image:x*"
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

    /**
     * Récupérer le bitmap à partir d'une uri
     *
     * @param uri
     *         le path
     * @return le bitmap
     * @throws IOException
     */
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    /**
     * Redimentionne un bitmap en respectant le ratio
     *
     * @param bitmap
     *         le bitmap à redimentionner
     * @param size
     *         la taille en pixel du côté le plus grand désiré
     * @return le bitmap redimentionné
     */
    public Bitmap resizeImageForImageView(Bitmap bitmap, int size) {
        Bitmap resizedBitmap = null;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if (originalHeight > originalWidth) {
            newHeight = size;
            multFactor = (float) originalWidth / (float) originalHeight;
            newWidth = (int) (newHeight * multFactor);
        } else if (originalWidth > originalHeight) {
            newWidth = size;
            multFactor = (float) originalHeight / (float) originalWidth;
            newHeight = (int) (newWidth * multFactor);
        } else if (originalHeight == originalWidth) {
            newHeight = size;
            newWidth = size;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }

    /**
     * @return vrai si champs valide, faux sinon
     */
    private boolean recupererValeurs() {
        nom = edit_nom.getText().toString();

        if (nom.isEmpty() || bitmapSelected == null)
            return false;

        return true;
    }
}
