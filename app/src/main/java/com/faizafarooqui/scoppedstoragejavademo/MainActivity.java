package com.faizafarooqui.scoppedstoragejavademo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Activity activity;
    ImageView ivPicture;
    Button btnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        ivPicture= findViewById(R.id.ivPicture);
        btnCapture = findViewById(R.id.btnCapture);



        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("-- hello ");
                dispatchTakePictureIntent();
            }
        });

    }


    String imgFilePathTemp = "";
    Uri outputFileUri;
    int REQUEST_IMAGE_CAPTURE = 1232;

    private void dispatchTakePictureIntent() {

        long tim = System.currentTimeMillis();
        String imgName = "image_"+tim+".jpg";
        File dir = new File(getFilesDir()+"/images");
        if(!dir.exists()){
            boolean b =  dir.mkdir();
            System.out.println("-- dir created "+b);
        }
        File op = new File(dir, imgName);

        outputFileUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", op);
        imgFilePathTemp = op.getAbsolutePath();

        System.out.println("-- temp file path "+imgFilePathTemp);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        List<ResolveInfo> resolvedIntentActivities = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            grantUriPermission(packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }





    //coming back from camera app take picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ivPicture.setImageBitmap(BitmapFactory.decodeFile(imgFilePathTemp));


            saveImage(BitmapFactory.decodeFile(imgFilePathTemp));
        }
        super.onActivityResult(requestCode,  resultCode,  data);
    }

    private void saveImage(Bitmap bitmap) {
        OutputStream fos;
        long tim = System.currentTimeMillis();
        String imgName = "image_"+tim+".jpg";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imgName + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                System.out.println("-- imageUri  " + imageUri);

                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File image = new File(imagesDir, imgName + ".jpg");
                fos = new FileOutputStream(image);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Objects.requireNonNull(fos).close();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("---- image saved");
    }

}