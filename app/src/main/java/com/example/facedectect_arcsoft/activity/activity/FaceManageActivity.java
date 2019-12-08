package com.example.facedectect_arcsoft.activity.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedectect_arcsoft.R;
import com.example.facedectect_arcsoft.activity.faceserver.FaceServer;
import com.example.facedectect_arcsoft.activity.listview.DataBean;
import com.example.facedectect_arcsoft.activity.listview.MyAdapter;

import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class FaceManageActivity extends AppCompatActivity {

    private ListView listView;

    private List<DataBean> mDatas;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facemanage);

        listView = (ListView) findViewById(R.id.listview);

        mDatas = new ArrayList<>();
        List<String> faceNames = FaceServer.getInstance().getAllRegisteredName();
        for (String str:faceNames) {
            DataBean dataBean = new DataBean(str);
            mDatas.add(dataBean);
        }

        mAdapter = new MyAdapter(this, mDatas);
        listView.setAdapter(mAdapter);
    }

    /**
     * 编辑、取消编辑
     * @param view
     */
    public void btnEditList(View view) {

//        mAdapter.flage = !mAdapter.flage;
//
//        if (mAdapter.flage) {
//            button.setText("取消");
//        } else {
//            button.setText("编辑");
//        }
//
//        mAdapter.notifyDataSetChanged();
    }
    /**
     * 全选
     * @param view
     */
    public void btnSelectAllList(View view) {
//        if (mAdapter.flage) {
        for (int i = 0; i < mDatas.size(); i++) {
            mDatas.get(i).isCheck = true;
        }

        mAdapter.notifyDataSetChanged();
//        }
    }
    /**
     * 全不选
     * @param view
     */
    public void btnNoList(View view) {

//        if (mAdapter.flage) {
        for (int i = 0; i < mDatas.size(); i++) {
            mDatas.get(i).isCheck = false;
        }

        mAdapter.notifyDataSetChanged();
//        }
    }

    /**
     * 反选
     * @param view
     */
    public void btnfanxuanList(View view) {
//        if (mAdapter.flage) {
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).isCheck) {
                mDatas.get(i).isCheck = false;
            } else {
                mDatas.get(i).isCheck = true;
            }
        }

        mAdapter.notifyDataSetChanged();
//        }
    }
    /**
     * 删除选中数据
     * @param view
     */
    public void btnOperateList(View view) {

        List<String> ids = new ArrayList<>();

//        if (mAdapter.flage) {

        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).isCheck) {
                ids.add(mDatas.get(i).getName());
                mDatas.remove(i);
            }
        }
        FaceServer.getInstance().deleteRegisteredFaceInfo(ids, this);
        mAdapter.notifyDataSetChanged();
        Toast.makeText(FaceManageActivity.this,"Deleted " + ids.toString(), Toast.LENGTH_SHORT).show();
        Log.e("TAG", ids.toString());
//        }
    }
}
