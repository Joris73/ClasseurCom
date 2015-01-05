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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Activité qui permet la modification d'un element ou sa suppression
 */
public class ModifyActivity extends Activity {

    static final int SELECT_PICTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static String mCurrentPhotoPath;
    static Bitmap bitmapSelected;

    private EditText edit_nom;
    private String nomInit;
    private String nom;
    private ImageView imagePreview;
    private boolean isCategorie = true;
    private Categorie categorie;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);


        Intent intent = getIntent();
        if (intent != null) {
            final DatabaseHelper db = new DatabaseHelper(getApplicationContext());

            edit_nom = (EditText) findViewById(R.id.edit_nomcat_mod);
            Button bt_path_image = (Button) findViewById(R.id.bt_path_image_mod);
            Button bt_take_image = (Button) findViewById(R.id.bt_take_image_mod);
            Button bt_modifier = (Button) findViewById(R.id.bt_modifier);
            Button bt_supprimer = (Button) findViewById(R.id.bt_supprimer);
            imagePreview = (ImageView) findViewById(R.id.imagePreview_mod);

            categorie = MainActivity.listeCategorie.get(intent.getIntExtra("posCategorie", 0));

            if (!intent.getBooleanExtra("isCategorie", true)) {
                isCategorie = false;
                item = categorie.getListItem().get(intent.getIntExtra("posItem", 0));
            }

            if (isCategorie) {
                try {
                    nomInit = categorie.getNom();
                    edit_nom.setText(nomInit);
                    String root = Environment.getExternalStorageDirectory().toString();
                    File f = new File(root + "/ClasseurCom_images/" + categorie.getImage());
                    Uri realUri = Uri.fromFile(f);
                    bitmapSelected = getBitmapFromUri(realUri);
                    imagePreview.setImageBitmap(bitmapSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    nomInit = item.getNom();
                    edit_nom.setText(nomInit);
                    String root = Environment.getExternalStorageDirectory().toString();
                    File f = new File(root + "/ClasseurCom_images/" + item.getImage());
                    Uri realUri = Uri.fromFile(f);
                    bitmapSelected = getBitmapFromUri(realUri);
                    imagePreview.setImageBitmap(bitmapSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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

            bt_modifier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recupererValeurs()) {
                        if (isCategorie) {
                            if (nomInit.equals(nom) || !MainActivity.isExist(nom, null)) {
                                deleteImageInSdCard(categorie.getImage());
                                String imageName = saveBitmapInSDCARD(nom);
                                categorie.setNom(nom);
                                categorie.setImage(imageName);
                                db.updateCategorie(categorie);
                                MainActivity.fragmentGrid.updateList();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.probleme_existe_deja), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (nomInit.equals(nom) || !MainActivity.isExist(nom, categorie)) {
                                deleteImageInSdCard(item.getImage());
                                String imageName = saveBitmapInSDCARD(nom);
                                item.setNom(nom);
                                item.setImage(imageName);
                                db.updateItem(item);
                                MainActivity.fragmentGrid.updateList();
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

            bt_supprimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isCategorie) {
                        if (categorie.getListItem().isEmpty()) {
                            db.deleteCategorie(categorie);
                            deleteImageInSdCard(categorie.getImage());
                            MainActivity.removeCategorie(categorie);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.probleme_categorie_non_vide), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        db.deleteItem(item);
                        categorie.getListItem().remove(item);
                        deleteImageInSdCard(item.getImage());
                        MainActivity.fragmentGrid.updateList();
                        finish();
                    }
                }
            });

            db.closeDB();
        }
    }

    /**
     * Récupère l'image selectionné par l'utilisateur En cas d'annulation on supprime le fichier
     * temp
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
                    bitmapSelected = resizeImageForImageView(bitmapSelected, 750);
                    imagePreview.setImageBitmap(bitmapSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_TAKE_PHOTO) {
                galleryAddPic();
                try {
                    File f = new File(mCurrentPhotoPath);
                    bitmapSelected = getBitmapFromUri(Uri.fromFile(f));
                    bitmapSelected = resizeImageForImageView(bitmapSelected, 750);
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
     * Supprime l'image à partir de son nom sur la carte sd
     *
     * @param name
     *         nom de l'element
     */
    void deleteImageInSdCard(String name) {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/ClasseurCom_images/" + name);
        file.delete();
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
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
     * @throws java.io.IOException
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

    /**
     * Lors de la destruction de l'activité on supprime bien nos deux variables static
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            mCurrentPhotoPath = null;
            bitmapSelected = null;
        }
    }

    @Override
    public boolean isChangingConfigurations() {
        return super.isChangingConfigurations();
    }
}
