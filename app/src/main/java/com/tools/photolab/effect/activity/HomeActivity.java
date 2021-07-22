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
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.tools.photolab.BuildConfig;
import com.tools.photolab.R;
import com.tools.photolab.effect.ads.FullScreenAdManager;
import com.tools.photolab.effect.blur_tool.BlurActivity;
import com.tools.photolab.effect.color_splash_tool.ColorSplashActivity;
import com.tools.photolab.effect.motion_tool.MotionEffectActivity;
import com.tools.photolab.effect.crop_img.newCrop.StoreManager;
import com.tools.photolab.effect.rate.AppRate;
import com.tools.photolab.effect.rate.OnClickButtonListener;
import com.tools.photolab.effect.support.Constants;
import com.tools.photolab.effect.support.SupportedClass;
import com.tools.photolab.effect.support.MyExceptionHandlerPix;

import java.io.File;
import java.io.IOException;
import static com.tools.photolab.effect.color_splash_tool.ColorSplashActivity.REQUEST_CODE_CAMERA;

public class HomeActivity extends BaseActivity {
    protected static final int MY_PERMISSIONS_REQUEST_CAMERA_STORAGE = 0x1;
    protected static final int REQUEST_CODE_GALLERY = 0x3;
    protected static final int REQUEST_CODE_CROPPING = 0x4;
    public static int screenHeight;
    public static int screenWidth;
    public Uri mSelectedImageUri;
    public String mSelectedImagePath;
    public String mSelectedOutputPath;
    FEATURES selectedFeatures = FEATURES.PIX_LAB_EFFECT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandlerPix(HomeActivity.this));

        RelativeLayout mAdView = findViewById(R.id.adView);
        loadBannerAds(mAdView);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels - Constants.dpToPx(this, 4);
        screenHeight = displayMetrics.heightPixels - Constants.dpToPx(this, 109);

        StoreManager.setCurrentCropedBitmap(HomeActivity.this, (Bitmap) null);
        StoreManager.setCurrentCroppedMaskBitmap(HomeActivity.this, (Bitmap) null);
        lin_pix  = findViewById(R.id.lin_pix);
        lin_b_w_effect  = findViewById(R.id.lin_b_w_effect);
        lin_blur_effect  = findViewById(R.id.lin_blur_effect);
        lin_neon_effect  = findViewById(R.id.lin_neon_effect);
        lin_wings_effect  = findViewById(R.id.lin_wings_effect);
        lin_frame_effect  = findViewById(R.id.lin_frame_effect);
        lin_drip_effect  = findViewById(R.id.lin_drip_effect);
        lin_my_photos  = findViewById(R.id.lin_my_photos);
        lin_motion_effect  = findViewById(R.id.lin_motion_effect);
        //rating app
        showRateDialog();

        //click for all effect
        lin_pix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFeatures = FEATURES.PIX_LAB_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();

            }
        });
        lin_b_w_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedFeatures = FEATURES.B_W_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();
            }
        });

        lin_blur_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                selectedFeatures = FEATURES.BLUR_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();
            }
        });

        lin_neon_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                selectedFeatures = FEATURES.NEON_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();


            }
        });  lin_wings_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFeatures = FEATURES.WINGS_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();

            }
        });
        lin_frame_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFeatures = FEATURES.FRAME_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();
            }
        });

        lin_drip_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFeatures = FEATURES.DRIP_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();
            }
        });

        lin_my_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFeatures = FEATURES.MY_PHOTOS;
                if (requestCameraStoragePermission())
                    takeAction();
            }
        });

        lin_motion_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFeatures = FEATURES.MOTION_EFFECT;
                if (requestCameraStoragePermission())
                    takeAction();
            }
        });

    }

    LinearLayout lin_pix;
    LinearLayout lin_b_w_effect;
    LinearLayout lin_neon_effect;
    LinearLayout lin_wings_effect;
    LinearLayout lin_frame_effect;
    LinearLayout lin_blur_effect;
    LinearLayout lin_drip_effect;
    LinearLayout lin_my_photos;
    LinearLayout lin_motion_effect;

    public boolean requestCameraStoragePermission() {
        //checking permission
        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // get both camera and storage permission
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA_STORAGE);
            return false;

        } else if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // get only camera permission
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA_STORAGE);
            return false;

        } else if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // get only storage permission
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA_STORAGE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA_STORAGE && requestCameraStoragePermission())
            takeAction();
    }

    public void takeAction() {
        if (selectedFeatures == FEATURES.PIX_LAB_EFFECT) {
            pixDialog();

        } else if (selectedFeatures == FEATURES.B_W_EFFECT) {
            Intent intent = new Intent(HomeActivity.this, ColorSplashActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);

        } else if (selectedFeatures == FEATURES.BLUR_EFFECT) {
            Intent intent = new Intent(HomeActivity.this, BlurActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);

        } else if (selectedFeatures == FEATURES.NEON_EFFECT) {
            pixDialog();

        } else if (selectedFeatures == FEATURES.WINGS_EFFECT) {
            pixDialog();

        }  else if (selectedFeatures == FEATURES.FRAME_EFFECT) {
            pixDialog();

        } else if (selectedFeatures == FEATURES.DRIP_EFFECT) {
            pixDialog();

        } else if (selectedFeatures == FEATURES.MY_PHOTOS) {
            openMyCreatePhotos();
        }else if (selectedFeatures == FEATURES.MOTION_EFFECT) {
            pixDialog();
        }
    }

    public void pixDialog() {
        final Dialog pixDialog = new Dialog(this);
        pixDialog.setContentView(R.layout.dialog_select_photo);
        pixDialog.setCancelable(false);
        pixDialog.setCanceledOnTouchOutside(false);
        Window window = pixDialog.getWindow();
        window.setLayout(((SupportedClass.getWidth(HomeActivity.this) / 100) * 90), LinearLayout.LayoutParams.WRAP_CONTENT);
        pixDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        LinearLayout camera_item = pixDialog.findViewById(R.id.camera_item);
        LinearLayout gallery_item = pixDialog.findViewById(R.id.gallery_item);
        ImageView btnDismiss =  pixDialog.findViewById(R.id.cancel);
        gallery_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.txt_select_picture)), REQUEST_CODE_GALLERY);
                if (pixDialog.isShowing() && !isFinishing()) {
                    pixDialog.dismiss();
                }
            }
        });
        camera_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", createImageFile());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (intent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_CODE_CAMERA);
                }
                if (pixDialog.isShowing() && !isFinishing()) {
                    pixDialog.dismiss();
                }
            }
        });
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pixDialog.dismiss();
            }
        });

        pixDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA) {
            mSelectedImagePath = mSelectedOutputPath;
            if (SupportedClass.stringIsNotEmpty(mSelectedImagePath)) {
                File fileImageClick = new File(mSelectedImagePath);
                if (fileImageClick.exists()) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        mSelectedImageUri = Uri.fromFile(fileImageClick);
                    } else {
                        mSelectedImageUri = FileProvider.getUriForFile(HomeActivity.this, BuildConfig.APPLICATION_ID + ".provider", fileImageClick);
                    }
                    onPhotoTakenApp();
                }
            }
        } else if (data != null && data.getData() != null) {
            if (requestCode == REQUEST_CODE_GALLERY) {
                mSelectedImageUri = data.getData();
                if (mSelectedImageUri != null) {
                    mSelectedImagePath = Constants.convertMediaUriToPath(HomeActivity.this, mSelectedImageUri);
                } else {
                    Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
                }
            } else {
                mSelectedImagePath = mSelectedOutputPath;
            }
            if (SupportedClass.stringIsNotEmpty(mSelectedImagePath)) {
                onPhotoTakenApp();
            }

        } else if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_CODE_CROPPING && selectedFeatures == FEATURES.PIX_LAB_EFFECT) {
            if (data.hasExtra("croppedUri")) {
                // handleUri((Uri) data.getParcelableExtra("croppedUri"));
                mSelectedImageUri = data.getParcelableExtra("croppedUri");
                Bitmap bitmap = null;
                try {
                    bitmap = Constants.getBitmapFromUriDrip(HomeActivity.this, mSelectedImageUri, 1080, 1080);
                    PixLabActivity.setFaceBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    //  mSelectedImagePath = Constants.convertMediaUriToPath(HomeActivity.this, mSelectedImageUri);

                    FullScreenAdManager.fullScreenAdsCheckPref(HomeActivity.this, FullScreenAdManager.ALL_PREFS.ATTR_ON_FIRST_PIX_SCREEN, new FullScreenAdManager.GetBackPointer() {
                        @Override
                        public void returnAction() {
                            Intent intent = new Intent(HomeActivity.this, PixLabActivity.class);
//                                intent.putExtra(Constants.KEY_SELECTED_IMAGE_PATH, mSelectedImagePath);
                            intent.putExtra(Constants.KEY_FROM_MAIN, getString(R.string.txt_gallery));
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                    });

                }

            }

        } else if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_CODE_CROPPING && selectedFeatures == FEATURES.DRIP_EFFECT) {
            try {
                mSelectedImageUri = data.getParcelableExtra("croppedUri");
                StoreManager.setCurrentCropedBitmap(HomeActivity.this, (Bitmap) null);
                StoreManager.setCurrentCroppedMaskBitmap(HomeActivity.this, (Bitmap) null);

                Bitmap bitmap = Constants.getBitmapFromUriDrip(HomeActivity.this, mSelectedImageUri, (float) screenWidth, (float) screenHeight);
                DripEffectActivity.setFaceBitmap(bitmap);
                StoreManager.setCurrentOriginalBitmap(this, bitmap);

                startActivity(new Intent(HomeActivity.this, DripEffectActivity.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_CODE_CROPPING && selectedFeatures == FEATURES.MOTION_EFFECT) {
            try {
                mSelectedImageUri = data.getParcelableExtra("croppedUri");
                StoreManager.setCurrentCropedBitmap(HomeActivity.this, (Bitmap) null);
                StoreManager.setCurrentCroppedMaskBitmap(HomeActivity.this, (Bitmap) null);

                Bitmap bitmap = Constants.getBitmapFromUriDrip(HomeActivity.this, mSelectedImageUri, (float) screenWidth, (float) screenHeight);
                MotionEffectActivity.setFaceBitmap(bitmap);
                StoreManager.setCurrentOriginalBitmap(this, bitmap);

                startActivity(new Intent(HomeActivity.this, MotionEffectActivity.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("TAG", "");
        }
    }

    public void onPhotoTakenApp() {
        if (selectedFeatures == FEATURES.PIX_LAB_EFFECT || selectedFeatures == FEATURES.DRIP_EFFECT || selectedFeatures == FEATURES.MOTION_EFFECT) {
            Intent intent = new Intent(HomeActivity.this, CropPhotoActivity.class);
            intent.putExtra("cropUri", mSelectedImageUri.toString());
            startActivityForResult(intent, REQUEST_CODE_CROPPING);

        } else if (selectedFeatures == FEATURES.NEON_EFFECT) {
            try {
                StoreManager.setCurrentCropedBitmap(HomeActivity.this, (Bitmap) null);
                StoreManager.setCurrentCroppedMaskBitmap(HomeActivity.this, (Bitmap) null);

                Bitmap bitmap = Constants.getBitmapFromUri(HomeActivity.this, mSelectedImageUri, (float) screenWidth, (float) screenHeight);
                NeonActivity.setFaceBitmap(bitmap);
                StoreManager.setCurrentOriginalBitmap(this, bitmap);

                startActivity(new Intent(this, NeonActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (selectedFeatures == FEATURES.WINGS_EFFECT) {
            try {
                StoreManager.setCurrentCropedBitmap(HomeActivity.this, (Bitmap) null);
                StoreManager.setCurrentCroppedMaskBitmap(HomeActivity.this, (Bitmap) null);

                Bitmap bitmap = Constants.getBitmapFromUri(HomeActivity.this, mSelectedImageUri, (float) screenWidth, (float) screenHeight);
                WingsActivity.setFaceBitmap(bitmap);
                StoreManager.setCurrentOriginalBitmap(this, bitmap);

                startActivity(new Intent(this, WingsActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (selectedFeatures == FEATURES.FRAME_EFFECT) {
            try {
                StoreManager.setCurrentCropedBitmap(HomeActivity.this, (Bitmap) null);
                StoreManager.setCurrentCroppedMaskBitmap(HomeActivity.this, (Bitmap) null);

                Bitmap bitmap = Constants.getBitmapFromUri(HomeActivity.this, mSelectedImageUri, (float) screenWidth, (float) screenHeight);
                FramesActivity.setFaceBitmap(bitmap);
                StoreManager.setCurrentOriginalBitmap(this, bitmap);

                startActivity(new Intent(this, FramesActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void forAds(String packageString, String path) {
        boolean isAppInstalled = appInstalledOrNot(packageString);

        if (isAppInstalled) {
            //This intent will help you to launch if the package is already installed
            Intent LaunchIntent = getPackageManager()
                    .getLaunchIntentForPackage(packageString);
            startActivity(LaunchIntent);
        } else {
            // Do whatever we want to do if application not installed
            // For example, Redirect to play store

            openPlaystoreApps(path);
        }
    }

    protected void openMyCreatePhotos() {
        startActivity(new Intent(this, MyCreatePhotosActivity.class));
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return false;
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


    public void showRateDialog() {
        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(false) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(HomeActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

    }



    enum FEATURES {
        //effect wise flag
        PIX_LAB_EFFECT,
        B_W_EFFECT,
        BLUR_EFFECT,
        NEON_EFFECT,
        WINGS_EFFECT,
        FRAME_EFFECT,
        DRIP_EFFECT,
        MY_PHOTOS,
        MOTION_EFFECT
    }
}