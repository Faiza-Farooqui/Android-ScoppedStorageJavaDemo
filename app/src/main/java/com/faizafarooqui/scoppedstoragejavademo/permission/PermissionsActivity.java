package com.faizafarooqui.scoppedstoragejavademo.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.faizafarooqui.scoppedstoragejavademo.R;

public class PermissionsActivity extends AppCompatActivity {

    Activity activity;
    SharedPreferences prefs;
    private static final int PERMISSION_REQUEST_CODE = 37110;
    private PermissionsChecker checker;

    Button btnTurnOn;

    @Override
    protected void onResume() {
        super.onResume();
        if (checker.lacksPermissions(PermissionsChecker.PERMISSIONS)) {
            //requestPermissions(PermissionsChecker.PERMISSIONS);
        } else {
            allPermissionsGranted();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);

        activity = this;
        checker = new PermissionsChecker(this);
        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        btnTurnOn = findViewById(R.id.btnTurnOn);
        btnTurnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });

    }

    private void checkPermissions() {
        if (checker.lacksPermissions(PermissionsChecker.PERMISSIONS)) {
            requestPermissions(PermissionsChecker.PERMISSIONS);
        } else {
            allPermissionsGranted();
        }
    }



    private void requestPermissions(String... permissions) {

        if(prefs.getBoolean("isFirstTimeRequest",true)){
            prefs.edit().putBoolean("isFirstTimeRequest",false).commit();
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }else {
            boolean shouldShowReqRationale = true;
            for (int i = 0; i < permissions.length; i++) {

                System.out.println("permission: "+permissions[i]+"  shouldshowrationale: "+ ActivityCompat.shouldShowRequestPermissionRationale(activity,permissions[i]));
                if(checker.lacksPermission(permissions[i])){
                    if(! ActivityCompat.shouldShowRequestPermissionRationale(activity,permissions[i])){
                        shouldShowReqRationale = false;
                    }
                }
            }
            if(shouldShowReqRationale){
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }else {
                openAppSettings();
            }
        }
    }

    private void allPermissionsGranted() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            //requiresCheck = true;
            allPermissionsGranted();
        } else {
            //requiresCheck = false;
            showDeniedResponse(grantResults);
            //finish();
        }
    }

    private void showDeniedResponse(int[] grantResults) {

        boolean shouldShowDialog = false;
        String msg = "Permission not granted for: ";

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(activity, "Permission not granted for: "+permissionFeatures.values()[i], Toast.LENGTH_SHORT).show();
                msg = msg + "\n* "+PermissionsChecker.permissionFeatures.values()[i];
                shouldShowDialog = true;
            }
        }


        if(shouldShowDialog){
            new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    //.setTitle("Permission ")
                    .setMessage(msg)
                    .setNegativeButton("Allow Permissions", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //openAppSettings();
                            checkPermissions();
                        }
                    })
                    .setPositiveButton("Not Now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .create().show();
        }
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    private void openAppSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}