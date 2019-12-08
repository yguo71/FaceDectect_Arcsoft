package com.example.facedectect_arcsoft.activity.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.FaceFeature;
import com.example.facedectect_arcsoft.R;
import com.example.facedectect_arcsoft.activity.faceserver.FaceServer;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.example.facedectect_arcsoft.activity.drawer.ImageProcess.bitmapToNv21;
import static com.example.facedectect_arcsoft.activity.drawer.ImageProcess.readPictureDegree;
import static com.example.facedectect_arcsoft.activity.drawer.ImageProcess.toturn;
import static com.example.facedectect_arcsoft.activity.faceserver.FaceServer.getInstance;

public class FaceAddActivity extends AppCompatActivity {
    private int REQ_CAMERA = 1;
    private int REQ_ALBUM = 2;
    private String PicturePath = Environment.getExternalStorageDirectory().getPath();
    private TextInputEditText textInputEditText = null;
    Bitmap bitmap = null;
    private byte[] NV21 = null;
    private Handler mhandler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faceadd);
        textInputEditText = (TextInputEditText)findViewById(R.id.inputName);
        FaceServer.getInstance().init(this);
    }



    public void takePhoto(View view){
        PicturePath = PicturePath + "/" + "temp.jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = Uri.fromFile(new File(PicturePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQ_CAMERA);
    }

    public void chooseFromAlbum(View view){
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_ALBUM);
    }

    public void addFaceInfo(View view){
        String name = null;
        View focusView = null;
        boolean cancel = false;
        if (TextUtils.isEmpty(textInputEditText.getText().toString())) {
            textInputEditText.setError("Please Enter name!");
            focusView = textInputEditText;
            cancel = true;
        }
        if(!cancel){
            if(NV21 == null){
                Toast.makeText(FaceAddActivity.this, "Please select a photo！", Toast.LENGTH_SHORT).show();
                cancel = true;
            }
        }
        if(cancel){
            focusView.requestFocus();
        }else{
            name = textInputEditText.getText().toString();
            boolean success =  FaceServer.getInstance().register(this, NV21, bitmap.getWidth(), bitmap.getHeight(), name);
            if(!success){
                Toast.makeText(FaceAddActivity.this, "Add Face unsuccessfully", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(FaceAddActivity.this, "Add Face successfully", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQ_CAMERA){
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(PicturePath);
                    bitmap = BitmapFactory.decodeStream(fis);
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            NV21 = bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
                        }
                    });
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
            else if(requestCode == REQ_ALBUM){
                Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String path = cursor.getString(columnIndex);  //获取照片路径
                cursor.close();
                bitmap = BitmapFactory.decodeFile(path);
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        NV21 = bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
                    }
                });
            }
        }
    }


}
