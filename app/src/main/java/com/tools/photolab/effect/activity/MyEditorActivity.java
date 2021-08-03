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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
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
import com.tools.photolab.effect.callBack.MenuItemClickLister;
import com.tools.photolab.effect.callBack.StickerClickListener;
import com.tools.photolab.effect.crop_img.newCrop.MLCropAsyncTask;
import com.tools.photolab.effect.custom.CustomTextView;
import com.tools.photolab.effect.custom.DHANVINE_MultiTouchListener;
import com.tools.photolab.effect.custom.stickerView.DrawableSticker;
import com.tools.photolab.effect.custom.stickerView.Sticker;
import com.tools.photolab.effect.custom.stickerView.StickerView;
import com.tools.photolab.effect.erase_tool.StickerEraseActivity;
import com.tools.photolab.effect.support.Constants;
import com.tools.photolab.effect.support.FastBlur;
import com.tools.photolab.effect.support.ImageUtils;
import com.tools.photolab.effect.support.SupportedClass;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MyEditorActivity extends BaseActivity implements MenuItemClickLister, View.OnClickListener {

    private Context mContext;
    SeekBar sbBackgroundOpacity;
    public int mCount = 0;
    boolean isFirstTime = true;
    private Animation slideUpAnimation, slideDownAnimation;
    public static ImageView setfront;
    private ImageView iv_face, setback, setimg;
    public static Bitmap eraserResultBmp;
    private ArrayList<String> neonEffect = new ArrayList<String>(), shapeEffect = new ArrayList<String>();
    private static Bitmap faceBitmap;
    private Bitmap selectedBit, cutBit;
    private RecyclerView recylcerFilter, recyclerSticker;
    private NeonEffectListAdapter neonEffectListAdapter;
    private TabLayout tabLayout;
    private StickerView stickerView;
    private RelativeLayout mContentRootView;
    private Sticker currentSticker;
    private LinearLayout linEffect, linBackgroundBlur, linThirdDivisionOption;
    private String oldSavedFileName;
    private Uri savedImageUri;

    public static void setFaceBitmap(Bitmap bitmap) {
        faceBitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_editor);

//        load Ads
        RelativeLayout mAdView = findViewById(R.id.adView);
        loadBannerAds(mAdView);

        mContext = this;
        selectedBit = faceBitmap;

        new Handler().postDelayed(() -> setimg.post(() -> {
            if (isFirstTime && selectedBit != null) {
                isFirstTime = false;
                initBMPNew();
            }
        }), 1000);

        neonEffect.add("none");
        // neonEffect looping add effect (filter)

        Init();
        setTollbarData();
    }

    // finish
    private void initBMPNew() {
        if (faceBitmap != null) {
            selectedBit = ImageUtils.getBitmapResize(mContext, faceBitmap, setimg.getWidth(), setimg.getHeight());
            mContentRootView.setLayoutParams(new LinearLayout.LayoutParams(selectedBit.getWidth(), selectedBit.getHeight()));
            if (selectedBit != null && iv_face != null) {
                iv_face.setImageBitmap(new FastBlur().processBlur(selectedBit, 1, sbBackgroundOpacity.getProgress() == 0 ? 1 : sbBackgroundOpacity.getProgress()));
            }
            cutmaskNew();
        }
    }

    private void Init() {
        findViewById(R.id.ivShowHomeOption).setVisibility(View.GONE);
        stickerView = (StickerView) findViewById(R.id.sticker_view);
        mContentRootView = findViewById(R.id.mContentRootView);
        setfront = findViewById(R.id.setfront);
        setback = findViewById(R.id.setback);
        iv_face = findViewById(R.id.iv_face);
        setimg = findViewById(R.id.setimg);

        iv_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stickerView.getCurrentSticker() != null) {
                    stickerView.getCurrentSticker().release();
                }
            }
        });

        linEffect = (LinearLayout) findViewById(R.id.linEffect);
        tabLayout = findViewById(R.id.tabs);
        createTabIcons();
        tabLayout.getTabAt(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onBottomTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onBottomTabSelected(tab);
            }
        });

        recylcerFilter = (RecyclerView) findViewById(R.id.recyclerForegroundFilter);
        recylcerFilter.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        linBackgroundBlur = findViewById(R.id.linBackgroundBlur);
        setUpBottomList();

        AppCompatImageView ivCheckMark = (AppCompatImageView) findViewById(R.id.ivCheckMark);
        ivCheckMark.setOnClickListener(this);
        AppCompatImageView ivClose = (AppCompatImageView) findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);

        recyclerSticker = findViewById(R.id.recyclerSticker);

        linThirdDivisionOption = (LinearLayout) findViewById(R.id.linThirdDivisionOption);
        initMainStickerViewMan();

        // Sticker List Create
        setStickerImages(30);

        tabLayout.setVisibility(View.VISIBLE);
        linEffect.setVisibility(View.GONE);
        linBackgroundBlur.setVisibility(View.GONE);

        findViewById(R.id.ivShowHomeOption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // gada tvBackgroundList
        iv_face.setRotation(0.0f);

        setimg.post(new Runnable() {
            @Override
            public void run() {
                initBMPNew();
            }
        });

        SeekBar sbFrameOpacity = findViewById(R.id.sbFrameOpacity);
        sbFrameOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (setback != null && setfront != null) {
                    float f = ((float) i) * 0.01f;
                    setback.setAlpha(f);
                    setfront.setAlpha(f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbBackgroundOpacity = findViewById(R.id.sbBackgroundOpacity);
        sbBackgroundOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (selectedBit != null && iv_face != null) {
                    iv_face.setImageBitmap(new FastBlur().processBlur(selectedBit, 1, seekBar.getProgress() == 0 ? 1 : seekBar.getProgress()));
                }
            }
        });
    }

    // finish
    public void setTollbarData() {
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackDialog();
            }
        });
        findViewById(R.id.tv_applay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new saveImageTaskMaking().execute();
            }
        });
    }

    // finish
    private void showBackDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_leave);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView button = (TextView) dialog.findViewById(R.id.btn_yes);
        TextView button2 = (TextView) dialog.findViewById(R.id.btn_no);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                dialog.dismiss();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
            }
        });
        dialog.show();
    }

    // finish
    private void createTabIcons() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView textOne = view.findViewById(R.id.text);
        ImageView ImageOne = view.findViewById(R.id.image);
        textOne.setText(getString(R.string.txt_effect));
        textOne.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
//        textOne.setTypeface(face);
        ImageOne.setImageResource(R.drawable.ic_module);
        textOne.setTextSize(12);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view));

        View view3 = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView text3 = view3.findViewById(R.id.text);
        ImageView Image3 = view3.findViewById(R.id.image);
        text3.setText(getString(R.string.txt_stickers));
        Image3.setImageResource(R.drawable.ic_stickers);
        text3.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
//        text3.setTypeface(face);
        textOne.setTextSize(12);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));

        View view2 = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView textTwo = view2.findViewById(R.id.text);
        ImageView ImageTwo = view2.findViewById(R.id.image);
        textTwo.setText(getString(R.string.txt_erase));
        ImageTwo.setImageResource(R.drawable.ic_erase);
        textTwo.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
//        textTwo.setTypeface(face);
        textOne.setTextSize(12);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));

        View view4 = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView text4 = view4.findViewById(R.id.text);
        ImageView Image4 = view4.findViewById(R.id.image);
        text4.setText(getString(R.string.txt_background));
        Image4.setImageResource(R.drawable.ic_backchange);
        text4.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
//        text4.setTypeface(face);
        textOne.setTextSize(12);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view4));
    }

    // finish
    private void onBottomTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) {
            findViewById(R.id.ivShowHomeOption).setVisibility(View.VISIBLE);
            viewSlideUpDown(linEffect, tabLayout);

        } else if (tab.getPosition() == 1) {
            itemSelectFromList(linThirdDivisionOption, recyclerSticker, true);

        } else if (tab.getPosition() == 2) {
            StickerEraseActivity.b = cutBit;
            Intent intent = new Intent(this, StickerEraseActivity.class);
            intent.putExtra(Constants.KEY_OPEN_FROM, Constants.VALUE_OPEN_FROM_NEON);
            startActivityForResult(intent, 1024);

        } else if (tab.getPosition() == 3) {
            findViewById(R.id.ivShowHomeOption).setVisibility(View.VISIBLE);
            viewSlideUpDown(linBackgroundBlur, tabLayout);
        }
    }

    final ArrayList<Integer> stickerArrayList = new ArrayList<>();

    // finish
    public void setStickerImages(int size) {
        for (int i = 1; i <= size; i++) {
            Resources resources = getResources();
            stickerArrayList.add(Integer.valueOf(resources.getIdentifier("sticker_n" + i, "drawable", getPackageName())));
        }
        recyclerSticker = findViewById(R.id.recyclerSticker);
        recyclerSticker.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerSticker.setAdapter(new StickerAdapter(this, stickerArrayList, new StickerClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                itemSelectFromList(linThirdDivisionOption, recyclerSticker, false);
                Drawable drawable = getResources().getDrawable(stickerArrayList.get(position));
                stickerView.addSticker(new DrawableSticker(drawable));
            }
        }));
    }

    // finish
    public void cutmaskNew() {

        ProgressBar progressBar = findViewById(R.id.crop_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        final ProgressBar progressBar2 = progressBar;
        new CountDownTimer(5000, 1000) {
            public void onFinish() {
            }

            public void onTick(long j) {
                int unused = mCount = mCount + 1;
                if (progressBar2.getProgress() <= 90) {
                    progressBar2.setProgress(mCount * 5);
                }
            }
        }.start();

        new MLCropAsyncTask((bitmap, bitmap2, left, top) -> {
            int[] iArr = {0, 0, selectedBit.getWidth(), selectedBit.getHeight()};
            int width = selectedBit.getWidth();
            int height = selectedBit.getHeight();
            int i = width * height;
            selectedBit.getPixels(new int[i], 0, width, 0, 0, width, height);
            int[] iArr2 = new int[i];
            Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            createBitmap.setPixels(iArr2, 0, width, 0, 0, width, height);
            cutBit = ImageUtils.getMask(mContext, selectedBit, createBitmap, width, height);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    bitmap, cutBit.getWidth(), cutBit.getHeight(), false);
            cutBit = resizedBitmap;

            runOnUiThread(() -> {
                Palette p = Palette.from(cutBit).generate();
                if (p.getDominantSwatch() == null) {
                    Toast.makeText(MyEditorActivity.this, getString(R.string.txt_not_detect_human), Toast.LENGTH_SHORT).show();
                }
                setimg.setImageBitmap(cutBit);
            });


        }, this, progressBar).execute(new Void[0]);
    }

    // finish
    public void setUpBottomList() {
        neonEffectListAdapter = new NeonEffectListAdapter(mContext);
        neonEffectListAdapter.setClickListener(this);
        recylcerFilter.setAdapter(neonEffectListAdapter);
        neonEffectListAdapter.addData(neonEffect);
    }

    // finish
    private void initMainStickerViewMan() {
        stickerView.setLocked(false);
        stickerView.setConstrained(true);
        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
                Log.e("TAG", "onStickerAdded");
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                Log.e("TAG", "onStickerClicked");
            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {
                removeStickerWithDeleteIcon();
                Log.e("TAG", "onStickerDeleted");
            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {
                Log.e("TAG", "onStickerDragFinished");
            }

            @Override
            public void onStickerTouchedDown(@NonNull final Sticker sticker) {
                stickerOptionTaskPerformMan(sticker);
            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {
                Log.e("TAG", "onStickerZoomFinished");
            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {
                Log.e("TAG", "onStickerFlipped");
            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                Log.e("TAG", "onDoubleTapped: double tap will be with two click");
            }
        });
    }

    // finish
    private void removeStickerWithDeleteIcon() {
        stickerView.remove(currentSticker);
        currentSticker = null;
        if (stickerView.getStickerCount() == 0) {

        } else {
            currentSticker = stickerView.getLastSticker();
        }
    }

    // finish
    public void stickerOptionTaskPerformMan(Sticker sticker) {
        stickerView.setLocked(false);
        currentSticker = sticker;
        stickerView.sendToLayer(stickerView.getStickerPosition(currentSticker));
        Log.e("TAG", "onStickerTouchedDown");
    }


    // finish
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        findViewById(R.id.ivShowHomeOption).setVisibility(View.GONE);
        if (linEffect.getVisibility() == View.VISIBLE) {
            viewSlideUpDown(tabLayout, linEffect);
        } else if (linBackgroundBlur.getVisibility() == View.VISIBLE) {
            viewSlideUpDown(tabLayout, linBackgroundBlur);
        } else if (linThirdDivisionOption.getVisibility() == View.VISIBLE) {
            findViewById(R.id.ivClose).performClick();
        } else {
            showBackDialog();
        }
    }

    // finish
    public void viewSlideUpDown(final View showLayout, final View hideLayout) {
        showLayout.setVisibility(View.VISIBLE);
        slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        showLayout.startAnimation(slideUpAnimation);
        slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        hideLayout.startAnimation(slideDownAnimation);
        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hideLayout.setVisibility(View.GONE);
                // recyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    // finish
    public void onMenuListClick(View view, int i) {
        if (i != 0) {
            Bitmap backspiral = ImageUtils.getBitmapFromAsset(mContext, "spiral/back/" + neonEffectListAdapter.getItemList().get(i)
                    + "_back.png");
            Bitmap fronspiral = ImageUtils.getBitmapFromAsset(mContext, "spiral/front/" + neonEffectListAdapter.getItemList().get(i) + "_front.png");
            setback.setImageBitmap(backspiral);
            setfront.setImageBitmap(fronspiral);
        } else {
            setback.setImageResource(0);
            setfront.setImageResource(0);
        }
        setback.setOnTouchListener(new DHANVINE_MultiTouchListener(this, true));
    }

    // finish
    public void itemSelectFromList(final LinearLayout linLayout, final RecyclerView recyclerView, boolean upAnimation) {
        if (upAnimation) {
            linLayout.setVisibility(View.VISIBLE);
            slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
            linLayout.startAnimation(slideUpAnimation);
            recyclerView.scrollToPosition(0);
        } else {
            slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
            linLayout.startAnimation(slideDownAnimation);
            slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    linLayout.setVisibility(View.GONE);
                    // recyclerView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    // almost finish
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCheckMark:
            case R.id.ivClose:
                if (linThirdDivisionOption.getVisibility() == View.VISIBLE) {
                    if (currentSticker == null) {
                        currentSticker = stickerView.getCurrentSticker();
                    }

                    if (recyclerSticker.getVisibility() == View.VISIBLE) {
                        itemSelectFromList(linThirdDivisionOption, recyclerSticker, false);
                    }
                }
                break;
        }
    }

    // finish
    private class saveImageTaskMaking extends android.os.AsyncTask<String, String, Exception> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.ivShowHomeOption).setVisibility(View.GONE);
            stickerView.setLocked(true);
        }

        public Bitmap getBitmapFromView(View view) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }


        @Override
        protected Exception doInBackground(String... strings) {
            mContentRootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = getBitmapFromView(mContentRootView);

            File myDir = new File(Environment.getExternalStorageDirectory().toString() + getString(R.string.app_folder));
            if (!myDir.exists())
                myDir.mkdirs();
            String fileName = getString(R.string.app_file) + System.currentTimeMillis() + Constants.KEY_JPG;
            File file = new File(myDir, fileName);
            if (oldSavedFileName != null) {
                File oldFile = new File(myDir, oldSavedFileName);
                if (oldFile.exists()) oldFile.delete();
            }
            oldSavedFileName = fileName;
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                Uri uri = SupportedClass.addImageToGallery(MyEditorActivity.this, file.getAbsolutePath());
                if (uri != null)
                    savedImageUri = uri;
//                notifyMediaScannerService(NeonActivity.this, myDir.getAbsolutePath());
                return null;
            } catch (Exception e) {
                return e;
            } finally {
                mContentRootView.setDrawingCacheEnabled(false);
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            findViewById(R.id.ivShowHomeOption).setVisibility(View.GONE);
            if (e == null) {
                openShareActivity();
//                loadAd();
//                FullScreenAdManager.fullScreenAdsCheckPref(NeonActivity.this, FullScreenAdManager.ALL_PREFS.ATTR_ON_SHARE_SCREEN, NeonActivity.this::openShareActivity);
            } else {
                Toast.makeText(MyEditorActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // finish
    public void openShareActivity() {
        Intent intent = new Intent(MyEditorActivity.this, ShareActivity.class);
        intent.putExtra(Constants.KEY_URI_IMAGE, savedImageUri.toString());
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }
}