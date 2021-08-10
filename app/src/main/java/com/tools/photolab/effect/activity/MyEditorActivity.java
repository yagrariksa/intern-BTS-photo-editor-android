package com.tools.photolab.effect.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.tools.photolab.R;
import com.tools.photolab.effect.adapter.NeonEffectListAdapter;
import com.tools.photolab.effect.adapter.StickerAdapter;
import com.tools.photolab.effect.ads.FullScreenAdManager;
import com.tools.photolab.effect.callBack.MenuItemClickLister;
import com.tools.photolab.effect.callBack.StickerClickListener;
import com.tools.photolab.effect.crop_img.newCrop.MLCropAsyncTask;
import com.tools.photolab.effect.custom.CustomTextView;
import com.tools.photolab.effect.custom.DHANVINE_MultiTouchListener;
import com.tools.photolab.effect.custom.MultiTouchListener;
import com.tools.photolab.effect.custom.stickerView.DrawableSticker;
import com.tools.photolab.effect.custom.stickerView.Sticker;
import com.tools.photolab.effect.custom.stickerView.StickerView;
import com.tools.photolab.effect.erase_tool.StickerEraseActivity;
import com.tools.photolab.effect.support.Constants;
import com.tools.photolab.effect.support.FastBlur;
import com.tools.photolab.effect.support.ImageUtils;
import com.tools.photolab.effect.support.MyExceptionHandlerPix;
import com.tools.photolab.effect.support.SupportedClass;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MyEditorActivity extends BaseActivity implements MenuItemClickLister, View.OnClickListener {

    private Uri savedImageUri;
    private static Bitmap bmpPic = null;
    private Bitmap filteredForegroundBitmap2, filteredForegroundBitmap3, filteredForegroundBitmap4;
    private ImageView ivFace, setBack, setImg, setFront;
    private int rotateImage = 0, pos = 0, lastSelectedPosTab = 0, displayWidth;
    private StickerView stickerView;
    private RelativeLayout mContentRootView;
    private String oldSavedFileName;

    static public void notifyMediaScannerService(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, new String[]{"image/jpeg"}, null);
    }

    public static void setFaceBitmap(Bitmap bitmap) {
        bmpPic = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_editor);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandlerPix(MyEditorActivity.this));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayWidth = displayMetrics.widthPixels;

        RelativeLayout mAdView = findViewById(R.id.adView);
        loadBannerAds(mAdView);

        mContentRootView = findViewById(R.id.mContentRootView);

        ivFace = findViewById(R.id.iv_mov);
        setBack = findViewById(R.id.setback);
        setImg = findViewById(R.id.setimg);
        setFront = findViewById(R.id.setfront);

        stickerView = findViewById(R.id.sticker_view);

        setImg.setOnTouchListener(new MultiTouchListener());

        if (bmpPic != null) {
            filteredForegroundBitmap2 = bmpPic.copy(Bitmap.Config.ARGB_8888, true);
            filteredForegroundBitmap3 = bmpPic.copy(Bitmap.Config.ARGB_8888, true);
            filteredForegroundBitmap4 = bmpPic.copy(Bitmap.Config.ARGB_8888, true);

            (findViewById(R.id.iv_back)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            (findViewById(R.id.iv_save)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveImage();
                }
            });

            Bitmap createScaledBitmap = Bitmap.createScaledBitmap(filteredForegroundBitmap3, displayWidth, displayWidth, true);
            setImg.setImageBitmap(createScaledBitmap);
        } else {
            finish();
        }

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMenuListClick(View view, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void saveImage() {
        new saveImageTaskMaking().execute();
    }

    private Uri addImageToGallery(final String filePath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void openShareActivity() {
        Intent intent = new Intent(MyEditorActivity.this, ShareActivity.class);
        intent.putExtra(Constants.KEY_URI_IMAGE, savedImageUri.toString());
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private class saveImageTaskMaking extends AsyncTask<String, String, Exception> {
        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            if (e == null) {
                FullScreenAdManager.fullScreenAdsCheckPref(MyEditorActivity.this, FullScreenAdManager.ALL_PREFS.ATTR_ON_SHARE_SCREEN, new FullScreenAdManager.GetBackPointer() {
                    @Override
                    public void returnAction() {
                        openShareActivity();
                    }
                });
            } else {
                Toast.makeText(MyEditorActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Exception doInBackground(String... strings) {
            Bitmap bitmap = Bitmap.createBitmap(mContentRootView.getWidth(), mContentRootView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mContentRootView.draw(canvas);

            File myDir = new File(Environment.getExternalStorageDirectory().toString() + getString(R.string.app_folder));
            if (!myDir.exists())
                myDir.mkdirs();
            String filename = getString(R.string.app_file) + System.currentTimeMillis() + Constants.KEY_JPG;
            File file = new File(myDir, filename);
            if (oldSavedFileName != null) {
                File oldFile = new File(myDir, oldSavedFileName);
                if (oldFile.exists()) oldFile.delete();
            }
            oldSavedFileName = filename;
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                Uri uri = addImageToGallery(file.getAbsolutePath());
                if (uri != null)
                    savedImageUri = uri;
                notifyMediaScannerService(MyEditorActivity.this, myDir.getAbsolutePath());
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            stickerView.setLocked(true);
        }
    }

}