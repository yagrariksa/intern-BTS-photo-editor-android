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
import android.media.ThumbnailUtils;
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
import com.tools.photolab.effect.adapter.FiltersForegroundAdapter;
import com.tools.photolab.effect.adapter.NeonEffectListAdapter;
import com.tools.photolab.effect.adapter.StickerAdapter;
import com.tools.photolab.effect.adapter.StickerCategoryListAdapter;
import com.tools.photolab.effect.ads.FullScreenAdManager;
import com.tools.photolab.effect.callBack.FilterPixItemClickListener;
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
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MyEditorActivity extends BaseActivity implements MenuItemClickLister, View.OnClickListener {

    private Uri savedImageUri;
    private static Bitmap bmpPic = null;
    private Bitmap filteredForegroundBitmap2, filteredForegroundBitmap3, filteredForegroundBitmap4;
    private ImageView mMovImage, mMainFrame;
    private int rotateImage = 0, pos = 0, lastSelectedPosTab = 0, displayWidth;
    private StickerView stickerView;
    private RelativeLayout mContentRootView;
    private String oldSavedFileName;
    private TabLayout tabLayout;
    private Context mContext;
    private Sticker currentSticker;
    private Animation slideUpAnimation, slideDownAnimation;
    private RecyclerView mRecyclerForegroundFilter, mRecyclerSticker;
    private FiltersForegroundAdapter filtersForegroundAdapter;

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
        mContext = this;

        findViewById(R.id.ivShowHomeOption).setVisibility(View.GONE);
        findViewById(R.id.ivShowHomeOption).setOnClickListener(this);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandlerPix(MyEditorActivity.this));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayWidth = displayMetrics.widthPixels;

        RelativeLayout mAdView = findViewById(R.id.adView);
        loadBannerAds(mAdView);

        mContentRootView = findViewById(R.id.mContentRootView);

        mMovImage = findViewById(R.id.iv_mov);
        mMainFrame = findViewById(R.id.main_frame);

        mMovImage.setOnTouchListener(new MultiTouchListener());

        stickerView = findViewById(R.id.sticker_view);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
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

        mRecyclerForegroundFilter = (RecyclerView) findViewById(R.id.recyclerForegroundFilter);
        mRecyclerForegroundFilter.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        AppCompatImageView ivCheckMark = (AppCompatImageView) findViewById(R.id.ivCheckMark);
        ivCheckMark.setOnClickListener(this);
        AppCompatImageView ivClose = (AppCompatImageView) findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);
        mRecyclerSticker = (RecyclerView) findViewById(R.id.recyclerSticker);
        mRecyclerSticker.setLayoutManager(new GridLayoutManager(this, 3));
        initMainStickerViewMan();
        setStickerImages(30);


        if (bmpPic != null) {
            filteredForegroundBitmap2 = bmpPic.copy(Bitmap.Config.ARGB_8888, true);
            filteredForegroundBitmap3 = bmpPic.copy(Bitmap.Config.ARGB_8888, true);
            filteredForegroundBitmap4 = bmpPic.copy(Bitmap.Config.ARGB_8888, true);

            mRecyclerForegroundFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bmpPic, 128, 128);
            /**
             * disable this method because
             * this method is initiate thumbnail
             * of effect to picture
             *
             * error on applyRGBCurve
             */
//            prepareThumbnailBackground(thumbBitmap);

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
            mMovImage.setImageBitmap(createScaledBitmap);
        } else {
            finish();
        }

    }


    public void prepareThumbnailBackground(final Bitmap thumbBitmaps) {
        Runnable r = new Runnable() {
            public void run() {

                ThumbnailsManager.clearThumbs();
                final List<ThumbnailItem> thumbnailItemList = new ArrayList<>();

                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbBitmaps;
                thumbnailItem.filterName = getString(R.string.filter_normal);
                ThumbnailsManager.addThumb(thumbnailItem);
                thumbnailItemList.add(thumbnailItem);


                List<Filter> filters = FilterPack.getFilterPack(MyEditorActivity.this);
                for (int i = 0; i < filters.size() - 3; i++) {
                    Filter filter = filters.get(i);
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbBitmaps;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }
                thumbnailItemList.addAll(ThumbnailsManager.processThumbs(MyEditorActivity.this));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filtersForegroundAdapter = new FiltersForegroundAdapter(thumbnailItemList, rotateImage, new FilterPixItemClickListener() {
                            @Override
                            public void onFilterClicked(Filter filter) {
                                filteredForegroundBitmap2 = filteredForegroundBitmap4.copy(Bitmap.Config.ARGB_8888, true);
                                filteredForegroundBitmap3 = filter.processFilter(filteredForegroundBitmap2);
                                //  setImg.setImageBitmap(filteredForegroundBitmap3);
                                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(filteredForegroundBitmap3, displayWidth, displayWidth, true);
                                mMovImage.setImageBitmap(createScaledBitmap);
                            }
                        });
                        mRecyclerForegroundFilter.setAdapter(filtersForegroundAdapter);
                    }
                });
            }
        };

        new Thread(r).start();
    }

    public void onBottomTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) {
            findViewById(R.id.ivShowHomeOption).setVisibility(View.VISIBLE);
            viewSlideUpDown(mRecyclerForegroundFilter, tabLayout);
        } else if (tab.getPosition() == 1) {
            findViewById(R.id.ivShowHomeOption).setVisibility(View.VISIBLE);
            viewSlideUpDown(mRecyclerSticker, tabLayout);
        } else if (tab.getPosition() == 2) {

        } else if (tab.getPosition() == 3) {

        }
        Log.e("TABS", String.valueOf(tab.getPosition()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivShowHomeOption:
                if (mRecyclerForegroundFilter.getVisibility() == View.VISIBLE) {
                    viewSlideUpDown(tabLayout, mRecyclerForegroundFilter);
                } else if (mRecyclerSticker.getVisibility() == View.VISIBLE) {
                    viewSlideUpDown(tabLayout, mRecyclerSticker);
                }
                break;
        }
    }

    @Override
    public void onMenuListClick(View view, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void itemSelectFromList(final LinearLayout linLayout, final RecyclerView recyclerView, boolean upAnimation) {
        //recyclerView.setVisibility(View.VISIBLE);
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

    private void createTabIcons() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView textOne = (CustomTextView) view.findViewById(R.id.text);
        ImageView ImageOne = (ImageView) view.findViewById(R.id.image);
        textOne.setText(getString(R.string.txt_effect));
        ImageOne.setImageResource(R.drawable.ic_neon_effect_svg);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view));

        View view3 = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView text3 = (CustomTextView) view3.findViewById(R.id.text);
        ImageView Image3 = (ImageView) view3.findViewById(R.id.image);
        text3.setText(getString(R.string.txt_stickers));
        Image3.setImageResource(R.drawable.ic_stickers);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));

        View view2 = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView textTwo = (CustomTextView) view2.findViewById(R.id.text);
        ImageView ImageTwo = (ImageView) view2.findViewById(R.id.image);
        textTwo.setText(getString(R.string.txt_erase));
        ImageTwo.setImageResource(R.drawable.ic_erase);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));

        View view4 = LayoutInflater.from(this).inflate(R.layout.custom_neon_tab, null);
        CustomTextView text4 = (CustomTextView) view4.findViewById(R.id.text);
        ImageView Image4 = (ImageView) view4.findViewById(R.id.image);
        text4.setText(getString(R.string.txt_background));
        Image4.setImageResource(R.drawable.ic_backchange);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view4));
    }

    public void viewSlideUpDown(final View showLaout, final View hideLayout) {
        showLaout.setVisibility(View.VISIBLE);
        slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        showLaout.startAnimation(slideUpAnimation);
        slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        hideLayout.startAnimation(slideDownAnimation);
        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hideLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
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


    public void setStickerImages(int size) {
        final ArrayList<Integer> stickerArrayList = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            Resources resources = getResources();
            stickerArrayList.add(Integer.valueOf(resources.getIdentifier("sticker_n" + i, "drawable", getPackageName())));
        }
        mRecyclerSticker = findViewById(R.id.recyclerSticker);
        mRecyclerSticker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerSticker.setAdapter(new StickerCategoryListAdapter(this, stickerArrayList, new StickerClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Drawable drawable = getResources().getDrawable(stickerArrayList.get(position));
                stickerView.addSticker(new DrawableSticker(drawable));
            }
        }));

    }

    public void stickerOptionTaskPerformMan(Sticker sticker) {
        stickerView.setLocked(false);
        currentSticker = sticker;
        stickerView.sendToLayer(stickerView.getStickerPosition(currentSticker));
        Log.e("TAG", "onStickerTouchedDown");
    }

    private void removeStickerWithDeleteIcon() {
        stickerView.remove(currentSticker);
        currentSticker = null;
        if (stickerView.getStickerCount() == 0) {

        } else {
            currentSticker = stickerView.getLastSticker();
        }
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