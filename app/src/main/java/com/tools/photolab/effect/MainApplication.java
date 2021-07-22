package com.tools.photolab.effect;

import android.content.Context;
import androidx.multidex.MultiDex;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

public class MainApplication extends android.app.Application {

    public static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        MainApplication.context = context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);


    }

    @Override
    public void onCreate() {
        super.onCreate();
        setContext(getApplicationContext());
        List<String> testDevices = new ArrayList<>();
        testDevices.add(AdRequest.DEVICE_ID_EMULATOR);
        testDevices.add("7BCD9CF420BB02B0F1B252AFAC067BE3");
        testDevices.add("16805B774118DE1F6B1BE6F37825F291");

        FirebaseApp.initializeApp(this);
//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);


    }
}
