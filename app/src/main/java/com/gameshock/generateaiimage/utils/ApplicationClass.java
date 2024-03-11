package com.gameshock.generateaiimage.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gameshock.generateaiimage.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.google.android.material.snackbar.Snackbar;

public class ApplicationClass extends Application {

    public static InterstitialAd mInterstitialAd;
    public static RewardedAd mRewardedAd;


    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


    }



    //Interstitial Ad
    public static void showInterstitialAd(Activity activity) {

        mInterstitialAd.show(activity);
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
//                Log.d("TAGAd", "Ad was clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {

                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
//                Log.d("TAGAd", "Ad dismissed fullscreen content.");


                mInterstitialAd = null;

                loadInterstitialAd(activity);

            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                // Called when ad fails to show.
                mInterstitialAd = null;
                loadInterstitialAd(activity);
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
//                Log.d("TAGAd", "Ad recorded an impression.");

            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
//                Log.d("TAGAd", "Ad showed fullscreen content.");
            }
        });
    }

    public static void loadInterstitialAd(Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, activity.getString(R.string.iid), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                Log.d("TAGAd", loadAdError.toString());
                mInterstitialAd = null;

            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
//                Log.i("TAGAd", "onAdLoaded");
            }
        });
    }
}