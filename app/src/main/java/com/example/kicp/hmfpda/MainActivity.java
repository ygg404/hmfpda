package com.example.kicp.hmfpda;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.fragment.ConfigFragment;
import com.example.kicp.hmfpda.fragment.DataFragment;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //提示窗口
    private Adialog aDialog;
    //单据类型
    private int BillType = 0;
    //通道
    @ViewInject(R.id.data_channel)
    private TextView datachannel;
    @ViewInject(R.id.sys_channel)
    private TextView syschannel;
//    @ViewInject(R.id.prod_channel)
//    private TextView prodchannel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        datachannel.setOnClickListener(this);
        syschannel.setOnClickListener(this);
//        prodchannel.setOnClickListener(this);
        aDialog = new Adialog(this);
    }
    /**
     * 重置所有通道的选中状态
     */
    protected void resetSelected(){
        datachannel.setSelected(false);
        syschannel.setSelected(false);
//        prodchannel.setSelected(false);
    }


    /**
     * 监听Back键按下事件
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */
    @Override
    public void onBackPressed() {
        Context mContext = MainActivity.this;
        // 创建退出对话框
        AlertDialog alert = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 设置对话框标题
        alert = builder.setIcon(R.mipmap.ic_launcher)
                .setTitle("系统提示：")
                .setMessage("确定要退出吗？")
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框
    }

    @Override
    public void onClick(View v) {
        resetSelected();
        switch (v.getId()) {
            //数据管理
            case R.id.data_channel:
                DataFragment fragment = new DataFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction tr = fragmentManager.beginTransaction();
                tr.replace(R.id.ly_content, fragment);
                tr.commit();
                datachannel.setSelected(true);
                break;
            //系统管理
            case R.id.sys_channel:
                ConfigFragment cfragment = new ConfigFragment();
                FragmentManager cfragmentManager = getFragmentManager();
                FragmentTransaction ctr = cfragmentManager.beginTransaction();
                ctr.replace(R.id.ly_content, cfragment);
                ctr.commit();
                syschannel.setSelected(true);
                break;
            default:
                break;
        }
    }
}
