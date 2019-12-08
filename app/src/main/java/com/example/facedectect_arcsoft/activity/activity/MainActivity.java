package com.example.facedectect_arcsoft.activity.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.example.facedectect_arcsoft.R;
import com.example.facedectect_arcsoft.activity.faceserver.CompareResult;
import com.example.facedectect_arcsoft.activity.faceserver.FaceServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.arcsoft.face.FaceEngine.CP_PAF_BGR24;
import static com.example.facedectect_arcsoft.activity.drawer.ImageProcess.DrawRect;
import static com.example.facedectect_arcsoft.activity.drawer.ImageProcess.bitmapToBgr;
import static com.example.facedectect_arcsoft.activity.drawer.ImageProcess.readPictureDegree;
import static com.example.facedectect_arcsoft.activity.drawer.ImageProcess.toturn;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "TAG";
    private static int REQ_1 = 1;
    private static int REQ_2 = 2;
    private static ImageView mImageView;
    private String mFilePath;
    private Toast toast = null;
    //ArcSoft
    private static FaceEngine mFaceEngine;
    private static List<FaceInfo> mFaceInfoList;
    //handler
    private Handler mhandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView)findViewById(R.id.imageView);
        mFilePath = Environment.getExternalStorageDirectory().getPath();
        mFilePath = mFilePath + "/" + "temp.jpg";
        mFaceEngine = new FaceEngine();
        mFaceInfoList = new ArrayList<>();
        initEngine();
        FaceServer.getInstance().init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceServer.getInstance().unInit();
        if(mFaceEngine != null){
            mFaceEngine.unInit();
        }
        if(mFaceInfoList != null){
            mFaceInfoList.clear();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.AddFace:
                startActivity(new Intent(this, FaceAddActivity.class));
                return true;
            case R.id.FaceManage:
                startActivity(new Intent(this, FaceManageActivity.class));
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void detectFace(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = Uri.fromFile(new File(mFilePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQ_1);
    }

    public void FaceRecognize(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = Uri.fromFile(new File(mFilePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQ_2);
    }

    public void activeMachine(View view){
        activeEngine();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQ_1){
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        detectFace();
                    }
                });
            }
            else if(requestCode == REQ_2){
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        RecognizeFace();
                    }
                });
            }
        }
    }

    void RecognizeFace(){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mFilePath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            bitmap = toturn(bitmap, readPictureDegree(mFilePath));
            Bitmap tempBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            byte[] bgr24 = bitmapToBgr(bitmap);
            //byte[] NV21 = bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
            List<FaceInfo> FaceInfoList = new ArrayList<>();
            CompareResult compareResult = null;
            int detectCode = mFaceEngine.detectFaces(bgr24, bitmap.getWidth(), bitmap.getHeight(), CP_PAF_BGR24, FaceInfoList);
            if(detectCode == ErrorInfo.MOK && FaceInfoList.size() > 0){
                FaceFeature faceFeature = new FaceFeature();
                //2.特征提取
                detectCode = mFaceEngine.extractFaceFeature(bgr24, bitmap.getWidth(), bitmap.getHeight(), FaceEngine.CP_PAF_BGR24, FaceInfoList.get(0), faceFeature);
                if(detectCode == ErrorInfo.MOK){
                    compareResult = FaceServer.getInstance().getTopOfFaceLib(faceFeature);
                    List<Rect> rectList = new ArrayList<>();
                    for(FaceInfo faceInfo : FaceInfoList){
                        rectList.add(faceInfo.getRect());
                    }
                    tempBitmap = DrawRect(tempBitmap, rectList, compareResult.getUserName());
                }else{
                    Log.d(TAG, "Face recognize unsuccessfully" + detectCode);
                }
            }else{
                Log.d(TAG, "Face detect unsuccessfully" + detectCode);
            }
            mImageView.setImageBitmap(tempBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void detectFace(){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mFilePath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            bitmap = toturn(bitmap, readPictureDegree(mFilePath));
            byte[] bgr24 = bitmapToBgr(bitmap);
            //byte[] NV21 = bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
            long fdStartTime = System.currentTimeMillis();
            List<FaceInfo> FaceInfoList = new ArrayList<>();
            int detectCode = mFaceEngine.detectFaces(bgr24, bitmap.getWidth(), bitmap.getHeight(), CP_PAF_BGR24, FaceInfoList);
            if(detectCode == ErrorInfo.MOK && FaceInfoList.size() > 0){
                Log.d(TAG, "Width: " + bitmap.getWidth() + " Height: " + bitmap.getHeight());
                Log.d(TAG, "Face detect successfully" + " size of FaceList is: " + FaceInfoList.size());
            }else{
                Log.d(TAG, "Face detect unsuccessfully" + detectCode);
            }
            List<Rect> rectList = new ArrayList<>();
            for(FaceInfo faceInfo : FaceInfoList){
                rectList.add(faceInfo.getRect());
                Log.d(TAG, "mFaceList add: " + faceInfo.getRect().bottom + "");
            }
            Bitmap tempBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            tempBitmap = DrawRect(tempBitmap, rectList);
            mImageView.setImageBitmap(tempBitmap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void activeEngine(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                int activeCode = mFaceEngine.active(MainActivity.this,
                        "7MZirLdWF5GXN9wJ1ZX9NcYLJR9eDHRR7eBW7o7TawfH",
                        "Fm1jfnsaJrXpFgEvWQd4hGHVXts6NPEejoGRK1yKARGN");
                if(activeCode == ErrorInfo.MOK){
                    toast = Toast.makeText(MainActivity.this, "Active Engine successfully", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d(TAG,"人脸引擎激活成功");
                }else {
                    toast = Toast.makeText(MainActivity.this, "Active Engine unsuccessfully", Toast.LENGTH_SHORT);
                    toast.show();
                    Looper.loop();
                    Log.d(TAG,"人脸引擎激活失败 " + activeCode);
                }
            }
        }).start();
    }

    public void initEngine(){
        int engineCode = mFaceEngine.init(this,
                FaceEngine.ASF_DETECT_MODE_VIDEO,
                FaceEngine.ASF_OP_0_HIGHER_EXT,
                3,
                20,
                FaceEngine.ASF_FACE_RECOGNITION |
                        FaceEngine.ASF_FACE_DETECT |
                        FaceEngine.ASF_AGE |
                        FaceEngine.ASF_GENDER |
                        FaceEngine.ASF_FACE3DANGLE);
        if(engineCode == ErrorInfo.MOK){
            toast = Toast.makeText(MainActivity.this, "Initialize Engine successfully", Toast.LENGTH_SHORT);
            toast.show();
            Log.d(TAG,"人脸引擎初始化成功");
        }else {
            toast = Toast.makeText(MainActivity.this, "Initialize Engine unsuccessfully", Toast.LENGTH_SHORT);
            toast.show();
            Log.d(TAG,"人脸引擎初始化失败 " + engineCode);
        }
    }
}
