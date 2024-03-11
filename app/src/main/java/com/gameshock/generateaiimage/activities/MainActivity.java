package com.gameshock.generateaiimage.activities;

import static com.gameshock.generateaiimage.utils.Const.adUnitIdRewarded;
import static com.gameshock.generateaiimage.utils.Const.testMode;
import static com.gameshock.generateaiimage.utils.Const.unityGameID;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gameshock.generateaiimage.R;
import com.gameshock.generateaiimage.utils.ApplicationClass;
import com.gameshock.generateaiimage.utils.Const;
import com.gameshock.generateaiimage.adapter.ImageAdapter;
import com.gameshock.generateaiimage.databinding.ActivityMainBinding;
import com.gameshock.generateaiimage.model.GeneratedImage;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.google.android.material.snackbar.Snackbar;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityMainBinding binding;

    //
    String genID;


    //
    //List<String> imageUrls;
    List<GeneratedImage> imageUrls;
    ImageAdapter imageAdapter;

    //data
    String modelId, presetStyle;

    //
    String negativePrompt = "";
    int height = 512;
    int width = 512;
    //rewarded ad unity

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 123;

    InterstitialAd mInterstitialAd;
    AdRequest adRequest;

    public static RewardedAd mRewardedAd;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with your operations
                binding.adLayout.setVisibility(View.VISIBLE);
//                if (loadListenerRewarded != null) {
//                    UnityAds.load(adUnitIdRewarded, loadListenerRewarded);
//                }
                ShowRewardedAdandGenerateResponse();

            } else {

                // Permission denied, handle this situation (e.g., show a message or request again)
                Snackbar s = Snackbar.make(binding.mainLayout, "To save the generated images to your phone, we kindly request you to give permission!", Snackbar.LENGTH_SHORT);
                s.show();

            }
        }
    }

    private void ShowRewardedAdandGenerateResponse() {
        if (mRewardedAd != null) {
            mRewardedAd.show(this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
//                    Log.d("TAGr", "The user earned the reward.");

                    binding.adLayout.setVisibility(View.GONE);
                    binding.loadingLayout.setVisibility(View.VISIBLE);

                    callAPIForGenerateID();


                    if (ApplicationClass.mRewardedAd == null) {
                        loadAd();
                    }
                }
            });
        } else {
            Snackbar.make(binding.mainLayout, "Ad is not available now, try again!", 1500).show();
            binding.adLayout.setVisibility(View.GONE);
            loadAd();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        //selected text color change
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //text color change
                if (binding.modelSpinner.getChildAt(0) != null) {
                    ((TextView) binding.modelSpinner.getChildAt(0)).setTextColor(Color.WHITE);
                }
                //selected text color change
                if (binding.styleSpinner.getChildAt(0) != null) {
                    ((TextView) binding.styleSpinner.getChildAt(0)).setTextColor(Color.WHITE);

                }
            }
        }, 1000);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adRequest = new AdRequest.Builder().build();


        //load interstitial
        loadAd();

        //backPressed
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                exitPopup();

            }
        });
        //
        //Ads init

        // Set up the RecyclerView
        binding.rvImgs.setLayoutManager(new LinearLayoutManager(this));

        //ratio
        binding.ratioOnebyone.setOnClickListener(this);
        binding.ratioFourbythree.setOnClickListener(this);
        binding.ratioSixteenbynine.setOnClickListener(this);
        binding.ratioThreebytwo.setOnClickListener(this);
        binding.ratioTwobythree.setOnClickListener(this);

        //
        // Data for the model spinner
        List<String> modelSpinnerItems = new ArrayList<>();
        modelSpinnerItems.add("Dream Shaper");
        modelSpinnerItems.add("Absolute Reality");
        modelSpinnerItems.add("Stable Diffusion");

        // Create an ArrayAdapter using the default spinner layout
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                modelSpinnerItems
        );
        // Set the dropdown layout style
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.modelSpinner.setAdapter(modelAdapter);
        // Set an item selected listener for the spinner
        binding.modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                // Handle the selected item here
                String selectedValue = modelSpinnerItems.get(position);
                if (Objects.equals(selectedValue, "Dream Shaper")) {
                    modelId = "ac614f96-1082-45bf-be9d-757f2d31c174";
                } else if (Objects.equals(selectedValue, "Absolute Reality")) {
                    modelId = "e316348f-7773-490e-adcd-46757c738eb7";
                } else if (Objects.equals(selectedValue, "Stable Diffusion")) {
                    modelId = "b820ea11-02bf-4652-97ae-9ac0cc00593d";
                }
//                //selected text color change
                if (selectedItemView != null) {
                    ((TextView) selectedItemView).setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        //
        // Data for the style spinner
        List<String> styleSpinnerItems = new ArrayList<>();
        styleSpinnerItems.add("Leonardo");
        styleSpinnerItems.add("None");


        // Create an ArrayAdapter using the default spinner layout
        ArrayAdapter<String> styleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                styleSpinnerItems
        );
        // Set the dropdown layout style
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.styleSpinner.setAdapter(styleAdapter);
        // Set an item selected listener for the spinner
        binding.styleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selected item here
                String selectedValue = styleSpinnerItems.get(position);
                //selected value will be saved
                if (Objects.equals(selectedValue, "Leonardo")) {
                    presetStyle = "LEONARDO";
                } else if (Objects.equals(selectedValue, "None")) {
                    presetStyle = null;
                }
                //selected text color change
                if (selectedItemView != null) {
                    ((TextView) selectedItemView).setTextColor(Color.WHITE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        //
        //back_result
        binding.icBackResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.resultScreen.setVisibility(View.GONE);
            }
        });
        //cls
        binding.ideaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                binding.promptText.setText("");
                final DialogPlus dialogPlus = DialogPlus.newDialog(MainActivity.this)
                        .setContentHolder(new ViewHolder(R.layout.idea_popup))
                        .setExpanded(false)
                        .setGravity(Gravity.CENTER)
                        .setContentBackgroundResource(R.color.transparent)
                        .create();


                dialogPlus.show();
            }
        });
        //advance settings
        binding.advanceSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(MainActivity.this)
                        .setContentHolder(new ViewHolder(R.layout.advance_settings))
                        .setExpanded(false)
                        .setGravity(Gravity.BOTTOM)
                        .setContentBackgroundResource(R.color.transparent)
                        .create();
                View v = dialogPlus.getHolderView();

                CardView save_btn = v.findViewById(R.id.saveBtn);
                EditText negPrompt = v.findViewById(R.id.prompt_text);
                save_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (negPrompt.getText().toString().equals("")) {
                            negativePrompt = "";
                        } else {
                            negativePrompt = negPrompt.getText().toString();
                        }
                        dialogPlus.dismiss();
                    }
                });

                dialogPlus.show();
            }
        });
        //microphone btn
        binding.microphoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    someActivityResultLauncher.launch(intent);
//                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT1);
                } catch (Exception e) {
//                    Toast.makeText(MainActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Snackbar.make(binding.mainLayout, " " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        //

        binding.generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.promptText.getText().toString().equals("")) {
//                    Toast.makeText(MainActivity.this, "Enter the prompt!", Toast.LENGTH_SHORT).show();
                    Snackbar.make(binding.mainLayout, "Enter the prompt!", Snackbar.LENGTH_SHORT).show();

                } else {
                    if (Const.InternetConnection.checkConnection(MainActivity.this)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            binding.adLayout.setVisibility(View.VISIBLE);
//                            if (loadListenerRewarded != null) {
//                                UnityAds.load(adUnitIdRewarded, loadListenerRewarded);
//                            }
                            ShowRewardedAdandGenerateResponse();

                        }// Check for runtime permissions on Android 6.0 and higher
                        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Check if the permission is not granted
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                // Request the permission
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        STORAGE_PERMISSION_REQUEST_CODE);
                            } else {
                                // Permission is already granted, you can proceed with your operations
                                binding.adLayout.setVisibility(View.VISIBLE);
//                                if (loadListenerRewarded != null) {
//                                    UnityAds.load(adUnitIdRewarded, loadListenerRewarded);
//                                }
                                ShowRewardedAdandGenerateResponse();

                            }
                        } else {
                            // Runtime permissions not required for devices below Android 6.0
                            // You can proceed with your operations
                            binding.adLayout.setVisibility(View.VISIBLE);
//                            if (loadListenerRewarded != null) {
//                                UnityAds.load(adUnitIdRewarded, loadListenerRewarded);
//                            }
                            ShowRewardedAdandGenerateResponse();

                        }

                    } else {
                        Snackbar.make(binding.mainLayout, "Check your internet connection", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //Rewarded Ad
    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.rrd),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("TAGr", loadAdError.toString());
                        binding.adLayout.setVisibility(View.GONE);

                        Snackbar.make(binding.mainLayout, "Ad is not available now, try again!", 1200).show();

                        mRewardedAd = null;
                        loadAd();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
//                        Log.d("TAGr", "Ad was loaded.");

                        ServerSideVerificationOptions options = new ServerSideVerificationOptions
                                .Builder()
                                .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                                .build();
                        rewardedAd.setServerSideVerificationOptions(options);

                        binding.adLayout.setVisibility(View.GONE);

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
//                                Log.d("TAGr", "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
//                                Log.d("TAGr", "Ad dismissed fullscreen content.");
                                mRewardedAd = null;
                                loadAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e("TAGr", "Ad failed to show fullscreen content." + adError.toString());
                                mRewardedAd = null;
                                loadAd();
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
//                                Log.d("TAGr", "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
//                                Log.d("TAGr", "Ad showed fullscreen content.");


                            }
                        });

                    }
                });
    }


    private void exitPopup() {
        final DialogPlus dialogPlus = DialogPlus.newDialog(MainActivity.this)
                .setContentHolder(new ViewHolder(R.layout.exit_popup))
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .create();
        View v = dialogPlus.getHolderView();
//
        CardView yes_btn = v.findViewById(R.id.yes_btn);
        CardView no_btn = v.findViewById(R.id.no_btn);

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPlus.dismiss();
            }
        });


        dialogPlus.show();
    }

    private void callAPIForGenerateID() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject jsonBody = new JSONObject();
        try {

            jsonBody.put("prompt", binding.promptText.getText().toString());
            jsonBody.put("modelId", modelId);
            jsonBody.put("height", height);
            jsonBody.put("width", width);
            jsonBody.put("presetStyle", presetStyle);
            jsonBody.put("promptMagic", true);
            jsonBody.put("num_images", 2);
            jsonBody.put("guidance_scale", 7);
            jsonBody.put("negative_prompt", negativePrompt);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Const.gurl, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("TAGr", "onResponse: " + response.toString());

                    try {

                        JSONObject jo = response.getJSONObject("sdGenerationJob");

                        genID = jo.getString("generationId");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.waitText.setText("Analysing...");
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.waitText.setText("Generating...");

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        callAPIForGeneratePics(genID);
                                                    }
                                                }, 13000);
                                            }
                                        }, 5000);
                                    }
                                }, 2000);
                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                        binding.loadingLayout.setVisibility(View.GONE);
                        binding.waitText.setText("Please wait...");

                    }

                }
            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {


                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        // Simulate a 400 status code error response with a specific JSON error message
                        handle400ErrorResponse(error);
                    } else {
                        // Handle other types of errors
                        Snackbar.make(binding.mainLayout, "Error", Snackbar.LENGTH_SHORT).show();
                    }
                    binding.generateBtn.setEnabled(true);
                    binding.loadingLayout.setVisibility(View.GONE);
                    binding.waitText.setText("Please wait...");

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
//                    header.put("Accept", "application/json");
                    header.put("authorization", "Bearer " + Const.ak);
                    return header;
                }
            };

            request.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 100000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 100000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    Log.d("TAGr", "retry: " + error.getMessage());
                    binding.loadingLayout.setVisibility(View.GONE);
                    binding.waitText.setText("Please wait...");
                }
            });
            MyRequestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
            binding.loadingLayout.setVisibility(View.GONE);
            binding.waitText.setText("Please wait...");
        }
    }

    private void handle400ErrorResponse(VolleyError error) {
        String errorMessage = getErrorMessage(error);
        // Display or log the error message
        // For example, if you want to get the error message like {"error":"content moderation filter","path":"$","code":"unexpected"}
        // you can use a JSON library to parse it, or use a simple string matching approach
        if (errorMessage != null && errorMessage.contains("content moderation filter")) {
            // Handle the specific error message
            Snackbar.make(binding.mainLayout, "Content moderation filter", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(binding.mainLayout, "Error: status code 400", Snackbar.LENGTH_SHORT).show();
        }
    }

    private String getErrorMessage(VolleyError error) {

        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                return new String(error.networkResponse.data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void callAPIForGeneratePics(String genID) {

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        imageUrls = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, imageUrls); // replace with your data
        binding.rvImgs.setAdapter(imageAdapter);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest("https://cloud.leonardo.ai/api/rest/v1/generations/" + genID, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {


                // Parsing json
                try {
                    Log.d("TAGrrr", "onResponse: " + response.toString());
                    JSONObject generationsByPk = response.getJSONObject("generations_by_pk");
                    JSONArray generatedImages = generationsByPk.getJSONArray("generated_images");


                    for (int i = 0; i < generatedImages.length(); i++) {
                        JSONObject imageObject = generatedImages.getJSONObject(i);
                        String imageUrl = imageObject.getString("url");
                        imageUrls.add(new GeneratedImage(imageUrl));

                    }
                    imageAdapter.notifyDataSetChanged();
                    binding.loadingLayout.setVisibility(View.GONE);
                    binding.waitText.setText("Please wait...");
                    binding.resultScreen.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    binding.loadingLayout.setVisibility(View.GONE);
                    binding.waitText.setText("Please wait...");
                }


                // notifying list adapter about data changes
                // so that it renders the list view with updated data

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // hidePDialog();
                binding.loadingLayout.setVisibility(View.GONE);
                binding.waitText.setText("Please wait...");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("content-type", "application/json");
                headers.put("authorization", "Bearer " + Const.ak);
                return headers;
            }
        };
        // Adding request to request queue
        requestQueue.add(jsonArrayRequest);

    }


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        if (result.getData() != null) {
                            ArrayList<String> r = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            binding.promptText.setText(Objects.requireNonNull(r).get(0));
                        }
                    }
                }
            });

    @Override
    public void onClick(View view) {
        if (view == binding.ratioOnebyone) {
            binding.ratioOnebyone.setImageResource(R.drawable.onebyone_selected);
            binding.ratioTwobythree.setImageResource(R.drawable.twobythree_unselected);
            binding.ratioFourbythree.setImageResource(R.drawable.fourbythree_unselected);
            binding.ratioThreebytwo.setImageResource(R.drawable.threebytwo_unselected);
            binding.ratioSixteenbynine.setImageResource(R.drawable.sixteenbynine_unselected);
            //
            width = 512;
            height = 512;
            Snackbar.make(binding.ratioOnebyone, "1:1 ratio selected", Snackbar.LENGTH_SHORT).show();
        } else if (view == binding.ratioFourbythree) {
            binding.ratioOnebyone.setImageResource(R.drawable.onebyone_unselected);
            binding.ratioTwobythree.setImageResource(R.drawable.twobythree_unselected);
            binding.ratioFourbythree.setImageResource(R.drawable.fourbythree_selected);
            binding.ratioThreebytwo.setImageResource(R.drawable.threebytwo_unselected);
            binding.ratioSixteenbynine.setImageResource(R.drawable.sixteenbynine_unselected);
            //
            width = 688;
            height = 512;
            Snackbar.make(binding.ratioFourbythree, "4:3 ratio selected", Snackbar.LENGTH_SHORT).show();
        } else if (view == binding.ratioThreebytwo) {
            binding.ratioOnebyone.setImageResource(R.drawable.onebyone_unselected);
            binding.ratioTwobythree.setImageResource(R.drawable.twobythree_unselected);
            binding.ratioFourbythree.setImageResource(R.drawable.fourbythree_unselected);
            binding.ratioThreebytwo.setImageResource(R.drawable.threebytwo_selected);
            binding.ratioSixteenbynine.setImageResource(R.drawable.sixteenbynine_unselected);
            //
            width = 768;
            height = 512;
            Snackbar.make(binding.ratioThreebytwo, "3:2 ratio selected", Snackbar.LENGTH_SHORT).show();
        } else if (view == binding.ratioTwobythree) {
            binding.ratioOnebyone.setImageResource(R.drawable.onebyone_unselected);
            binding.ratioTwobythree.setImageResource(R.drawable.twobythree_selected);
            binding.ratioFourbythree.setImageResource(R.drawable.fourbythree_unselected);
            binding.ratioThreebytwo.setImageResource(R.drawable.threebytwo_unselected);
            binding.ratioSixteenbynine.setImageResource(R.drawable.sixteenbynine_unselected);
            //
            width = 512;
            height = 768;
            Snackbar.make(binding.ratioTwobythree, "2:3 ratio selected", Snackbar.LENGTH_SHORT).show();

        } else if (view == binding.ratioSixteenbynine) {
            binding.ratioOnebyone.setImageResource(R.drawable.onebyone_unselected);
            binding.ratioTwobythree.setImageResource(R.drawable.twobythree_unselected);
            binding.ratioFourbythree.setImageResource(R.drawable.fourbythree_unselected);
            binding.ratioThreebytwo.setImageResource(R.drawable.threebytwo_unselected);
            binding.ratioSixteenbynine.setImageResource(R.drawable.sixteenbyninteen_selected);
            //
            width = 912;
            height = 512;
            Snackbar.make(binding.ratioSixteenbynine, "16:9 ratio selected", Snackbar.LENGTH_SHORT).show();
        }
    }
}