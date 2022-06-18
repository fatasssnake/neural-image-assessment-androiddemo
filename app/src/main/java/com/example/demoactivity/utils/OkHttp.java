package com.example.demoactivity.utils;

import android.util.Log;
import android.widget.TextView;


import com.example.demoactivity.view.WaveProgressView;
import com.google.gson.Gson;

import org.chromium.net.NetworkException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttp {
    public static final String TAG = "Okhttp";
    private final OkHttpClient client = new OkHttpClient();
    private static boolean flag=false;
    private static float score;



    public String postImage(String url, File file) throws NetworkException {
        MediaType type = MediaType.parse("image/jpeg");
        RequestBody fileBody = RequestBody.create(type, file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "connected failed!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    Log.d(TAG, "没有得到回复!");
                    response.close();
                }
                String result = response.body().string();
                Log.e(TAG, "result=" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String s =jsonObject.getString("pred");
                    score=Float.parseFloat(s);
                    Log.e(TAG,score+"");
                    flag=true;
//                    waveProgress.setTextView(progressText);
//                    waveProgress.setOnAnimationListener(new WaveProgressView.OnAnimationListener() {
//                        @Override
//                        public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
//                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
//                            String s = decimalFormat.format(interpolatedTime * updateNum / maxNum*10) + "分";
//                            return s;
//                        }
//
//                        @Override
//                        public float howToChangeWaveHeight(float percent, float waveHeight) {
//                            return 0;
//                        }
//                    });
////        waveProgress.setProgressNum(maxScore, 1500);
//                    waveProgress.setProgressNum(score*10, 1500);

                } catch (JSONException e) {
                    e.printStackTrace();
                }




//                waveProgress.setTextView(progressText);
//                waveProgress.setOnAnimationListener(new WaveProgressView.OnAnimationListener() {
//                    @Override
//                    public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
//                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
//                        String s = decimalFormat.format(interpolatedTime * updateNum / maxNum * 10) + "分";
//                        return s;
//                    }
//
//                    @Override
//                    public float howToChangeWaveHeight(float percent, float waveHeight) {
//                        return 0;
//                    }
//                });
////        waveProgress.setProgressNum(maxScore, 1500);
//                waveProgress.setProgressNum(maxScore * 10, 1500);
            }
        });
        return null;
    }

    public boolean getFlag() {
        return flag;
    }

    public float getScore() {
        return score;
    }

    public void setFlag(boolean b) {
        flag=b;
    }
}
