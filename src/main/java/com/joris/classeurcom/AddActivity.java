package com.joris.classeurcom;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activité qui permet l'ajout de nouvelles élement dans l'application
 */
public class AddActivity extends Activity {

    static final int SELECT_PICTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    String mCurrentPhotoPath;

    private EditText edit_nom;
    private String nom;
    private Spinner dropdownCat;
    private Spinner dropdownType;
    private boolean isCategorie = false;
    private ImageView imagePreview;
    private Bitmap bitmapSelected = null;
    private TextView tv_cat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        final DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        tv_cat = (TextView) findViewById(R.id.tv_categorie);
        dropdownType = (Spinner) findViewById(R.id.spinner_type);
        dropdownCat = (Spinner) findViewById(R.id.spinner_cat);
        edit_nom = (EditText) findViewById(R.id.edit_nom);
        Button bt_path_image = (Button) findViewById(R.id.bt_path_image);
        Button bt_take_image = (Button) findViewById(R.id.bt_take_image);
        Button button_add = (Button) findViewById(R.id.bt_ajouter);
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

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownCat.setAdapter(adapter);

        dropdownType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    if (!MainActivity.listeCategorie.isEmpty()) {
                        isCategorie = false;
                        tv_cat.setVisibility(View.VISIBLE);
                        dropdownCat.setVisibility(View.VISIBLE);
                    } else {
                        dropdownType.setSelection(1);
                    }
                } else {
                    isCategorie = true;
                    tv_cat.setVisibility(View.GONE);
                    dropdownCat.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        bt_path_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.select_picture)), SELECT_PICTURE);
            }
        });

        bt_take_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
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

    /**
     * Récupère l'image selectionné par l'utilisateur
     * En cas d'annulation on supprime le fichier temp
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    selectedImagePath = getRealPathFromURI(selectedImageUri);
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
            } else if (requestCode == REQUEST_TAKE_PHOTO) {
                galleryAddPic();
                try {
                    File f = new File(mCurrentPhotoPath);
                    bitmapSelected = getBitmapFromUri(Uri.fromFile(f));
                    imagePreview.setImageBitmap(bitmapSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (resultCode == RESULT_CANCELED && requestCode == REQUEST_TAKE_PHOTO) {
            File f = new File(mCurrentPhotoPath);
            f.delete();
        }
    }

    /**
     * Sauvegarde l'image importer après l'avoir redimentionné
     *
     * @param name
     *         le nom du fichier
     * @return Le nom du fichier enregistré
     */
    String saveBitmapInSDCARD(String name) {
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

        // Permet de rendre visible les fichier depuis l'explorer windows
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);

        return fname;
    }

    /**
     * Pour récupérer le realPath avant kitkat
     *
     * @param contentUri
     *         uri
     * @return le chemin absolu
     */
    String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
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
     *         uri
     * @return le chemin absolu
     */
    @SuppressLint("NewApi")
    String getRealPathAfterKitKat(Uri contentUri) {// Will return "image:x*"
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
    Bitmap resizeImageForImageView(Bitmap bitmap, int size) {
        Bitmap resizedBitmap;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor;
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
     * Permet de lancer l'application photo
     */
    void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Créé la photo prise par l'appareil photo.
     *
     * @return le fichier créé
     * @throws IOException
     */
    File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "ClasseurCom_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Ajoute l'image prise par l'appareil photo à la galerie.
     */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * @return vrai si champs valide, faux sinon
     */
    private boolean recupererValeurs() {
        nom = edit_nom.getText().toString();

        return !(nom.isEmpty() || bitmapSelected == null);

    }
}
