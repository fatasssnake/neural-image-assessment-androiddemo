package com.example.demoactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.demoactivity.utils.BitmapRound;

public class IntroductionActivity extends AppCompatActivity {
    public final String TAG="introductionActivity!";

    private ImageView imageView;
    private Button button;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        applyForSinglePermission();
        imageView=findViewById(R.id.changtu);
        Animation animation=new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,
                0.0f,
                Animation.RELATIVE_TO_PARENT,
                -1.0f,
                Animation.RELATIVE_TO_PARENT,
                0.0f,
                Animation.RELATIVE_TO_PARENT,
                0.0f
        );
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(20000);
        animation.setRepeatCount(Animation.INFINITE);
        imageView.setAnimation(animation);
        button=findViewById(R.id.start);
        button.setOnClickListener(view -> {
            Intent intent=new Intent(IntroductionActivity.this,MainActivity.class);
            startActivity(intent);
        });
    }
    private String PM_SINGLE=Manifest.permission.READ_EXTERNAL_STORAGE;
    //申请单个权限
    public void applyForSinglePermission(){
        Log.i(TAG,"applyForSinglePermission");
        try{
            //如果操作系统SDK级别在23之上（android6.0），就进行动态权限申请
            if(Build.VERSION.SDK_INT>=23){
                //判断是否拥有权限
                int nRet=ContextCompat.checkSelfPermission(this,PM_SINGLE);
                Log.i(TAG,"checkSelfPermission nRet="+nRet);
                if(nRet!= PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG,"进行权限申请...");
                    ActivityCompat.requestPermissions(this,new String[]{PM_SINGLE},10000);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }



}