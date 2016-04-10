package com.xushuzhan.downloadpicturetest;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private int mImageName;
    private Bitmap mBitmap;
    ImageView picture;
    Button button;
    String mSaveMessage;
    private final static String PICTURE_PATH = Environment.getExternalStorageDirectory() + "/MyDownloadTest/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //测试的URL
        URL Turl = null;
        try {
            Turl = new URL("http://static.wgpet.com/editor/attached/image/20141126/20141126213121_26063.jpg");

        } catch (MalformedURLException e) {
            e.getMessage();
        }
        Turl.getHost().hashCode();
        System.out.println("这是测试连接的URL的hash码"+Turl.getHost().hashCode());

       String savedFileName[]=getHashcode().clone();
        if (savedFileName.length == 0) {
            Thread run = new Thread(getUrlRunnable);
            run.start();
        } else {
            for (int i = 0; i < savedFileName.length; i++) {
                if (String.valueOf(Turl.getHost().hashCode()).equals(savedFileName[i])) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(PICTURE_PATH + savedFileName[i]);
                    } catch (FileNotFoundException e) {
                        e.getMessage();
                    }
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    picture.setImageBitmap(bitmap);
                    System.out.println("本地图片》》》》》》》》》》》》》》》》》》》》》》》");
                } else {
                    System.out.println("本地没有这张图片23333333333333333333333333333333333");
                    Thread run = new Thread(getUrlRunnable);
                    run.start();
                }
            }
        }
    }

    private Runnable getUrlRunnable =new Runnable() {

        @Override
        public void run() {
            try {
                URL url = new URL("http://static.wgpet.com/editor/attached/image/20141126/20141126213121_26063.jpg");
                url.getHost().hashCode();
                mImageName = url.getHost().hashCode();
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10*1000);
                conn.setRequestMethod("GET");
                if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                    //从取得的inputstream生成bitmap
                    mBitmap = BitmapFactory.decodeStream(conn.getInputStream());
                }
                //让hanlder发送消息，通知UI更新
                connectHanlder.sendEmptyMessage(0x123);

            } catch (IOException e) {
                Toast.makeText(MainActivity.this,"无法连接网络",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    };
    private  String[] getHashcode(){
        File file =new File(PICTURE_PATH);
        String pictureList[] =file.list();
        return pictureList;
    }


    private Handler connectHanlder = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //显示图片
            if(msg.what==0x123){
                picture.setImageBitmap(mBitmap);
            }else {
                Toast.makeText(MainActivity.this,"图片正在加载...",Toast.LENGTH_LONG);
            }
        }
    };


    //保存图片的线程
    private Runnable downloadFileRunnable =new Runnable() {
        @Override
        public void run() {
            try {
                saveImage(mBitmap, String.valueOf(mImageName));
                mSaveMessage = "图片保存成功";
            } catch (IOException e) {
                mSaveMessage ="图片保存失败";
                e.printStackTrace();
            }
        }
    };
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, mSaveMessage,
                    Toast.LENGTH_SHORT).show();
        }
    };
    private void initView() {
        picture= (ImageView) findViewById(R.id.picture_test);
        button= (Button) findViewById(R.id.save_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread saveThread=new Thread(saveImageRunnable);
                saveThread.start();
            }
        });
    }

    private Runnable saveImageRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                saveImage(mBitmap, String.valueOf(mImageName));
                mSaveMessage = "图片保存成功！";
            } catch (Exception e) {
                mSaveMessage = "图片保存失败！";
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    };

    //从网络获取图片，并且返回一个输入流
    protected InputStream getImageStream(String path) throws IOException{
        URL url = new URL(path);
        url.getHost().hashCode();
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10*1000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
            return conn.getInputStream();
        }
            return  null;
    }
    //保存文件
    protected void saveImage(Bitmap bitmap,String imageName) throws IOException{
        File dirFile=new File(PICTURE_PATH);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }
        File myFile=new File (PICTURE_PATH+imageName);
        BufferedOutputStream bos=new BufferedOutputStream(
                new FileOutputStream(myFile));
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,bos);
        bos.flush();
        bos.close();
    }
}
