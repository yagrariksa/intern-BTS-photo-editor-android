package com.tools.photolab.effect.ads;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.tools.photolab.R;
import com.tools.photolab.effect.support.SupportedClass;

public class FullScreenAdManager {

    public static final String PREF_ON_PHOTO_SCREEN = "PREF_ON_PHOTO_SCREEN";
    public static final String PREF_ON_HOME_SCREEN = "PREF_ON_HOME_SCREEN";
    public static final String PREF_ON_FIRST_PIX_SCREEN = "PREF_ON_FIRST_PIX_SCREEN";
    public static final String PREF_ON_SHARE_SCREEN = "PREF_ON_SHARE_SCREEN";
    public static final String PREF_SAVED_IMAGE_CLICKED = "SAVED_IMAGE_CLICKED";


    public static void initFullScreenAds(android.content.Context context) {
        if (SupportedClass.checkConnection(context)) {
            if (interstitialAd == null) {
                interstitialAd = new InterstitialAd(context);
                interstitialAd.setAdUnitId(context.getString(R.string.admob_interstitial_ads_id));
            }
            loadFullScreenAd(context);
        }

    }


    public static InterstitialAd interstitialAd;

    public static void loadFullScreenAd(android.content.Context context) {
        if (interstitialAd != null && !interstitialAd.isLoaded() && SupportedClass.checkConnection(context)) {
            interstitialAd.loadAd(new AdRequest.Builder()
                    .build());
        }

    }

    public static void fullScreenAdsCheckPref(final android.content.Context context, ALL_PREFS all_prefs, final GetBackPointer getBackPointer) {

        int getCount = SharedPreferencesHelper.getInstance().getInt(all_prefs.prefName, 0);
        int newCount = getCount + 1;
        SharedPreferencesHelper.getInstance().setInt(all_prefs.prefName, newCount);
        if (getCount != 0 && getCount % all_prefs.value == 0 && interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    loadFullScreenAd(context);
                    if (getBackPointer != null)
                        getBackPointer.returnAction();
                    super.onAdClosed();
                }
            });
        } else if (getBackPointer != null)
            getBackPointer.returnAction();

    }


    public enum ALL_PREFS {

        ATTR_ON_PHOTO_SCREEN(PREF_ON_PHOTO_SCREEN, 2),   //number click per ads are comming
        ATTR_ON_HOME_SCREEN(PREF_ON_HOME_SCREEN, 2),
        ATTR_ON_FIRST_PIX_SCREEN(PREF_ON_FIRST_PIX_SCREEN, 2),
        ATTR_ON_SHARE_SCREEN(PREF_ON_SHARE_SCREEN, 2),
        ATTR_SAVED_IMAGE_CLICKED(PREF_SAVED_IMAGE_CLICKED, 2);

        private String prefName;
        private int value;

        /**
         * @param prefName Preferences name
         * @param value    Preferences default value
         */
        ALL_PREFS(String prefName, int value) {
            this.prefName = prefName;
            this.value = value;
        }
    }


    public interface GetBackPointer {
        public void returnAction();
    }


}
