package com.example.demoactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoactivity.utils.BitmapRound;
import com.example.demoactivity.utils.OkHttp;
import com.example.demoactivity.utils.PostImage;
import com.example.demoactivity.view.WaveProgressView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.chromium.net.NetworkException;
import org.pytorch.IValue;
//import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "fatasssnake";
    private Button select;
    private Button evaluate;
    private ImageView imageView;
    private WaveProgressView waveProgress;
    private TextView progressText;
    private ImageView mIvScan;
    private Button reset;
    Bitmap image;
    Animation mTop2Bottom, mBottom2Top;
    private final int IMAGE_REQUEST_CODE = 1;
    private String imagePath;
    private Module module;

    private boolean isPictureSelect = false;
    private boolean isEva = false;

    private final float INDEX=100;

    //POST
    private final String url="http://159.75.246.129:8080/predict";




//    private String[] image_test={
//            "7114.jpg",
//            "11242.jpg",
//            "26212.jpg"
//    };



    private void init(){
        select = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        evaluate = findViewById(R.id.evaluate);
        waveProgress = findViewById(R.id.wave_progress);
        progressText = findViewById(R.id.progress_text);
        mIvScan = findViewById(R.id.scan_line);
        reset = findViewById(R.id.reset);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        String temp=assetFilePath(this,"mobilenet_v2.pt");
//        String temp=assetFilePath(this,"AlexNet_model.pt");
//        String temp=assetFilePath(this,"AlexNet_model.ptl");
//        String temp=assetFilePath(this,"yolov5s.torchscript.ptl");
        Log.e(TAG,temp);
//        module= LiteModuleLoader.load(temp);
        module= Module.load(temp);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在这里跳转到手机系统相册里面
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });
        evaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEva) {
                    Toast.makeText(MainActivity.this, "已评估！", Toast.LENGTH_SHORT).show();
                } else {
                    if (isPictureSelect) {
                        Log.e(TAG, "image has loaded");
                        startScan();

//                        evaluateImage();
//                        displayImage(image);
                        httpEvaluate();


                    } else {
                        Toast.makeText(MainActivity.this, "没选择图片", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(R.drawable.empty);
                isPictureSelect = false;
                waveProgress.setProgressNum(0, 0);
                progressText.setText("");
            }
        });

    }

    private void httpEvaluate() {
        Log.e(TAG, "httpEvaluate: post!");
        OkHttp test=new OkHttp();
        File file=new File(imagePath);
        long startTime=System.currentTimeMillis();
        try {
            test.postImage(url,file);
        } catch (NetworkException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            while(!test.getFlag()){
            }
            float score=test.getScore();
            Log.e(TAG,System.currentTimeMillis()-startTime+"ms");
            Log.e(TAG,score+"");
            test.setFlag(false);
            waveProgress.setTextView(progressText);
            waveProgress.setOnAnimationListener(new WaveProgressView.OnAnimationListener() {
                @Override
                public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String s = decimalFormat.format(interpolatedTime * updateNum / maxNum*10) + "分";
                    return s;
                }

                @Override
                public float howToChangeWaveHeight(float percent, float waveHeight) {
                    return 0;
                }
            });
//        waveProgress.setProgressNum(maxScore, 1500);
            waveProgress.setProgressNum(score*10, 1500);
            isEva = true;

        }).start();

    }

    private void evaluateImage(){
        if(image==null){
            Toast.makeText(MainActivity.this,"没有图片！",Toast.LENGTH_SHORT);
            return;
        }
        long cur=System.currentTimeMillis();


        //testing
//        try {
//            image=BitmapFactory.decodeStream(getAssets().open(image_test[1]));
//            image=imageScale(image,224,224);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        // preparing input tensor
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(image,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB );

        // running the model
        final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

        // getting tensor content as java array of floats
        final float[] scores = outputTensor.getDataAsFloatArray();

        // searching for the index with maximum score
//        float maxScore = scores[0];
        float maxScore =Float.MIN_VALUE;
        int maxScoreIdx = -1;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]%7;
                maxScoreIdx = i;
            }
        }
//        maxScore=maxScore*3/7+3;
        Log.e(TAG,String.valueOf(maxScore));
        Log.e(TAG,String.valueOf(System.currentTimeMillis()-cur)+"ms");
//        Log.e(TAG,String.valueOf(scores[0]));
//
//        for (int i = 0; i < image_test.length; i++) {
//            try {
//                Bitmap imageT=BitmapFactory.decodeStream(getAssets().open(image_test[i]));
//
//                // preparing input tensor
//                Tensor in = TensorImageUtils.bitmapToFloat32Tensor(image,
//                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB );
//
//                // running the model
//                Tensor out = module.forward(IValue.from(in)).toTensor();
//
//                // getting tensor content as java array of floats
//                float[] sco = outputTensor.getDataAsFloatArray();
//
//                // searching for the index with maximum score
//                float res = scores[0];
//                Log.e(TAG,image_test[i]+":  "+res);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }


        waveProgress.setTextView(progressText);
        waveProgress.setOnAnimationListener(new WaveProgressView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String s = decimalFormat.format(interpolatedTime * updateNum / maxNum*10) + "分";
                return s;
            }

            @Override
            public float howToChangeWaveHeight(float percent, float waveHeight) {
                return 0;
            }
        });
//        waveProgress.setProgressNum(maxScore, 1500);
        waveProgress.setProgressNum(maxScore*10, 1500);
    }

    public String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error process asset " + assetName + " to file path");
        }
        return null;
    }

    private void startScan() {
//        mIvScan.setVisibility(View.VISIBLE);
//        boolean flag = true;
        mIvScan.setImageResource(R.drawable.icon_scan_line);
        mTop2Bottom = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.5f);

        mBottom2Top = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.5f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f);

//        mBottom2Top.setRepeatMode(Animation.ABSOLUTE);
        mBottom2Top.setInterpolator(new LinearInterpolator());
        mBottom2Top.setDuration(500);
        mBottom2Top.setFillEnabled(true);//使其可以填充效果从而不回到原地
        mBottom2Top.setFillAfter(true);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        mBottom2Top.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                    mIvScan.startAnimation(mTop2Bottom);
                mIvScan.setImageBitmap(null);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

//        mTop2Bottom.setRepeatMode(Animation.ABSOLUTE);
        mTop2Bottom.setInterpolator(new LinearInterpolator());
        mTop2Bottom.setDuration(500);
        mTop2Bottom.setFillEnabled(true);
        mTop2Bottom.setFillAfter(true);
        mTop2Bottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mIvScan.startAnimation(mBottom2Top);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mIvScan.startAnimation(mTop2Bottom);

    }
    public Bitmap imageScale(Bitmap bitmap,int new_w,int new_h){
        int src_w=bitmap.getWidth();
        int src_h=bitmap.getHeight();
        float scale_w=((float) new_w)/src_w;
        float scale_h=((float) new_h)/src_h;
        Matrix matrix=new Matrix();
        matrix.postScale(scale_w,scale_h);
        Bitmap bihuanbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,true);
        return bihuanbmp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //在相册里面选择好相片之后调回到现在的这个activity中
        switch (requestCode) {
            case IMAGE_REQUEST_CODE://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
                if (resultCode == RESULT_OK) {//resultcode是setResult里面设置的code值
                    try {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imagePath = cursor.getString(columnIndex);  //获取照片路径
                        cursor.close();
                        Bitmap bitmap;
                        bitmap=BitmapFactory.decodeFile(imagePath);
                        if(bitmap==null){
                            Toast.makeText(MainActivity.this,"读取图片失败！",Toast.LENGTH_LONG);
                        }

                        image = bitmap;
                        imageView.setImageBitmap(BitmapRound.bimapRound(bitmap,INDEX));
//                        image=imageScale(bitmap,224,224);
                        isPictureSelect = true;
                        isEva = false;
                        waveProgress.setProgressNum(0, 0);
                        progressText.setText("待评分");
                    } catch (Exception e) {
                        // TODO Auto-generatedcatch block
                        e.printStackTrace();
                    }
                }
                break;
        }


    }


}