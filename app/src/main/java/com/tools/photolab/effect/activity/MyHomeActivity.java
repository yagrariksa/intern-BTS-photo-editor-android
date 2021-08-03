package com.tools.photolab.effect.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.tools.photolab.BuildConfig;
import com.tools.photolab.R;
import com.tools.photolab.effect.crop_img.newCrop.StoreManager;
import com.tools.photolab.effect.support.Constants;
import com.tools.photolab.effect.support.MyExceptionHandlerPix;
import com.tools.photolab.effect.support.SupportedClass;

import java.io.File;
import java.io.IOException;

import static com.tools.photolab.effect.color_splash_tool.ColorSplashActivity.REQUEST_CODE_CAMERA;

public class MyHomeActivity extends BaseActivity {
    protected static final int MY_PERMISSIONS_REQUEST_CAMERA_STORAGE = 0x1;
    protected static final int REQUEST_CODE_GALLERY = 0x3;
    protected static final int REQUEST_CODE_CROPPING = 0x4;
    public static int screenHeight;
    public static int screenWidth;
    public Uri mSelectedImageUri;
    public String mSelectedImagePath;
    public String mSelectedOutputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_my_home);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandlerPix(MyHomeActivity.this));

        RelativeLayout mAdView = findViewById(R.id.adView);
        loadBannerAds(mAdView);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels - Constants.dpToPx(this, 4);
        screenHeight = displayMetrics.heightPixels - Constants.dpToPx(this, 109);

        mPickGallery = findViewById(R.id.btn_pick_gallery);
        mPickCamera = findViewById(R.id.btn_pick_camera);

        mPickGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestCameraStoragePermission())
                    pixDialog(false);
            }
        });
        mPickCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestCameraStoragePermission())
                    pixDialog(true);
            }
        });
    }


    LinearLayout mPickGallery;
    LinearLayout mPickCamera;

    public boolean requestCameraStoragePermission() {
        //checking permission
        if (ActivityCompat.checkSelfPermission(MyHomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MyHomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // get both camera and storage permission
            ActivityCompat.requestPermissions(MyHomeActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA_STORAGE);
            return false;

        } else if (ActivityCompat.checkSelfPermission(MyHomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // get only camera permission
            ActivityCompat.requestPermissions(MyHomeActivity.this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA_STORAGE);
            return false;

        } else if (ActivityCompat.checkSelfPermission(MyHomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // get only storage permission
            ActivityCompat.requestPermissions(MyHomeActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA_STORAGE);
            return false;
        }
        return true;
    }

    public void pixDialog(Boolean camera) {
        if (camera) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", createImageFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (intent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.txt_select_picture)), REQUEST_CODE_GALLERY);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA) {
            mSelectedImagePath = mSelectedOutputPath;
            if (SupportedClass.stringIsNotEmpty(mSelectedImagePath)) {
                File fileImageClick = new File(mSelectedImagePath);
                if (fileImageClick.exists()) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        mSelectedImageUri = Uri.fromFile(fileImageClick);
                    } else {
                        mSelectedImageUri = FileProvider.getUriForFile(MyHomeActivity.this, BuildConfig.APPLICATION_ID + ".provider", fileImageClick);
                    }
                    startEditorActivity();
                }
            }
        } else if (data != null && data.getData() != null) {
            if (requestCode == REQUEST_CODE_GALLERY) {
                mSelectedImageUri = data.getData();
                if (mSelectedImageUri != null) {
                    mSelectedImagePath = Constants.convertMediaUriToPath(MyHomeActivity.this, mSelectedImageUri);
                } else {
                    Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
                }
            } else {
                mSelectedImagePath = mSelectedOutputPath;
            }
            if (SupportedClass.stringIsNotEmpty(mSelectedImagePath)) {
                startEditorActivity();
            }
        } else {
            Log.e("TAG", "");
        }
    }

    private void startEditorActivity() {
        try {
            StoreManager.setCurrentCropedBitmap(MyHomeActivity.this, (Bitmap) null);
            StoreManager.setCurrentCroppedMaskBitmap(MyHomeActivity.this, (Bitmap) null);

            Bitmap bitmap = Constants.getBitmapFromUri(MyHomeActivity.this, mSelectedImageUri, (float) screenWidth, (float) screenHeight);
            MyEditorActivity.setFaceBitmap(bitmap);
            StoreManager.setCurrentOriginalBitmap(this, bitmap);

            startActivity(new Intent(this, MyEditorActivity.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() {
        File storageDir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + BuildConfig.APPLICATION_ID + "/CamPic/");
        storageDir.mkdirs();
        File image = null;
        try {
            image = new File(storageDir, getString(R.string.app_folder));
            if (image.exists())
                image.delete();
            image.createNewFile();

            mSelectedOutputPath = image.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}