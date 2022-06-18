package com.example.demoactivity.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class PostImage {
//    private final static String SERVICE="159.75.246.129:8080/predict";
    private static boolean flag=false;
    public static String doJsonPost(URL url, String jsonstr) {
        HttpURLConnection urlConnection = null;
        System.out.println("tag"+"这只从客户端得到的数据"+jsonstr);

        try {

            urlConnection = (HttpURLConnection) url.openConnection();//打开http连接
            urlConnection.setConnectTimeout(8000);//连接的超时时间
            urlConnection.setUseCaches(false);//不使用缓存
            //urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
            urlConnection.setInstanceFollowRedirects(true);//是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
            urlConnection.setReadTimeout(8000);//响应的超时时间
            urlConnection.setDoInput(true);//设置这个连接是否可以写入数据
            urlConnection.setDoOutput(true);//设置这个连接是否可以输出数据
            urlConnection.setRequestMethod("POST" );//设置请求的方式
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置消息的类型
            urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接

            OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))) {
                bw.write(jsonstr);//把json字符串写入缓冲区中
                bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
                out.close();
                bw.close();//使用完关闭
            }//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
            if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){//得到服务端的返回码是否连接成功

                InputStream in = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }catch (Exception e) {
        }finally{
            if(urlConnection!=null){

                urlConnection.disconnect();//使用完关闭TCP连接，释放资源
            }
            else {
                Log.e("postimage","tcp failed!");
            }
        }
        return null;
    }
    public static String bitmapToBase64(Bitmap bitmap) {
        // 要返回的字符串
        String reslut = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                /**
                 * 压缩只对保存有效果bitmap还是原来的大小
                 */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                baos.flush();
                // 转换为字节数组
                byte[] byteArray = baos.toByteArray();

                // 转换为字符串
                reslut = Base64.encodeToString(byteArray, Base64.DEFAULT);
                baos.close();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {

            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return reslut;
    }
}
